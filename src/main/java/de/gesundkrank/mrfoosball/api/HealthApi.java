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

package de.gesundkrank.mrfoosball.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import de.gesundkrank.mrfoosball.Controller;
import de.gesundkrank.mrfoosball.HealthChecker;
import de.gesundkrank.mrfoosball.models.HealthStatus;

@Path("api/health")
public class HealthApi {

    @GET
    public HealthStatus checkHealth() {
        if (HealthChecker.isHealthy()) {
            return new HealthStatus();
        }

        throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
}
