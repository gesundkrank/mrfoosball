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

@Path("api/channel")
public class ChannelApi {

    private final Logger logger;
    private final QRCodeGenerator qrCodeGenerator;


    public ChannelApi() {
        this.logger = LogManager.getLogger();
        this.qrCodeGenerator = new QRCodeGenerator();
    }


    @GET
    @Path("{channelId: [0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89AB][0-9a-f]{3}-[0-9a-f]{12}}/qrcode")
    @Produces("image/png")
    @CheckChannelId

    public byte[] getQRCode(@PathParam("channelId") final String channelId) {
        try {
            return qrCodeGenerator.generateChannelId(channelId);
        } catch (IOException | WriterException e) {
            logger.error(String.format("Failed to generate QrCode for channel %s", channelId), e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
