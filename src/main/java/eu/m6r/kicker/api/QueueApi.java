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
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.m6r.kicker.controller.Controller;
import eu.m6r.kicker.api.annotations.CheckChannelId;
import eu.m6r.kicker.models.Player;
import eu.m6r.kicker.models.PlayerQueue;

@Path("/api/{channelId: [0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89AB][0-9a-f]{3}-[0-9a-f]{12}}/queue")
@CheckChannelId
public class QueueApi {

    private final Logger logger;

    @Inject
    private Controller controller;

    @PathParam("channelId")
    private String channelId;

    public QueueApi() {
        this.logger = LogManager.getLogger();
    }

    @GET
    public List<Player> getPlayersInQueue() {
        try {
            return controller.getPlayersInQueue(channelId);
        } catch (Exception e) {
            logger.error("Failed to get players in queue.", e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
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
    @Path("{id}")
    public void removePlayer(@PathParam("id") final String id) {
        try {
            controller.removePlayer(channelId, id);
        } catch (IOException e) {
            logger.error("Failed to remove player from the queue", e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

}
