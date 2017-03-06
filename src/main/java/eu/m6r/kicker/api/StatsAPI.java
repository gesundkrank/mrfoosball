package eu.m6r.kicker.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("stats")
public class StatsAPI {

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
               + "    \"kriechen\": 2,\n"
               + "  },\n"
               + "  {\n"
               + "    \"rank\": 2,\n"
               + "    \"name\": \"Thomas\",\n"
               + "    \"scored\": 100,\n"
               + "    \"received\": 24,\n"
               + "    \"difference\": 76,\n"
               + "    \"tournaments\": 3,\n"
               + "    \"matches\": 34,\n"
               + "    \"kriechen\": 2,\n"
               + "  },\n"
               + "  {\n"
               + "    \"rank\": 3,\n"
               + "    \"name\": \"Thomas\",\n"
               + "    \"scored\": 100,\n"
               + "    \"received\": 24,\n"
               + "    \"difference\": 76,\n"
               + "    \"tournaments\": 3,\n"
               + "    \"matches\": 34,\n"
               + "    \"kriechen\": 2,\n"
               + "  }\n"
               + "]";
    }

}
