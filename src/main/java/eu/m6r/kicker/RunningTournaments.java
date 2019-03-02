package eu.m6r.kicker;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.m6r.kicker.models.Tournament;
import eu.m6r.kicker.utils.ZookeeperClient;

public class RunningTournaments {
    private static final String TOURNAMENT_PATH = "/kicker/tournament";

    private final ZookeeperClient zookeeperClient;
    private final ObjectMapper mapper;

    public RunningTournaments(final String zookeeperHosts) throws IOException {
        this.zookeeperClient = new ZookeeperClient(zookeeperHosts);
        this.mapper = new ObjectMapper();
        zookeeperClient.createPath(TOURNAMENT_PATH);
    }

    private String path(final String channelId) {
        return String.format("%s/%s", TOURNAMENT_PATH, channelId);
    }

    public Tournament get(final String channelId)
            throws IOException, Controller.TournamentNotRunningException {

        final String value = zookeeperClient.readNode(path(channelId));
        if (value == null || value.isEmpty()) {
            throw new Controller.TournamentNotRunningException();
        }

        return mapper.readValue(value, Tournament.class);
    }

    public void clear(final String channelId) throws IOException {
        zookeeperClient.writeNode(path(channelId), "");
    }

    public void save(final Tournament tournament) throws IOException {
        final String tournamentPath = path(tournament.channel.id);
        zookeeperClient.writeNode(tournamentPath, mapper.writeValueAsString(tournament));
    }
}
