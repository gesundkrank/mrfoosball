package eu.m6r.kicker.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/")
public class RootAPI {

    @GET
    public Response redirectToFrontend() {
        return Response.status(Response.Status.MOVED_PERMANENTLY)
                .header("Location", "/frontend/index.html")
                .build();
    }
}
