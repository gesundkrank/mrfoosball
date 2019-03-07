package eu.m6r.kicker.models;

import java.util.ArrayList;
import java.util.List;

public class PlayerQueue {

    public List<Player> queue;

    public PlayerQueue() {
        this.queue = new ArrayList<>();
    }

    public void addPlayer(final Player player) throws PlayerAlreadyInQueueException,
                                                         TooManyUsersException {
        if (queue.contains(player)) {
            throw new PlayerAlreadyInQueueException(player);
        }

        if (isFull()) {
            throw new TooManyUsersException(player);
        }

        queue.add(player);
    }

    public void remove(final Player player) {
        queue.remove(player);
    }

    public boolean isFull() {
        return queue.size() == 4;
    }

    public static class PlayerAlreadyInQueueException extends Exception {

        PlayerAlreadyInQueueException(final Player player) {
            super(String.format("<@%s> is already in the queue!", player.name));
        }
    }

    public static class TooManyUsersException extends Exception {

        TooManyUsersException(final Player player) {
            super(String.format("Unable to add <@%s> to the game. Too many users in the queue. "
                                + "Please remove users from the queue or start a game.",
                                player.name));
        }
    }
}
