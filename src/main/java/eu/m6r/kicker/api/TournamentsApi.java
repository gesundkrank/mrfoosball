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

import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.m6r.kicker.controller.Controller;
import eu.m6r.kicker.api.annotations.CheckChannelId;
import eu.m6r.kicker.models.Tournament;

@Path("/api/{channelId}/tournaments")
@CheckChannelId
public class TournamentsApi {

    private final Logger logger;

    @Inject
    private Controller controller;

    @PathParam("channelId")
    private String channelId;

    public TournamentsApi() {
        this.logger = LogManager.getLogger();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Tournament> getTournament(@QueryParam("num") final Integer num) {
        if (num == null) {
            return controller.getTournaments(channelId);
        }

        return controller.getTournaments(channelId, num);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void newTournament(@FormParam("playerA1") final String playerA1,
                              @FormParam("playerA2") final String playerA2,
                              @FormParam("playerB1") final String playerB1,
                              @FormParam("playerB2") final String playerB2,
                              @FormParam("bestOfN") final int bestOfN) {
        try {
            controller.startTournament(channelId, bestOfN, playerA1, playerA2, playerB1, playerB2);
        } catch (final Exception e) {
            logger.error(e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
