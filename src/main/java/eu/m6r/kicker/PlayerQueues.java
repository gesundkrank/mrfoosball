package eu.m6r.kicker;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.m6r.kicker.models.Player;
import eu.m6r.kicker.utils.ZookeeperClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlayerQueues {

    private static final String QUEUE_PATH = "/kicker/queue";

    private final ZookeeperClient zookeeperClient;
    private final ObjectMapper mapper;

    public PlayerQueues(final String zookeeperHosts) throws IOException {
        this.zookeeperClient = new ZookeeperClient(zookeeperHosts);
        this.mapper = new ObjectMapper();

        zookeeperClient.createPath(QUEUE_PATH);
    }

    private String path(final String channelId) {
        return String.format("%s/%s", QUEUE_PATH, channelId);
    }

    public List<Player> get(final String channelId) throws IOException {

        final String value = zookeeperClient.readNode(path(channelId));
        if (value == null || value.isEmpty()) {
            return new ArrayList<>();
        }

        return mapper.readValue(value, new TypeReference<List<Player>>() {
        });
    }

    public void clear(final String channelId) throws IOException {
        zookeeperClient.writeNode(path(channelId), "");
    }

    public void add(final String channelId, final Player player)
            throws IOException, Controller.PlayerAlreadyInQueueException,
                   Controller.TooManyUsersException {
        final List<Player> players = get(channelId);

        if (players.contains(player)) {
            throw new Controller.PlayerAlreadyInQueueException(player);
        }

        if (players.size() == 4) {
            throw new Controller.TooManyUsersException(player);
        }

        players.add(player);
        save(channelId, players);
    }

    public void remove(final String channelId, final Player player) throws IOException {
        List<Player> players = get(channelId);
        players.remove(player);
        save(channelId, players);
    }

    private void save(final String channelId, final List<Player> players) throws IOException {
        zookeeperClient.writeNode(path(channelId), mapper.writeValueAsString(players));
    }
}
