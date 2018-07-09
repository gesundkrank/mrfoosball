package eu.m6r.kicker.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import eu.m6r.kicker.models.HealthStatus;

@Path("api/health")
public class HealthAPI {

    @GET
    public HealthStatus checkHealth() {
        return new HealthStatus();
    }
}
