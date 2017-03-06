package eu.m6r.kicker.api;

import eu.m6r.kicker.Controller;
import eu.m6r.kicker.models.Tournament;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/api/tournament")
public class TournamentAPI {

    private final Controller controller = Controller.INSTANCE;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Tournament getTournament() {
        return controller.getTournaments().get(0);
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
}
