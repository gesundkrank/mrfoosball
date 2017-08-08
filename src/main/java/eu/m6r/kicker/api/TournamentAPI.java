package eu.m6r.kicker.api;

import eu.m6r.kicker.Controller;
import eu.m6r.kicker.models.Player;
import eu.m6r.kicker.models.Tournament;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/tournament")
public class TournamentAPI {

    private final Logger logger = LogManager.getLogger();
    private final Controller controller = Controller.INSTANCE;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Tournament> getTournament() {
        return controller.getTournaments();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateTournament(final Tournament tournament) {
        controller.updateTournament(tournament);
    }

    @DELETE
    public void cancelTournament() {
        controller.cancelRunningTournament();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void newTournament(@FormParam("playerA1") final String playerA1,
                              @FormParam("playerA2") final String playerA2,
                              @FormParam("playerB1") final String playerB1,
                              @FormParam("playerB2") final String playerB2,
                              @FormParam("bestOfN") final int bestOfN) {

        try {
            controller.resetPlayers();
            controller.addPlayer(playerA1);
            controller.addPlayer(playerA2);
            controller.addPlayer(playerB1);
            controller.addPlayer(playerB2);

            controller.startTournament(false, bestOfN);
        } catch (Exception e) {
            logger.error(e);
            throw new WebApplicationException(500);
        }
    }

    @GET
    @Path("running")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRunningTournament() {
        if (controller.hasRunningTournament()) {
            return Response.ok(controller.getRunningTournament()).build();
        }

        return Response.noContent().build();
    }

    @POST
    @Path("match")
    public void newMatch() {
        try {
            controller.newMatch();
        } catch (Controller.InvalidTournamentStateException |
                Controller.TournamentNotRunningException e) {
            throw new WebApplicationException(e.getMessage(), Response.Status.BAD_REQUEST);
        }
    }

    @POST
    @Path("finish")
    public void finishTournament() {
        try {
            controller.finishTournament();
        } catch (Controller.InvalidTournamentStateException e) {
            throw new WebApplicationException(e.getMessage(), Response.Status.BAD_REQUEST);
        }
    }

    @GET
    @Path("queue")
    public List<Player> getPlayersInQueue() {
        return controller.getPlayersInQueue();
    }
}
