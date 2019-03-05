package eu.m6r.kicker;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.m6r.kicker.models.Channel;
import eu.m6r.kicker.models.Match;
import eu.m6r.kicker.models.Player;
import eu.m6r.kicker.models.PlayerSkill;
import eu.m6r.kicker.models.State;
import eu.m6r.kicker.models.Team;
import eu.m6r.kicker.models.Tournament;
import eu.m6r.kicker.slack.MessageWriter;
import eu.m6r.kicker.slack.models.Message;
import eu.m6r.kicker.store.Store;
import eu.m6r.kicker.trueskill.TrueSkillCalculator;
import eu.m6r.kicker.utils.Properties;

public class Controller {

    private static Controller INSTANCE;

    private final Logger logger;
    private final TrueSkillCalculator trueSkillCalculator;
    private final PlayerQueues queues;
    private final RunningTournaments runningTournaments;
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
        this.trueSkillCalculator = new TrueSkillCalculator();

        final var properties = Properties.getInstance();
        final var zookeeperHosts = properties.zookeeperHosts();
        this.queues = new PlayerQueues(zookeeperHosts);
        this.runningTournaments = new RunningTournaments(zookeeperHosts);
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

    public synchronized Tournament startTournament(final String channelId, final boolean shuffle,
                                                   final int bestOfN)
            throws TournamentRunningException, IOException {
        if (hasRunningTournament(channelId)) {
            throw new TournamentRunningException();
        }

        List<Player> playerList = queues.get(channelId);

        if (shuffle) {
            Collections.shuffle(playerList);
            playerList = trueSkillCalculator.getBestMatch(playerList);
        }

        queues.clear(channelId);

        try (final var store = new Store()) {
            final var teamA = store.getTeam(playerList.get(0), playerList.get(1));
            final var teamB = store.getTeam(playerList.get(2), playerList.get(3));
            final var channel = store.getChannel(channelId);
            final var tournament = new Tournament(bestOfN, teamA, teamB, channel);
            runningTournaments.save(tournament);
            return tournament;
        }

    }

    public synchronized void finishTournament(final String channelId)
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
            final var updatedTournament =
                    trueSkillCalculator.updateRatings(runningTournament);
            store.saveTournament(updatedTournament);
        }

        checkCrawlShaming(runningTournament);
        this.runningTournaments.clear(channelId);

        final var winner = runningTournament.winner();
        final Message message =
                new Message(runningTournament.channel.slackId,
                            String.format("The game is over. Congratulations to <@%s> and <@%s>!",
                                          winner.player1.id,
                                          winner.player2.id), null);
        messageWriter.postMessage(message);

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

        checkCrawlShaming(tournament);
        tournament.matches.add(new Match());
        runningTournaments.save(tournament);
    }

    public String getPlayersString(final String channelId) throws IOException {
        return queues.get(channelId).stream().map(p -> String.format("<@%s>", p.id))
                .collect(Collectors.joining(", "));
    }

    public void addPlayer(final String channelId, final String playerId)
            throws TooManyUsersException, PlayerAlreadyInQueueException,
                   TournamentRunningException, IOException {
        try (final Store store = new Store()) {
            addPlayer(channelId, store.getPlayer(playerId), false);
        }
    }

    public void addPlayer(final String channelId, final Player player)
            throws TooManyUsersException, PlayerAlreadyInQueueException,
                   TournamentRunningException, IOException {
        addPlayer(channelId, player, true);
    }

    public void addPlayer(final String channelId, final Player player,
                          final boolean autoStartTournament)
            throws TournamentRunningException, PlayerAlreadyInQueueException,
                   TooManyUsersException, IOException {

        if (hasRunningTournament(channelId)) {
            throw new TournamentRunningException();
        }

        try (final var store = new Store()) {
            final var storedPlayer = store.getPlayer(player);
            if (storedPlayer != null) {
                player.trueSkillMean = storedPlayer.trueSkillMean;
                player.trueSkillStandardDeviation = storedPlayer.trueSkillStandardDeviation;
            }
        }

        queues.add(channelId, player);

        if (queues.get(channelId).size() == 4 && autoStartTournament) {
            startTournament(channelId);
        }
    }

    public void resetPlayers(final String channelId) throws IOException {
        queues.clear(channelId);
    }

    public void removePlayer(final String channelId, final Player player) throws IOException {
        queues.remove(channelId, player);
        logger.info("Removed {} from the queues", player);
    }

    public List<Player> getPlayersInQueue(final String channelId) throws IOException {
        return queues.get(channelId);
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

    private void checkCrawlShaming(final Tournament tournament) {
        if (tournament.matches.isEmpty()) {
            return;
        }

        final var slackId = tournament.channel.slackId;
        final var lastMatch = tournament.matches.get(tournament.matches.size() - 1);

        final Team loosers;
        if (lastMatch.teamA == 0) {
            loosers = tournament.teamA;
        } else if (lastMatch.teamB == 0) {
            loosers = tournament.teamB;
        } else {
            return;
        }

        var messageString = String.format("<@%s> and <@%s> have to crawl. How embarrassing!!",
                                    loosers.player1.id, loosers.player2.id);

        final var message = new Message(slackId, messageString, null);
        messageWriter.postMessage(message);
    }

    public static class TooManyUsersException extends Exception {

        TooManyUsersException(final Player player) {
            super(String.format("Unable to add %s to the game. Too many users in the queues. "
                                + "Please remove users from the queues or start a game.",
                                player.name));
        }
    }

    public static class PlayerAlreadyInQueueException extends Exception {

        PlayerAlreadyInQueueException(final Player player) {
            super(String.format("%s is already in the queues!", player.name));
        }
    }

    public static class TournamentRunningException extends Exception {

        TournamentRunningException() {
            super("A tournament is already running!");
        }
    }

    public static class TournamentNotRunningException extends Exception {

        TournamentNotRunningException() {
            super("No tournament is running!");
        }
    }

    public static class InvalidTournamentStateException extends Exception {

        InvalidTournamentStateException(final String message) {
            super(message);
        }
    }
}
