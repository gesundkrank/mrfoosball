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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import javax.inject.Singleton;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.message.internal.ReaderWriter;

import de.gesundkrank.mrfoosball.api.annotations.VerifySlackRequest;
import de.gesundkrank.mrfoosball.slack.RequestVerifier;
import de.gesundkrank.mrfoosball.utils.Properties;

@Singleton
@Provider
@VerifySlackRequest
public class SlackVerificationFilter implements ContainerRequestFilter {


    private final Logger logger;
    private final RequestVerifier requestVerifier;

    public SlackVerificationFilter() throws GeneralSecurityException {
        this.logger = LogManager.getLogger();
        this.requestVerifier =
                new RequestVerifier(Properties.getInstance().getSlackSigningSecret());
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        final var requestBody = getEntityBody(requestContext);
        final var timestampHeader = requestContext.getHeaderString("X-Slack-Request-Timestamp");
        final var headerSignature = requestContext.getHeaderString("X-Slack-Signature");
        if (!requestVerifier.verify(headerSignature, requestBody, timestampHeader)) {
            final var response = Response.status(Response.Status.BAD_REQUEST).build();
            requestContext.abortWith(response);
            logger.info("Request Aborted. Details:timestamp: \"{}\"body:\"{}\"signature:\"{}\"",
                        timestampHeader, requestBody, headerSignature);
            return;
        }

        logger.debug("Successfully verified request.");
    }

    private String getEntityBody(ContainerRequestContext requestContext) throws IOException {
        final var out = new ByteArrayOutputStream();
        final var in = requestContext.getEntityStream();

        ReaderWriter.writeTo(in, out);

        final var requestEntity = out.toByteArray();
        requestContext.setEntityStream(new ByteArrayInputStream(requestEntity));
        return new String(requestEntity, StandardCharsets.UTF_8);
    }
}
