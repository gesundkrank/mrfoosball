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

package de.gesundkrank.mrfoosball.slack;

import java.io.IOException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.client.ClientProperties;

import de.gesundkrank.mrfoosball.models.Player;
import de.gesundkrank.mrfoosball.models.SlackWorkspace;
import de.gesundkrank.mrfoosball.slack.models.SlackUser;
import de.gesundkrank.mrfoosball.utils.JsonConverter;

public class UserFetcher {

    private final Client client;
    private final JsonConverter jsonConverter;

    public UserFetcher() throws IOException {
        this.client = ClientBuilder.newClient();
        client.property(ClientProperties.CONNECT_TIMEOUT, 30000);
        client.property(ClientProperties.READ_TIMEOUT, 30000);

        this.jsonConverter = new JsonConverter(SlackUser.class);
    }

    public Player getUser(final String userId, final SlackWorkspace workspace)
            throws FetchUserFailedException {
        final var target = client.target("https://slack.com")
                .path("/api/users.info")
                .queryParam("token", workspace.accessToken)
                .queryParam("user", userId);
        try {
            final var userString = target.request(MediaType.APPLICATION_JSON).get(String.class);
            final var slackUser = jsonConverter.fromString(userString, SlackUser.class);

            if (slackUser.user.id == null || slackUser.user.name == null
                || slackUser.user.profile.image192 == null) {
                throw new FetchUserFailedException(userId);
            }

            final var player = new Player();
            player.id = slackUser.user.id;
            player.name = slackUser.user.name;
            player.avatarImage = slackUser.user.profile.image192;
            return player;
        } catch (final ResponseProcessingException | IOException e) {
            throw new FetchUserFailedException(userId, e);
        }
    }

    public static class FetchUserFailedException extends Exception {
        FetchUserFailedException(final String userId) {
            this(userId, null);
        }

        FetchUserFailedException(final String userId, final Throwable cause) {
            super(String.format("Failed to get user information for '%s' from slack API.", userId),
                  cause);
        }
    }
}
