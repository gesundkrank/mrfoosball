/*
 * This file is part of kicker (https://github.com/mbrtargeting/kicker).
 * Copyright (c) 2019 Jan Graßegger.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

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
        zookeeperClient.deleteNode(path(channelId));
    }

    public void save(final Tournament tournament) throws IOException {
        final String tournamentPath = path(tournament.channel.id);
        zookeeperClient.writeNode(tournamentPath, jsonConverter.toString(tournament));
    }
}
