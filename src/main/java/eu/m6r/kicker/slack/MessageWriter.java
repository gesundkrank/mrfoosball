package eu.m6r.kicker.slack;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientProperties;

import eu.m6r.kicker.slack.models.ApiResponse;
import eu.m6r.kicker.slack.models.Message;

public class MessageWriter {

    private final Logger logger;
    private final String token;
    private final Client client;

    public MessageWriter(final String token) {
        this.logger = LogManager.getLogger();
        this.token = token;
        this.client = ClientBuilder.newClient();
        client.property(ClientProperties.CONNECT_TIMEOUT, 30000);
        client.property(ClientProperties.READ_TIMEOUT, 30000);
    }

    public void postEphemeral(final Message message) {
        sendMessageToApi(message, "chat.postEphemeral");
    }

    public void postMessage(final Message message) {
        sendMessageToApi(message, "chat.postMessage");
    }

    private void sendMessageToApi(final Message message, final String method) {
        final ApiResponse apiResponse = client.target("https://slack.com")
                .path("/api/" + method)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Bearer " + token).post(Entity.json(message))
                .readEntity(ApiResponse.class);
        if (!apiResponse.ok) {
            logger.error("Sending message {} failed. Error: {}", message, apiResponse.error);
        }
    }
}
