package eu.m6r.kicker.api;

import eu.m6r.kicker.Stats;
import eu.m6r.kicker.models.TeamStat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

@Path("api/stats")
public class StatsAPI {

    private final Logger logger = LogManager.getLogger();
    private final Stats stats = new Stats();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getStats() {
        return "[\n"
               + "  {\n"
               + "    \"rank\": 1,\n"
               + "    \"name\": \"Thomas\",\n"
               + "    \"scored\": 100,\n"
               + "    \"received\": 24,\n"
               + "    \"difference\": 76,\n"
               + "    \"tournaments\": 3,\n"
               + "    \"matches\": 34,\n"
               + "    \"kriechen\": 2\n"
               + "  },\n"
               + "  {\n"
               + "    \"rank\": 2,\n"
               + "    \"name\": \"Thomas\",\n"
               + "    \"scored\": 100,\n"
               + "    \"received\": 24,\n"
               + "    \"difference\": 76,\n"
               + "    \"tournaments\": 3,\n"
               + "    \"matches\": 34,\n"
               + "    \"kriechen\": 2\n"
               + "  },\n"
               + "  {\n"
               + "    \"rank\": 3,\n"
               + "    \"name\": \"Thomas\",\n"
               + "    \"scored\": 100,\n"
               + "    \"received\": 24,\n"
               + "    \"difference\": 76,\n"
               + "    \"tournaments\": 3,\n"
               + "    \"matches\": 34,\n"
               + "    \"kriechen\": 2\n"
               + "  }\n"
               + "]";
    }

    @GET
    @Path("teams")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TeamStat> getTeamStats() {

        try {
            return stats.calcTeamStats();
        } catch (Exception e) {
            logger.error("Failed to calculate team stats", e);
            throw new WebApplicationException(e.getMessage());
        }
    }

}
