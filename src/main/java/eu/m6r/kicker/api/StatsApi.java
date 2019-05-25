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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.m6r.kicker.controller.Controller;
import eu.m6r.kicker.Stats;
import eu.m6r.kicker.api.annotations.CheckChannelId;
import eu.m6r.kicker.models.PlayerSkill;
import eu.m6r.kicker.models.TeamStat;

@Path("api/{channelId: [0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89AB][0-9a-f]{3}-[0-9a-f]{12}}/stats")
@CheckChannelId
public class StatsApi {

    @Inject
    private Controller controller;

    private final Logger logger;
    private final Stats stats;

    public StatsApi() throws IOException {
        this.logger = LogManager.getLogger();
        this.stats = new Stats();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<PlayerSkill> getStats(@PathParam("channelId") final String channelId) {
        try {
            return controller.playerSkills(channelId);
        } catch (Exception e) {
            logger.error("Failed to get skills", e);
            throw new WebApplicationException(e.getMessage());
        }
    }

    @GET
    @Path("teams")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TeamStat> getTeamStats(@PathParam("channelId") final String channelId) {

        try {
            return stats.calcTeamStats(channelId);
        } catch (Exception e) {
            logger.error("Failed to calculate team stats", e);
            throw new WebApplicationException(e.getMessage());
        }
    }

}
