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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientProperties;

import de.gesundkrank.mrfoosball.models.SlackWorkspace;
import de.gesundkrank.mrfoosball.slack.models.AccessResponse;
import de.gesundkrank.mrfoosball.store.hibernate.Store;
import de.gesundkrank.mrfoosball.utils.Properties;


public class Registrar {

    private final Logger logger;
    private final Client client;
    private final String authHeader;

    public Registrar() {
        this.logger = LogManager.getLogger();
        this.client = ClientBuilder.newClient();
        client.property(ClientProperties.CONNECT_TIMEOUT, 30000);
        client.property(ClientProperties.READ_TIMEOUT, 30000);

        final var properties = Properties.getInstance();

        final var clientId = properties.getSlackClientId();
        final var clientSecret = properties.getSlackClientSecret();

        if (clientId == null || clientSecret == null) {
            throw new RuntimeException("clientId and clientSecret must not be null");
        }
        this.authHeader = getAuthHeader(clientId, clientSecret);
    }

    public void registerWorkspace(final String code) {
        logger.info("Trying to register new workspace...");

        final var form = new Form();
        form.param("code", code);

        final var response = client.target("https://slack.com")
                .path("/api/oauth.v2.access")
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .property(ClientProperties.FOLLOW_REDIRECTS, Boolean.TRUE)
                .header("Authorization", authHeader)
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        if (!response.getStatusInfo().equals(Response.Status.OK)) {
            logger.error("Call to access resource failed. StatusCode: {} Headers: {}.",
                         response.getStatus(), response.getStringHeaders());
            return;
        }

        final var accessResponse = response.readEntity(AccessResponse.class);
        logger.debug("Access response: {}", accessResponse);

        if (!accessResponse.ok) {
            logger.error("Call to access resource failed with error \"{}\"", accessResponse.error);
            return;
        }

        final var workspace = new SlackWorkspace(accessResponse.team.id,
                                                 accessResponse.accessToken,
                                                 accessResponse.scope,
                                                 accessResponse.team.name,
                                                 accessResponse.botUserId);

        try (final Store store = new Store()) {
            store.saveSlackWorkSpace(workspace);
        }

        logger.info("Successfully installed app to new workspace \"{}\" with ID \"{}\"",
                    workspace.teamName, workspace.teamId);
    }

    private static String getAuthHeader(final String clientId, final String clientSecret) {
        return "Basic " + Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));
    }
}
