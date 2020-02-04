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

import de.gesundkrank.mrfoosball.models.Player;
import de.gesundkrank.mrfoosball.models.PlayerQueue;
import de.gesundkrank.mrfoosball.utils.JsonConverter;

public class PlayerQueues extends ZookeeperClient {

    private final JsonConverter jsonConverter;

    public PlayerQueues(final String zookeeperHosts) throws IOException {
        super(zookeeperHosts, "queue");
        this.jsonConverter = new JsonConverter(PlayerQueue.class, Player.class);
    }

    private String path(final String channelId) {
        return String.format("%s/%s", subDir, channelId);
    }

    public PlayerQueue get(final String channelId) throws IOException {

        final String value = readNode(path(channelId));
        if (value == null || value.isEmpty()) {
            return new PlayerQueue();
        }

        return jsonConverter.fromString(value, PlayerQueue.class);
    }

    public void clear(final String channelId) throws IOException {
        deleteNode(path(channelId));
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
        writeNode(path(channelId), jsonConverter.toString(players));
    }
}
