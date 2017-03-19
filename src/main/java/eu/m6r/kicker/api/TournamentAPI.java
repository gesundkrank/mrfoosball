package eu.m6r.kicker.api;

import eu.m6r.kicker.Controller;
import eu.m6r.kicker.models.Tournament;
import eu.m6r.kicker.models.Player;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/tournament")
public class TournamentAPI {

    private final Controller controller = Controller.INSTANCE;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTournament() {
        if (controller.hasRunningTournament()) {
            return Response.ok(controller.getRunningTournament()).build();
        }

        return Response.noContent().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateTournament(final Tournament tournament) {
        controller.updateTournament(tournament);
    }

    @POST
    @Path("{tournamentId}")
    public void newMatch(@PathParam("tournamentId") final int tournamentId) {
        controller.newMatch(tournamentId);
    }

    @GET
    @Path("queue")
    public List<Player> getPlayersInQueue() {
        return controller.getPlayersInQueue();
    }
}
