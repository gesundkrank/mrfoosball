/*
 * This file is part of MrFoosball (https://github.com/gesundkrank/mrfoosball).
 * Copyright (c) 2020 Jan Graßegger.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.gesundkrank.mrfoosball.api.filters;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import de.gesundkrank.mrfoosball.api.annotations.CheckChannelId;
import de.gesundkrank.mrfoosball.store.hibernate.Store;

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
