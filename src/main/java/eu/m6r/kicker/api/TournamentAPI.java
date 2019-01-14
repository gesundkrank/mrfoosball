package eu.m6r.kicker.api;

import eu.m6r.kicker.Controller;
import eu.m6r.kicker.api.annotations.CheckChannelId;
import eu.m6r.kicker.models.Player;
import eu.m6r.kicker.models.Tournament;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/tournament/{channelId: [0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89AB][0-9a-f]{3}-[0-9a-f]{12}}")
@CheckChannelId
public class TournamentAPI {

    private final Logger logger;
    private final Controller controller;

    @PathParam("channelId")
    private String channelId;

    public TournamentAPI() throws IOException {
        this.logger = LogManager.getLogger();
        this.controller = Controller.getInstance();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Tournament> getTournament(@QueryParam("num") final Integer num) {
        if (num == null) {
            return controller.getTournaments(channelId);
        }

        return controller.getTournaments(channelId, num);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateTournament(final Tournament tournament) {
        try {
            controller.updateTournament(channelId, tournament);
        } catch (IOException | Controller.TournamentNotRunningException e) {
            logger.error("Failed to update tournament", e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @DELETE
    public void cancelTournament() {
        try {
            controller.cancelRunningTournament(channelId);
        } catch (IOException e) {
            logger.info("Tried to cancel non running match.");
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void newTournament(@FormParam("playerA1") final String playerA1,
                              @FormParam("playerA2") final String playerA2,
                              @FormParam("playerB1") final String playerB1,
                              @FormParam("playerB2") final String playerB2,
                              @FormParam("bestOfN") final int bestOfN) {

        try {
            controller.resetPlayers(channelId);
            controller.addPlayer(channelId, playerA1);
            controller.addPlayer(channelId, playerA2);
            controller.addPlayer(channelId, playerB1);
            controller.addPlayer(channelId, playerB2);

            controller.startTournament(channelId, false, bestOfN);
        } catch (Exception e) {
            logger.error(e);
            throw new WebApplicationException(500);
        }
    }

    @GET
    @Path("running")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRunningTournament() {
        try {
            if (controller.hasRunningTournament(channelId)) {
                return Response.ok(controller.getRunningTournament(channelId)).build();
            }
        } catch (IOException e) {
            logger.error("Failed to get running tournament.", e);
        } catch (Controller.TournamentNotRunningException e) {
            logger.debug("No tournament running.");
        }

        return Response.noContent().build();
    }

    @POST
    @Path("match")
    public void newMatch() {
        try {
            controller.newMatch(channelId);
        } catch (Controller.InvalidTournamentStateException |
                Controller.TournamentNotRunningException | IOException e) {
            logger.error("Failed to create new match!", e);
            throw new WebApplicationException(e.getMessage(), Response.Status.BAD_REQUEST);
        }
    }

    @POST
    @Path("finish")
    public void finishTournament() {
        try {
            controller.finishTournament(channelId);
        } catch (Controller.InvalidTournamentStateException |
                Controller.TournamentNotRunningException | IOException e) {
            logger.error("Failed to finish tournament!", e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("queue")
    public List<Player> getPlayersInQueue() {
        try {
            return controller.getPlayersInQueue(channelId);
        } catch (IOException e) {
            logger.error("Failed to get players in queue.", e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
