package eu.m6r.kicker;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.m6r.kicker.models.Player;
import eu.m6r.kicker.utils.ZookeeperClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlayerQueue {

    private static final String QUEUE_PATH = "/kicker/queue";

    private final ZookeeperClient zookeeperClient;
    private final ObjectMapper mapper;


    public PlayerQueue(final String zookeeperHosts) throws IOException {
        this.zookeeperClient = new ZookeeperClient(zookeeperHosts);
        this.mapper = new ObjectMapper();
    }

    public List<Player> get() throws IOException {

        final String value = zookeeperClient.readNode(QUEUE_PATH);
        if (value == null || value.isEmpty()) {
            return new ArrayList<>();
        }

        return mapper.readValue(value, new TypeReference<List<Player>>() {
        });
    }

    public void clear() throws IOException {
        zookeeperClient.writeNode(QUEUE_PATH, "");
    }

    public void add(final Player player)
            throws IOException, Controller.PlayerAlreadyInQueueException,
                   Controller.TooManyUsersException {
        final List<Player> players = get();

        if (players.contains(player)) {
            throw new Controller.PlayerAlreadyInQueueException(player);
        }

        if (players.size() == 4) {
            throw new Controller.TooManyUsersException(player);
        }

        players.add(player);
        save(players);
    }

    public void remove(final Player player) throws IOException {
        List<Player> players = get();
        players.remove(player);
        save(players);
    }

    private void save(final List<Player> players) throws IOException {
        zookeeperClient.writeNode(QUEUE_PATH, mapper.writeValueAsString(players));
    }
}
