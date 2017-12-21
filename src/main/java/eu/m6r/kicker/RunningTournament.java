package eu.m6r.kicker;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.m6r.kicker.models.Tournament;
import eu.m6r.kicker.utils.ZookeeperClient;
import java.io.IOException;

public class RunningTournament {
    private static final String TOURNAMENT_PATH = "/kicker/tournament";

    private final ZookeeperClient zookeeperClient;
    private final ObjectMapper mapper;

    public RunningTournament(final String zookeeperHosts) throws IOException {
        this.zookeeperClient = new ZookeeperClient(zookeeperHosts);
        this.mapper = new ObjectMapper();
    }

    public Tournament get() throws IOException, Controller.TournamentNotRunningException {

        final String value = zookeeperClient.readNode(TOURNAMENT_PATH);
        if (value == null || value.isEmpty()) {
            throw new Controller.TournamentNotRunningException();
        }

        return mapper.readValue(value, Tournament.class);
    }

    public void clear() throws IOException {
        zookeeperClient.writeNode(TOURNAMENT_PATH, "");
    }

    public void save(final Tournament tournament) throws IOException {
        zookeeperClient.writeNode(TOURNAMENT_PATH, mapper.writeValueAsString(tournament));
    }
}
