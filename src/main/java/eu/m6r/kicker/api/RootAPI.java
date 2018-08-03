package eu.m6r.kicker.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/")
public class RootAPI {

    @GET
    public Response redirectToFrontend() {
        return Response.status(Response.Status.TEMPORARY_REDIRECT)
                .header("Location", "/frontend/index.html")
                .build();
    }

    @GET
    @Path("{id}")
    public Response redirectToFrontend(@PathParam("id") final String id) {
        return Response.status(Response.Status.TEMPORARY_REDIRECT)
                .header("Location", "/frontend/index.html?id=" + id)
                .build();
    }
}
