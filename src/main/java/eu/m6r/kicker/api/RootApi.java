package eu.m6r.kicker.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import eu.m6r.kicker.utils.Properties;

@Path("/")
public class RootApi {

    private final String baseUrl = Properties.getInstance().getAppUrl();

    @GET
    public Response redirectToFrontend() {
        return Response.status(Response.Status.TEMPORARY_REDIRECT)
                .header("Location", baseUrl + "/frontend/index.html")
                .build();
    }

    @GET
    @Path("{id}")
    public Response redirectToFrontend(@PathParam("id") final String id) {
        return Response.status(Response.Status.TEMPORARY_REDIRECT)
                .header("Location", baseUrl + "/frontend/index.html?id=" + id)
                .build();
    }
}
