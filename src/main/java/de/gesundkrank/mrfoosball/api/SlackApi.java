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

package de.gesundkrank.mrfoosball.api;

import java.io.IOException;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.server.ManagedAsync;

import de.gesundkrank.mrfoosball.api.annotations.VerifySlackRequest;
import de.gesundkrank.mrfoosball.slack.Bot;
import de.gesundkrank.mrfoosball.slack.Registrar;
import de.gesundkrank.mrfoosball.slack.models.EventWrapper;
import de.gesundkrank.mrfoosball.slack.models.UrlVerification;
import de.gesundkrank.mrfoosball.utils.JsonConverter;

@Path("/api/slack")
public class SlackApi {

    private static final Logger logger = LogManager.getLogger();

    private final JsonConverter jsonConverter;

    @Inject
    private Bot slackBot;

    public SlackApi() throws IOException {
        this.jsonConverter = new JsonConverter(EventWrapper.class, UrlVerification.class);
    }

    @POST
    @ManagedAsync
    @VerifySlackRequest
    @Consumes(MediaType.APPLICATION_JSON)
    public void eventApiEndPoint(final String content,
                                 @Suspended final AsyncResponse asyncResponse) {
        try {
            if (content.contains("url_verification")) {
                final var urlVerification =
                        jsonConverter.fromString(content, UrlVerification.class);
                asyncResponse.resume(Response.ok(urlVerification.challenge).build());
                return;
            }

            asyncResponse.resume(Response.ok().build());

            final var wrappedEvent = jsonConverter.fromString(content, EventWrapper.class);
            logger.info("Received event:\n{}", wrappedEvent);

            switch (wrappedEvent.event.type) {
                case "app_mention":
                    slackBot.onAppMention(wrappedEvent);
                    break;
                case "member_joined_channel":
                    slackBot.onChannelJoined(wrappedEvent);
                    break;
                default:
                    logger.warn("Unknown event type \"{}\" in \n{}", wrappedEvent.event.type,
                                content);
            }

        } catch (final Exception e) {
            logger.warn(e, e);
        }
    }

    @GET
    @Path("authorize")
    public Response authorize(@QueryParam("code") final String code) {
        try {
            final var registrar = new Registrar();
            registrar.registerWorkspace(code);
        } catch (Exception e) {
            logger.error(e, e);
        }

        return Response.ok().build();
    }

}
