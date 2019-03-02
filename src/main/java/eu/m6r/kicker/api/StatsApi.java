package eu.m6r.kicker.api;

import java.io.IOException;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.m6r.kicker.Controller;
import eu.m6r.kicker.Stats;
import eu.m6r.kicker.api.annotations.CheckChannelId;
import eu.m6r.kicker.models.PlayerSkill;
import eu.m6r.kicker.models.TeamStat;

@Path("api/stats/{channelId: [0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89AB][0-9a-f]{3}-[0-9a-f]{12}}")
@CheckChannelId
public class StatsApi {

    private final Logger logger;
    private final Stats stats;
    private final Controller controller;

    public StatsApi() throws IOException {
        this.logger = LogManager.getLogger();
        this.stats = new Stats();
        this.controller = Controller.getInstance();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<PlayerSkill> getStats(@PathParam("channelId") final String channelId) {
        return controller.playerSkills(channelId);
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
