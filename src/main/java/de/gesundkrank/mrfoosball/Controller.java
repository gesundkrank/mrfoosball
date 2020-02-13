/*
 * This file is part of MrFoosball (https://github.com/gesundkrank/mrfoosball).
 * Copyright (c) 2020 Jan Graßegger.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.gesundkrank.mrfoosball;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.gesundkrank.mrfoosball.models.Channel;
import de.gesundkrank.mrfoosball.models.Crawl;
import de.gesundkrank.mrfoosball.models.Match;
import de.gesundkrank.mrfoosball.models.Player;
import de.gesundkrank.mrfoosball.models.PlayerQueue;
import de.gesundkrank.mrfoosball.models.PlayerSkill;
import de.gesundkrank.mrfoosball.models.State;
import de.gesundkrank.mrfoosball.models.Team;
import de.gesundkrank.mrfoosball.models.Tournament;
import de.gesundkrank.mrfoosball.slack.MessageWriter;
import de.gesundkrank.mrfoosball.slack.models.Message;
import de.gesundkrank.mrfoosball.store.hibernate.Store;
import de.gesundkrank.mrfoosball.store.zookeeper.LastCrawl;
import de.gesundkrank.mrfoosball.store.zookeeper.PlayerQueues;
import de.gesundkrank.mrfoosball.store.zookeeper.RunningTournaments;
import de.gesundkrank.mrfoosball.trueskill.PlayerTrueSkillCalculator;
import de.gesundkrank.mrfoosball.trueskill.TeamTrueSkillCalculator;
import de.gesundkrank.mrfoosball.trueskill.TrueSkillCalculator;
import de.gesundkrank.mrfoosball.utils.Properties;

public class Controller {

    private static volatile Controller INSTANCE;

    private final Logger logger;
    private final TrueSkillCalculator playerTrueSkillCalculator;
    private final TrueSkillCalculator teamTrueSkillCalculator;
    private final PlayerQueues queues;
    private final RunningTournaments runningTournaments;
    private final LastCrawl lastCrawl;
    private final String baseUrl;
    private final MessageWriter messageWriter;

    public static Controller getInstance() throws IOException {
        if (INSTANCE == null) {
            INSTANCE = new Controller();
        }

        return INSTANCE;
    }

    private Controller() throws IOException {
        this.logger = LogManager.getLogger();
        this.playerTrueSkillCalculator = new PlayerTrueSkillCalculator();
        this.teamTrueSkillCalculator = new TeamTrueSkillCalculator();

        final var properties = Properties.getInstance();
        final var zookeeperHosts = properties.zookeeperHosts();
        this.queues = new PlayerQueues(zookeeperHosts);
        this.runningTournaments = new RunningTournaments(zookeeperHosts);
        this.lastCrawl = new LastCrawl(zookeeperHosts);
        this.baseUrl = properties.getAppUrl();
        this.messageWriter = new MessageWriter(properties.getSlackToken());
    }

    public String joinChannel(final String slackId, final String slackName) {
        final var id = UUID.randomUUID().toString();
        final var channel = new Channel();
        channel.id = id;
        channel.name = slackName;
        channel.slackId = slackId;

        try (final var store = new Store()) {
            store.saveChannel(channel);
        }

        return id;
    }

    private void startTournament(final String channelId)
            throws TournamentRunningException, IOException {
        startTournament(channelId, true, 3);
    }

    public void startTournament(final String channelId, final boolean shuffle,
                                final int bestOfN)
            throws IOException, TournamentRunningException {
        var playerList = queues.get(channelId).queue;
        queues.clear(channelId);
        startTournament(channelId, shuffle, bestOfN, playerList);
    }

    private void startTournament(final String channelId, final int bestOfN, final Team teamA,
                                 final Team teamB) throws IOException, TournamentRunningException {
        final var list = List.of(teamA.player1, teamA.player2, teamB.player1, teamB.player2);
        startTournament(channelId, false, bestOfN, list);
    }

    private synchronized void startTournament(final String channelId, final boolean shuffle,
                                              final int bestOfN, List<Player> playerList)
            throws TournamentRunningException, IOException {
        if (hasRunningTournament(channelId)) {
            throw new TournamentRunningException();
        }

        if (shuffle) {
            Collections.shuffle(playerList);
            playerList = TrueSkillCalculator.getBestMatch(playerList);
        }

        try (final var store = new Store()) {
            final var teamA = store.getTeam(playerList.get(0), playerList.get(1));
            final var teamB = store.getTeam(playerList.get(2), playerList.get(3));
            final var channel = store.getChannel(channelId);
            final var tournament = new Tournament(bestOfN, teamA, teamB, channel);
            runningTournaments.save(tournament);

            final var message = String.format("A new game started:%n <@%s> <@%s> vs. <@%s> <@%s>",
                                              tournament.teamA.player1.id,
                                              tournament.teamA.player2.id,
                                              tournament.teamB.player1.id,
                                              tournament.teamB.player2.id);
            messageWriter.postMessage(tournament.channel.slackId, message);

        }


    }

    public synchronized void finishTournament(final String channelId, final boolean rematch)
            throws InvalidTournamentStateException, IOException, TournamentNotRunningException {
        final var runningTournament = this.runningTournaments.get(channelId);
        for (final var match : runningTournament.matches) {
            if (match.state == State.RUNNING) {
                throw new InvalidTournamentStateException("Can't finish tournament if matches"
                                                          + "are still running!");
            }
        }

        runningTournament.state = State.FINISHED;

        try (final var store = new Store()) {
            var updatedTournament =
                    playerTrueSkillCalculator.updateRatings(runningTournament);
            updatedTournament =
                    teamTrueSkillCalculator.updateRatings(updatedTournament);
            store.saveTournament(updatedTournament);
        }

        checkCrawl(runningTournament, channelId);
        this.runningTournaments.clear(channelId);

        final var winner = runningTournament.winner();
        final var message = String.format("The game is over. Congratulations to <@%s> and <@%s>!",
                                          winner.player1.id,
                                          winner.player2.id);
        messageWriter.postMessage(runningTournament.channel.slackId, message);

        try {
            if (rematch) {
                startRematch(runningTournament);
            } else if (queues.get(channelId).isFull()) {
                startTournament(channelId);
            }
        } catch (TournamentRunningException e) {
            logger.error("This should not happen.", e);
        }
    }

    private void startRematch(final Tournament tournament)
            throws IOException, TournamentRunningException {
        final Team teamA;
        final Team teamB;

        if (tournament.matches.size() % 2 == 0) {
            teamA = tournament.teamA;
            teamB = tournament.teamB;
        } else {
            teamA = tournament.teamB;
            teamB = tournament.teamA;
        }

        startTournament(tournament.channel.id, tournament.bestOfN, teamA, teamB);
    }

    public List<Tournament> getTournaments(final String channelId) {
        try (final var store = new Store()) {
            return store.getTournaments(channelId);
        }
    }

    public List<Tournament> getTournaments(final String channelId, final int last) {
        try (final var store = new Store()) {
            return store.getLastTournaments(channelId, last);
        }
    }

    public String getChannelUrl(final String channelId) {
        final var appUrl = Properties.getInstance().getAppUrl();
        try (final var store = new Store()) {
            return appUrl + "/" + store.getChannel(channelId).id;
        }
    }

    public boolean hasRunningTournament(final String channelId) throws IOException {
        try {
            runningTournaments.get(channelId);
            return true;
        } catch (TournamentNotRunningException e) {
            return false;
        }
    }

    public Tournament getRunningTournament(final String channelId)
            throws IOException, TournamentNotRunningException {
        return runningTournaments.get(channelId);
    }

    public void updateTournament(final String channelId, final Tournament tournament)
            throws IOException, TournamentNotRunningException {
        final var storedTournament = runningTournaments.get(channelId);
        storedTournament.matches = tournament.matches;

        runningTournaments.save(storedTournament);
    }

    public boolean cancelRunningTournament(final String channelId) throws IOException {
        if (!hasRunningTournament(channelId)) {
            return false;
        }

        runningTournaments.clear(channelId);
        return true;
    }

    public void newMatch(final String channelId)
            throws InvalidTournamentStateException, TournamentNotRunningException, IOException {

        int teamAWins = 0;
        int teamBWins = 0;

        final var tournament = runningTournaments.get(channelId);

        for (final Match match : tournament.matches) {
            if (match.state == State.FINISHED) {
                if (match.teamA > match.teamB) {
                    teamAWins++;
                } else {
                    teamBWins++;
                }
            } else if (match.state == State.RUNNING) {
                throw new InvalidTournamentStateException("Can't create new match if matches "
                                                          + "are still running!");
            }
        }

        final var maxTeamWins = (tournament.bestOfN / 2) + 1;

        if (maxTeamWins <= Math.max(teamAWins, teamBWins)) {
            throw new InvalidTournamentStateException("Cannot create more matches than bestOfN.");
        }

        checkCrawl(tournament, channelId);
        tournament.matches.add(new Match());
        runningTournaments.save(tournament);
    }

    public String getPlayersString(final String channelId) throws IOException {
        return queues.get(channelId).queue.stream().map(p -> String.format("<@%s>", p.id))
                .collect(Collectors.joining(", "));
    }

    public void addPlayer(final String channelId, final Player player)
            throws PlayerQueue.PlayerAlreadyInQueueException, PlayerQueue.TooManyUsersException,
                   IOException {

        try (final var store = new Store()) {
            final var storedPlayer = store.getPlayer(player);
            if (storedPlayer != null) {
                player.trueSkillMean = storedPlayer.trueSkillMean;
                player.trueSkillStandardDeviation = storedPlayer.trueSkillStandardDeviation;
            }
        }

        queues.add(channelId, player);

        if (queues.get(channelId).isFull() && !hasRunningTournament(channelId)) {
            try {
                startTournament(channelId);
            } catch (TournamentRunningException e) {
                logger.error("This should not happen", e);
            }
        }
    }

    public void resetPlayers(final String channelId) throws IOException {
        queues.clear(channelId);
    }

    public void removePlayer(final String channelId, final String playerId) throws IOException {
        final var player = new Player(playerId);
        removePlayer(channelId, player);
    }

    public void removePlayer(final String channelId, final Player player) throws IOException {
        queues.remove(channelId, player);
        logger.info("Removed {} from the queues", player);
    }

    public List<Player> getPlayersInQueue(final String channelId) throws IOException {
        return queues.get(channelId).queue;
    }

    public List<PlayerSkill> playerSkills(final String channelId) {
        try (final var store = new Store()) {
            return store.playerSkills(channelId);
        }
    }

    public String getChannelId(String slackChannelId) {
        try (final var store = new Store()) {
            return store.getChannelBySlackId(slackChannelId).id;
        }
    }

    public String getChannelQRCodeUrl(final String channelId) {
        return String.format("%s/api/channel/%s/qrcode", baseUrl, channelId);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public Crawl getLastCrawl(String channelId)
            throws IOException, Controller.NoLastCrawlException {
        return lastCrawl.get(channelId);
    }

    private void checkCrawl(final Tournament tournament, final String channelId)
            throws IOException {
        if (tournament.matches.isEmpty()) {
            return;
        }

        final var slackId = tournament.channel.slackId;
        final var lastMatch = tournament.matches.get(tournament.matches.size() - 1);

        final Team losers;
        final Team winners;
        if (lastMatch.teamA == 0) {
            losers = tournament.teamA;
            winners = tournament.teamB;
        } else if (lastMatch.teamB == 0) {
            losers = tournament.teamB;
            winners = tournament.teamA;
        } else {
            return;
        }

        final Crawl crawl = new Crawl(channelId, winners, losers);
        lastCrawl.save(crawl);

        var messageString = String.format("<@%s> and <@%s> have to crawl. How embarrassing!!",
                                          losers.player1.id, losers.player2.id
        );

        final var message = new Message(slackId, messageString, null);
        messageWriter.postMessage(message);
    }

    public static class NoLastCrawlException extends Exception {

    }

    public static class TournamentRunningException extends Exception {

        TournamentRunningException() {
            super("A tournament is already running!");
        }
    }

    public static class TournamentNotRunningException extends Exception {

        public TournamentNotRunningException() {
            super("No tournament is running!");
        }
    }

    public static class InvalidTournamentStateException extends Exception {

        InvalidTournamentStateException(final String message) {
            super(message);
        }
    }
}
