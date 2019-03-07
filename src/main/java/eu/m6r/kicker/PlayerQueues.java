package eu.m6r.kicker;

import java.io.IOException;

import eu.m6r.kicker.models.Player;
import eu.m6r.kicker.models.PlayerQueue;
import eu.m6r.kicker.utils.JsonConverter;
import eu.m6r.kicker.utils.ZookeeperClient;

public class PlayerQueues {

    private static final String QUEUE_PATH = "/kicker/queue/v2";

    private final ZookeeperClient zookeeperClient;
    private final JsonConverter jsonConverter;

    public PlayerQueues(final String zookeeperHosts) throws IOException {
        this.zookeeperClient = new ZookeeperClient(zookeeperHosts);
        this.jsonConverter = new JsonConverter(PlayerQueue.class, Player.class);

        zookeeperClient.createPath(QUEUE_PATH);
    }

    private String path(final String channelId) {
        return String.format("%s/%s", QUEUE_PATH, channelId);
    }

    public PlayerQueue get(final String channelId) throws IOException {

        final String value = zookeeperClient.readNode(path(channelId));
        if (value == null || value.isEmpty()) {
            return new PlayerQueue();
        }

        return jsonConverter.fromString(value, PlayerQueue.class);
    }

    public void clear(final String channelId) throws IOException {
        zookeeperClient.writeNode(path(channelId), "");
    }

    public void add(final String channelId, final Player player)
            throws IOException, PlayerQueue.PlayerAlreadyInQueueException,
                   PlayerQueue.TooManyUsersException {
        final var queue = get(channelId);

        queue.addPlayer(player);
        save(channelId, queue);
    }

    public void remove(final String channelId, final Player player) throws IOException {
        final var players = get(channelId);
        players.remove(player);
        save(channelId, players);
    }

    private void save(final String channelId, final PlayerQueue players) throws IOException {
        zookeeperClient.writeNode(path(channelId), jsonConverter.toString(players));
    }
}
