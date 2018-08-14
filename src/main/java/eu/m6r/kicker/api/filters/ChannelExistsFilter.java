package eu.m6r.kicker.api.filters;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import eu.m6r.kicker.Store;
import eu.m6r.kicker.api.annotations.CheckChannelId;

@Provider
@CheckChannelId
public class ChannelExistsFilter implements ContainerRequestFilter {

    private static final Pattern UUID_REGEX = Pattern
            .compile("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89AB][0-9a-f]{3}-[0-9a-f]{12}");

    @Override
    public void filter(ContainerRequestContext requestContext) {
        final String path = requestContext.getUriInfo().getPath();
        final Matcher matcher = UUID_REGEX.matcher(path);
        if (matcher.find()) {
            try (final Store store = new Store()) {
                final String channelId = matcher.group();
                if (!store.channelExists(channelId)) {
                    final Response notFoundResponse = Response
                            .status(Response.Status.NOT_FOUND)
                            .entity(String.format("{ \"error\": \"Channel %s does not exist.\"}",
                                                  channelId))
                            .type(MediaType.APPLICATION_JSON_TYPE)
                            .build();
                    requestContext.abortWith(notFoundResponse);
                }
            }
        }
    }
}
