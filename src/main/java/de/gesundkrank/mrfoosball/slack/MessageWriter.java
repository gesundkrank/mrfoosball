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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientProperties;

import de.gesundkrank.mrfoosball.slack.models.ApiResponse;
import de.gesundkrank.mrfoosball.slack.models.Message;

public class MessageWriter {

    private final Logger logger;
    private final String token;
    private final Client client;

    public MessageWriter(final String token) {
        this.logger = LogManager.getLogger();
        this.token = token;
        this.client = ClientBuilder.newClient();
        client.property(ClientProperties.CONNECT_TIMEOUT, 30000);
        client.property(ClientProperties.READ_TIMEOUT, 30000);
    }

    public void postEphemeral(final Message message) {
        sendMessageToApi(message, "chat.postEphemeral");
    }

    public void postMessage(final String channelId, final String messageString) {
        final var message = new Message(channelId, messageString, null);
        postMessage(message);
    }

    public void postMessage(final Message message) {
        sendMessageToApi(message, "chat.postMessage");
    }

    private void sendMessageToApi(final Message message, final String method) {
        final ApiResponse apiResponse = client.target("https://slack.com")
                .path("/api/" + method)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Bearer " + token).post(Entity.json(message))
                .readEntity(ApiResponse.class);
        if (!apiResponse.ok) {
            logger.error("Sending message {} failed. Error: {}", message, apiResponse.error);
        }
    }
}
