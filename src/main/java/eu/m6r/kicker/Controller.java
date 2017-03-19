package eu.m6r.kicker;

import eu.m6r.kicker.models.Player;
import eu.m6r.kicker.models.Tournament;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public enum Controller implements Closeable {
    INSTANCE;

    private final Logger logger;
    private final Store store;
    private final Set<Player> players;

    Controller() {
        this.logger = LogManager.getLogger();
        this.store = new Store();
        this.players = new HashSet<>(6);
    }

    public void startTournament() {
        store.newTournament(new ArrayList<>(players));
        players.clear();
    }

    public List<Tournament> getTournaments() {
        return store.getTournaments();
    }

    public boolean hasRunningTournament() {
        return store.hasRunningTournament();
    }

    public Tournament getRunningTournament() {
        return store.getRunningTournament();
    }

    public void updateTournament(final Tournament tournament) {
        logger.debug("Updating tournament");
        store.updateTournament(tournament);
    }

    public void newMatch(int tournamentId) {
        store.addMatch(tournamentId);
    }

    public String addPlayer(Player player) throws TooManyUsersException, PlayerAlreadyInQueueException {
        if (players.contains(player)) {
            throw new PlayerAlreadyInQueueException(player);
        }

        if (players.size() == 4) {
            throw new TooManyUsersException(player);
        }

        players.add(player);

        String playersString = players.stream().map(p -> String.format("<@%s>", p.id))
                .collect(Collectors.joining(", "));

        if (players.size() == 4) {
            startTournament();
            return String.format("%s a new game started!", playersString);
        }

        return String.format("Added %s to the queue. Current queue: %s.", player.name, playersString);
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


    @Override
    public void close() throws IOException {
        store.close();
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
