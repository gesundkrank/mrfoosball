/*
 * This file is part of MrFoosball (https://github.com/gesundkrank/mrfoosball).
 * Copyright (c) 2020 Jan Graßegger.
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

package de.gesundkrank.mrfoosball.store.zookeeper;

import java.io.IOException;

import de.gesundkrank.mrfoosball.Controller;
import de.gesundkrank.mrfoosball.models.Tournament;
import de.gesundkrank.mrfoosball.utils.JsonConverter;

public class RunningTournaments extends ZookeeperClient {

    private final JsonConverter jsonConverter;

    public RunningTournaments(final String zookeeperHosts) throws IOException {
        super(zookeeperHosts, "tournament");
        this.jsonConverter = new JsonConverter(Tournament.class);
    }

    private String path(final String channelId) {
        return String.format("%s/%s", subDir, channelId);
    }

    public Tournament get(final String channelId)
            throws IOException, Controller.TournamentNotRunningException {

        final String value = readNode(path(channelId));
        if (value == null || value.isEmpty()) {
            throw new Controller.TournamentNotRunningException();
        }

        return jsonConverter.fromString(value, Tournament.class);
    }

    public void clear(final String channelId) throws IOException {
        deleteNode(path(channelId));
    }

    public void save(final Tournament tournament) throws IOException {
        final String tournamentPath = path(tournament.channel.id);
        writeNode(tournamentPath, jsonConverter.toString(tournament));
    }
}
