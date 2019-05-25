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

import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.google.zxing.WriterException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.m6r.kicker.api.annotations.CheckChannelId;
import eu.m6r.kicker.utils.QRCodeGenerator;

@Path("api/{channelId: [0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89AB][0-9a-f]{3}-[0-9a-f]{12}}")
@CheckChannelId
public class ChannelApi {

    private final Logger logger;

    @PathParam("channelId")
    private String channelId;

    public ChannelApi() {
        this.logger = LogManager.getLogger();
    }


    @GET
    @Path("qrcode")
    @Produces("image/png")
    public byte[] getQRCode() {
        try {
            final QRCodeGenerator qrCodeGenerator = new QRCodeGenerator();
            return qrCodeGenerator.generateChannelId(channelId);
        } catch (IOException | WriterException e) {
            logger.error(String.format("Failed to generate QrCode for channel %s", channelId), e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
