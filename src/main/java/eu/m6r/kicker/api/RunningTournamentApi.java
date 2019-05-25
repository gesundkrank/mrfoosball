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

package eu.m6r.kicker.api;

import java.io.IOException;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.m6r.kicker.RunningTournaments;
import eu.m6r.kicker.api.annotations.CheckChannelId;
import eu.m6r.kicker.controller.Controller;

@Path("/api/{channelId}/tournaments/running")
@CheckChannelId
public class RunningTournamentApi {

    private final Logger logger;

    @Inject
    private Controller controller;

    @PathParam("channelId")
    private String channelId;

    public RunningTournamentApi() {
        this.logger = LogManager.getLogger();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRunningTournament(@Context final Request request) {
        try {
            if (controller.hasRunningTournament(channelId)) {
                final var runningTournament = controller.getRunningTournament(channelId);
                final var entityTag = new EntityTag(Integer.toString(runningTournament.hashCode()));

                final var responseBuilder = request.evaluatePreconditions(entityTag);

                // in case client is not up-to-date send latest version.
                if (responseBuilder == null) {
                    return Response.ok(runningTournament).tag(entityTag).build();
                }

                return responseBuilder.build();
            }
        } catch (IOException e) {
            logger.error("Failed to get running tournament.", e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        } catch (RunningTournaments.TournamentNotRunningException e) {
            logger.debug("No tournament running.");
        }
        return Response.noContent().build();
    }

    @DELETE
    public void cancelTournament() {
        try {
            controller.cancelRunningTournament(channelId);
        } catch (IOException e) {
            logger.info("Tried to cancel non running match.");
        }
    }

    @POST
    @Path("finish")
    public Response finishTournament(
            @Context final Request request,
            @QueryParam("startNext") @DefaultValue("true") final boolean startNext) {
        try {
            final var eTag = new EntityTag(Integer.toString(
                    controller.getRunningTournament(channelId).hashCode()));
            final var responseBuilder = request.evaluatePreconditions(eTag);

            if (responseBuilder != null) {
                return responseBuilder.build();
            }

            controller.finishTournament(channelId, startNext);

            return Response.noContent().build();
        } catch (Controller.InvalidTournamentStateException
                | RunningTournaments.TournamentNotRunningException | IOException e) {
            logger.error("Failed to finish tournament!", e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

}
