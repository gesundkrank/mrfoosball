/*
 * This file is part of kicker (https://github.com/mbrtargeting/kicker).
 * Copyright (c) 2019 Jan Graßegger.
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
