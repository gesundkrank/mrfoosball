package eu.m6r.kicker;

import eu.m6r.kicker.models.Match;
import eu.m6r.kicker.models.Player;
import eu.m6r.kicker.models.PlayerSkill;
import eu.m6r.kicker.models.State;
import eu.m6r.kicker.models.Team;
import eu.m6r.kicker.models.Tournament;
import eu.m6r.kicker.trueskill.TrueSkillCalculator;
import eu.m6r.kicker.utils.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Controller {

    private static Controller INSTANCE;

    private final Logger logger;
    private final TrueSkillCalculator trueSkillCalculator;
    private final PlayerQueue queue;
    private final RunningTournament runningTournament;

    public static Controller getInstance() throws IOException {
        if (INSTANCE == null) {
            INSTANCE = new Controller();
        }

        return INSTANCE;
    }

    private Controller() throws IOException {
        this.logger = LogManager.getLogger();
        this.trueSkillCalculator = new TrueSkillCalculator();

        final String zookeeperHosts = Properties.getInstance().zookeeperHosts();
        this.queue = new PlayerQueue(zookeeperHosts);
        this.runningTournament = new RunningTournament(zookeeperHosts);

    }

    public void startTournament() throws TournamentRunningException, IOException {
        startTournament(true, 3);
    }

    public synchronized Tournament startTournament(final boolean shuffle, final int bestOfN)
            throws TournamentRunningException, IOException {
        if (hasRunningTournament()) {
            throw new TournamentRunningException();
        }

        List<Player> playerList = queue.get();

        if (shuffle) {
            Collections.shuffle(playerList);
            playerList = trueSkillCalculator.getBestMatch(playerList);
        }

        queue.clear();

        try (final Store store = new Store()) {
            final Team teamA = store.getTeam(playerList.get(0), playerList.get(1));
            final Team teamB = store.getTeam(playerList.get(2), playerList.get(3));
            final Tournament tournament = new Tournament(bestOfN, teamA, teamB);
            runningTournament.save(tournament);
            return tournament;
        }

    }

    public synchronized void finishTournament()
            throws InvalidTournamentStateException, IOException, TournamentNotRunningException {
        final Tournament runningTournament = this.runningTournament.get();
        for (final Match match : runningTournament.matches) {
            if (match.state == State.RUNNING) {
                throw new InvalidTournamentStateException("Can't finish tournament if matches"
                                                          + "are still running!");
            }
        }

        runningTournament.state = State.FINISHED;

        try (final Store store = new Store()) {
            final Tournament updatedTournament =
                    trueSkillCalculator.updateRatings(runningTournament);
            store.saveTournament(updatedTournament);
        }

    }

    public List<Tournament> getTournaments() {
        try (final Store store = new Store()) {
            return store.getTournaments();
        }
    }

    public List<Tournament> getTournaments(int last) {
        try (final Store store = new Store()) {
            return store.getLastTournaments(last);
        }
    }

    public boolean hasRunningTournament() throws IOException {
        try {
            runningTournament.get();
            return true;
        } catch (TournamentNotRunningException e) {
            return false;
        }
    }

    public String newTournamentMessage() throws IOException, TournamentNotRunningException {
        final Tournament tournament = getRunningTournament();
        return String.format("A new game started:%n <@%s> <@%s> vs. <@%s> <@%s>",
                             tournament.teamA.player1.id, tournament.teamA.player2.id,
                             tournament.teamB.player1.id, tournament.teamB.player2.id);
    }

    public Tournament getRunningTournament() throws IOException, TournamentNotRunningException {
        return runningTournament.get();
    }

    public void updateTournament(final Tournament tournament)
            throws IOException, TournamentNotRunningException {
        final Tournament storedTournament = runningTournament.get();
        storedTournament.matches = tournament.matches;

        runningTournament.save(storedTournament);
    }

    public boolean cancelRunningTournament() throws IOException {
        if (!hasRunningTournament()) {
            return false;
        }

        runningTournament.clear();
        return true;
    }

    public void newMatch() throws InvalidTournamentStateException, TournamentNotRunningException,
                                  IOException {

        int teamAWins = 0;
        int teamBWins = 0;

        final Tournament tournament = runningTournament.get();

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

        final int maxTeamWins = (tournament.bestOfN / 2) + 1;

        if (maxTeamWins <= Math.max(teamAWins, teamBWins)) {
            throw new InvalidTournamentStateException("Cannot create more matches than bestOfN.");
        }

        tournament.matches.add(new Match());
        runningTournament.save(tournament);
    }

    public String getPlayersString() throws IOException {
        return queue.get().stream().map(p -> String.format("<@%s>", p.id))
                .collect(Collectors.joining(", "));
    }

    public boolean playerInQueue(final Player player) throws IOException {
        return queue.get().contains(player);
    }

    public void addPlayer(final String playerId) throws TooManyUsersException,
                                                        PlayerAlreadyInQueueException,
                                                        TournamentRunningException, IOException {
        try (final Store store = new Store()) {
            addPlayer(store.getPlayer(playerId), false);
        }
    }

    public void addPlayer(final Player player) throws TooManyUsersException,
                                                      PlayerAlreadyInQueueException,
                                                      TournamentRunningException, IOException {
        addPlayer(player, true);
    }

    public void addPlayer(final Player player, final boolean autoStartTournament)
            throws TournamentRunningException, PlayerAlreadyInQueueException,
                   TooManyUsersException, IOException {

        if (hasRunningTournament()) {
            throw new TournamentRunningException();
        }

        try (final Store store = new Store()) {
            final Player storedPlayer = store.getPlayer(player);
            if (storedPlayer != null) {
                player.trueSkillMean = storedPlayer.trueSkillMean;
                player.trueSkillStandardDeviation = storedPlayer.trueSkillStandardDeviation;
            }
        }

        queue.add(player);

        if (queue.get().size() == 4 && autoStartTournament) {
            startTournament();
        }
    }

    public void resetPlayers() throws IOException {
        queue.clear();
    }

    public void removePlayer(final Player player) throws IOException {
        queue.remove(player);
        logger.info("Removed {} from the queue", player);
    }

    public List<Player> getPlayersInQueue() throws IOException {
        return queue.get();
    }

    public List<PlayerSkill> playerSkills() {
        try (final Store store = new Store()) {
            return store.playerSkills();
        }
    }

    public void recalculateSkills() {
        try (final Store store = new Store()) {
            store.resetPlayerSkills();

            for (final Tournament tournament : getTournaments()) {
                final Tournament updatedTournament = trueSkillCalculator.updateRatings(tournament);
                store.getTeam(updatedTournament.teamA.player1, updatedTournament.teamA.player2);
                store.getTeam(updatedTournament.teamB.player1, updatedTournament.teamB.player2);
            }
        }
    }

    public static class TooManyUsersException extends Exception {

        public TooManyUsersException(final Player player) {
            super(String.format("Unable to add %s to the game. Too many users in the queue. "
                                + "Please remove users from the queue or start a game.",
                                player.name));
        }
    }

    public static class PlayerAlreadyInQueueException extends Exception {

        public PlayerAlreadyInQueueException(final Player player) {
            super(String.format("%s is already in the queue!", player.name));
        }
    }

    public static class TournamentRunningException extends Exception {

        public TournamentRunningException() {
            super("A tournament is already running!");
        }
    }

    public static class TournamentNotRunningException extends Exception {

        public TournamentNotRunningException() {
            super("No tournament is running!");
        }
    }

    public static class InvalidTournamentStateException extends Exception {

        public InvalidTournamentStateException(final String message) {
            super(message);
        }
    }
}
