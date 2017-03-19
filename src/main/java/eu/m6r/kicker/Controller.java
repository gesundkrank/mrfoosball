package eu.m6r.kicker;

import eu.m6r.kicker.models.Tournament;
import eu.m6r.kicker.models.User;

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
    private final Set<User> players;

    Controller() {
        this.logger = LogManager.getLogger();
        this.store = new Store();
        this.players = new HashSet<>(6);
    }

    public void createTestTournament() {
        List<User> players = new ArrayList<>();

        User heye = new User();
        heye.id = "dfafsd";
        heye.name = "Heye";
        players.add(heye);

        User jan = new User();
        jan.id = "dfasdfsd";
        jan.name = "Jan";
        players.add(jan);

        User thomas = new User();
        thomas.id = "dfaffdfsd";
        thomas.name = "Thomas";
        players.add(thomas);

        User niklas = new User();
        niklas.id = "dfafvvvsd";
        niklas.name = "Niklas";
        players.add(niklas);

        store.newTournament(players);
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

    public String addPlayer(User user) throws TooManyUsersException, PlayerAlreadyInQueueException {
        if (players.contains(user)) {
            throw new PlayerAlreadyInQueueException(user);
        }

        if (players.size() == 4) {
            throw new TooManyUsersException(user);
        }

        players.add(user);

        String playersString = players.stream().map(player -> String.format("<@%s>", player.id))
                .collect(Collectors.joining(", "));

        if (players.size() == 4) {
            startTournament();
            return String.format("%s a new game started!", playersString);
        }

        return String.format("Added %s to the queue. Current queue: %s.", user.name, playersString);
    }

    public void resetPlayers() {
        players.clear();
    }

    public void removePlayer(User user) {
        players.remove(user);
    }

    public List<User> getPlayersInQueue() {
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

        public TooManyUsersException(final User user) {
            super(String.format("Unable to add %s to the game. Too many users in the queue. "
                                + "Please remove users from the queue or start a game.",
                                user.name));
        }
    }

    public static class PlayerAlreadyInQueueException extends Exception {

        public PlayerAlreadyInQueueException(final User user) {
            super(String.format("%s is already in the queue!", user.name));
        }
    }

}
