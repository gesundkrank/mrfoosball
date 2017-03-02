package eu.m6r.kicker.api;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("tournament")
public class TournamentAPI {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getTournament() {
        return "{\n"
               + "  \"id\": 1,\n"
               + "  \"teamA\": {\n"
               + "    \"playerA\": \"Sulu\",\n"
               + "    \"playerB\": \"Chekov\"\n"
               + "  },\n"
               + "  \"teamB\": {\n"
               + "    \"playerA\": \"Sulu\",\n"
               + "    \"playerB\": \"Chekov\"\n"
               + "  },\n"
               + "  \"state\": \"RUNNING\",\n"
               + "  \"winningMatches\": 2,\n"
               + "  \"matches\": [\n"
               + "    {\n"
               + "      \"id\": 1,\n"
               + "      \"date\": \"2017-03-02T12:33:57.509Z\",\n"
               + "      \"teamA\": {\n"
               + "        \"score\": 6\n"
               + "      },\n"
               + "      \"teamB\": {\n"
               + "        \"score\": 0\n"
               + "      },\n"
               + "      \"state\": \"FINISHED\"\n"
               + "    },\n"
               + "    {\n"
               + "      \"id\": 2,\n"
               + "      \"date\": \"2017-03-02T12:43:57.509Z\",\n"
               + "      \"teamA\": {\n"
               + "        \"score\": 6\n"
               + "      },\n"
               + "      \"teamB\": {\n"
               + "        \"score\": 0\n"
               + "      },\n"
               + "      \"state\": \"RUNNING\"\n"
               + "    }\n"
               + "  ]\n"
               + "}";
    }

    @PUT
    public Response updateTournament() {
        return Response.accepted().build();
    }
}
