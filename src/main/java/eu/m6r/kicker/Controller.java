package eu.m6r.kicker;

import eu.m6r.kicker.models.Player;
import eu.m6r.kicker.models.Tournament;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public enum Controller {
    INSTANCE;

    private final Logger logger;
    private final Set<Player> players;

    Controller() {
        this.logger = LogManager.getLogger();
        this.players = new HashSet<>(6);
    }

    public void startTournament() {
        try (final Store store = new Store()) {
            store.newTournament(new ArrayList<>(players));
            players.clear();
        }
    }

    public List<Tournament> getTournaments() {
        try (final Store store = new Store()) {

            return store.getTournaments();
        }
    }

    public boolean hasRunningTournament() {
        try (final Store store = new Store()) {
            return store.hasRunningTournament();
        }
    }

    public Tournament getRunningTournament() {
        try (final Store store = new Store()) {
            return store.getRunningTournament();
        }
    }

    public void updateTournament(final Tournament tournament) {
        try (final Store store = new Store()) {
            logger.debug("Updating tournament");
            store.updateTournament(tournament);
        }
    }

    public boolean cancelRunningTournament() {
        if (!hasRunningTournament()) {
            return false;
        }

        try (final Store store = new Store()) {
            store.deleteTournament(getRunningTournament());
        }
        return true;
    }

    public void newMatch(int tournamentId) {
        try (final Store store = new Store()) {
            store.addMatch(tournamentId);
        }
    }

    public String getPlayersString() {
        return players.stream().map(p -> String.format("<@%s>", p.id))
                .collect(Collectors.joining(", "));
    }

    public String addPlayer(Player player)
            throws TooManyUsersException, PlayerAlreadyInQueueException {
        if (players.contains(player)) {
            throw new PlayerAlreadyInQueueException(player);
        }

        if (players.size() == 4) {
            throw new TooManyUsersException(player);
        }

        if (hasRunningTournament()) {
            return "A tournament is still running!";
        }

        players.add(player);

        if (players.size() == 4) {
            final String players = getPlayersString();
            startTournament();
            return String.format("%s a new game started!", players);
        }

        return String.format("Added %s to the queue.", player.name);
    }

    public void resetPlayers() {
        players.clear();
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public List<Player> getPlayersInQueue() {
        return new ArrayList<>(players);
    }

    public String getListOfPlayers() {
        List<String> playerNames = players.stream().map(player -> player.name)
                .collect(Collectors.toList());
        return String.join(",", playerNames);
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

}
