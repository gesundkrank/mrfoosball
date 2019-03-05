package eu.m6r.kicker;

import java.io.IOException;

import eu.m6r.kicker.models.Tournament;
import eu.m6r.kicker.utils.JsonConverter;
import eu.m6r.kicker.utils.ZookeeperClient;

public class RunningTournaments {
    private static final String TOURNAMENT_PATH = "/kicker/tournament";

    private final ZookeeperClient zookeeperClient;
    private final JsonConverter jsonConverter;

    public RunningTournaments(final String zookeeperHosts) throws IOException {
        this.zookeeperClient = new ZookeeperClient(zookeeperHosts);
        zookeeperClient.createPath(TOURNAMENT_PATH);
        this.jsonConverter = new JsonConverter(Tournament.class);
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

        return jsonConverter.fromString(value, Tournament.class);
    }

    public void clear(final String channelId) throws IOException {
        zookeeperClient.writeNode(path(channelId), "");
    }

    public void save(final Tournament tournament) throws IOException {
        final String tournamentPath = path(tournament.channel.id);
        zookeeperClient.writeNode(tournamentPath, jsonConverter.toString(tournament));
    }
}
