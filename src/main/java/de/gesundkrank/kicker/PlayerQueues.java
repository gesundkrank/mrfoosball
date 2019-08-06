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

package de.gesundkrank.kicker;

import java.io.IOException;

import de.gesundkrank.kicker.models.Player;
import de.gesundkrank.kicker.models.PlayerQueue;
import de.gesundkrank.kicker.utils.JsonConverter;
import de.gesundkrank.kicker.utils.ZookeeperClient;

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
        zookeeperClient.deleteNode(path(channelId));
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