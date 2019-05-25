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
import javax.validation.constraints.NotNull;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.m6r.kicker.RunningTournaments;
import eu.m6r.kicker.controller.Controller;

@Path("/api/{channelId: [0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89AB][0-9a-f]{3}-[0-9a-f]{12}}/tournament/match")
public class MatchApi {

    private final Logger logger;

    @Inject
    private Controller controller;

    @PathParam("channelId")
    private String channelId;

    public MatchApi() {
        this.logger = LogManager.getLogger();
    }

    @POST
    public void newMatch() {
        try {
            controller.newMatch(channelId);
        } catch (Controller.InvalidTournamentStateException
                | RunningTournaments.TournamentNotRunningException e) {
            logger.error("Failed to create new match!", e);
            throw new WebApplicationException(e.getMessage(), Response.Status.BAD_REQUEST);
        } catch (final IOException e) {
            logger.error("Failed to create new match!", e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Path("score")
    public Response score(@Context final Request request,
                      @QueryParam("team") @NotNull final Teams team) {
        try {
            final var runningTournament = controller.getRunningTournament(channelId);
            final var eTag = new EntityTag(Integer.toString(runningTournament.hashCode()));
            final var responseBuilder = request.evaluatePreconditions(eTag);

            if (responseBuilder != null) {
                return responseBuilder.build();
            }

            switch (team) {
                case A:
                    controller.scoreTeamA(channelId, runningTournament);
                    break;
                case B:
//                    controller.scoreTeamB(channelId, runningTournament);
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (RunningTournaments.TournamentNotRunningException e) {
            e.printStackTrace();
        }

    }

    public enum Teams {
        A, B;
    }

}
