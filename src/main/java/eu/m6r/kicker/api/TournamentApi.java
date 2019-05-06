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
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.m6r.kicker.Controller;
import eu.m6r.kicker.api.annotations.CheckChannelId;
import eu.m6r.kicker.models.Player;
import eu.m6r.kicker.models.PlayerQueue;
import eu.m6r.kicker.models.Tournament;

@Path("/api/tournament/{channelId: [0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89AB][0-9a-f]{3}-[0-9a-f]{12}}")
@CheckChannelId
public class TournamentApi {

    private final Logger logger;
    private final Controller controller;

    @PathParam("channelId")
    private String channelId;

    public TournamentApi() throws IOException {
        this.logger = LogManager.getLogger();
        this.controller = Controller.getInstance();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Tournament> getTournament(@QueryParam("num") final Integer num) {
        if (num == null) {
            return controller.getTournaments(channelId);
        }

        return controller.getTournaments(channelId, num);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateTournament(final Tournament tournament) {
        try {
            controller.updateTournament(channelId, tournament);
        } catch (IOException | Controller.TournamentNotRunningException e) {
            logger.error("Failed to update tournament", e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @DELETE
    public void cancelTournament() {
        try {
            controller.cancelRunningTournament(channelId);
        } catch (IOException e) {
            logger.info("Tried to cancel non running match.");
        }
    }

    @GET
    @Path("running")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRunningTournament() {
        try {
            if (controller.hasRunningTournament(channelId)) {
                return Response.ok(controller.getRunningTournament(channelId)).build();
            }
        } catch (IOException e) {
            logger.error("Failed to get running tournament.", e);
        } catch (Controller.TournamentNotRunningException e) {
            logger.debug("No tournament running.");
        }

        return Response.noContent().build();
    }

    @POST
    @Path("match")
    public void newMatch() {
        try {
            controller.newMatch(channelId);
        } catch (Controller.InvalidTournamentStateException
                | Controller.TournamentNotRunningException | IOException e) {
            logger.error("Failed to create new match!", e);
            throw new WebApplicationException(e.getMessage(), Response.Status.BAD_REQUEST);
        }
    }

    @POST
    @Path("finish")
    public void finishTournament(
            @QueryParam("rematch") @DefaultValue("true") final boolean rematch) {
        try {
            controller.finishTournament(channelId, rematch);
        } catch (Controller.InvalidTournamentStateException
                | Controller.TournamentNotRunningException | IOException e) {
            logger.error("Failed to finish tournament!", e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("queue")
    public List<Player> getPlayersInQueue() {
        try {
            return controller.getPlayersInQueue(channelId);
        } catch (Exception e) {
            logger.error("Failed to get players in queue.", e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Path("queue")
    @Consumes(MediaType.APPLICATION_JSON)
    public void addPlayer(final Player player) {
        try {
            controller.addPlayer(channelId, player);
        } catch (PlayerQueue.PlayerAlreadyInQueueException | PlayerQueue.TooManyUsersException e) {
            logger.error("Failed to add player to the queue", e);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        } catch (IOException e) {
            logger.error("Failed to add player to the queue", e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @DELETE
    @Path("queue/{id}")
    public void removePlayer(@PathParam("id") final String id) {
        try {
            controller.removePlayer(channelId, id);
        } catch (IOException e) {
            logger.error("Failed to remove player from the queue", e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
