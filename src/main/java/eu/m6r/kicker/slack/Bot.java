package eu.m6r.kicker.slack;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.m6r.kicker.Controller;
import eu.m6r.kicker.models.Player;
import eu.m6r.kicker.slack.models.Message;
import eu.m6r.kicker.slack.models.RtmInitResponse;
import eu.m6r.kicker.slack.models.SlackUser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientProperties;

import java.io.IOException;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

@ClientEndpoint
public class Bot {

    private final static Pattern COMMAND_PATTERN = Pattern.compile("(\\w+)( ?<@([^>]*)>)*");

    private final Logger logger;
    private final String token;
    private final ObjectMapper objectMapper;
    private final Controller controller;

    private Session socketSession;
    private String botUserId;
    private Pattern botUserIdPattern;

    public Bot(final String token) {
        this.logger = LogManager.getLogger();

        if (token == null) {
            logger.error("Slack token is not set.");
            throw new RuntimeException("Slack token cannot be null");
        }

        this.token = token;
        this.objectMapper = new ObjectMapper();
        this.controller = Controller.INSTANCE;
    }

    public void startNewSession() throws StartSocketSessionException {
        final Client client = ClientBuilder.newClient();
        client.property(ClientProperties.CONNECT_TIMEOUT, 1000);
        client.property(ClientProperties.READ_TIMEOUT, 1000);

        final WebTarget target = client.target("https://slack.com").path("/api/rtm.start")
                .queryParam("token", token);
        final RtmInitResponse response =
                target.request(MediaType.APPLICATION_JSON).get(RtmInitResponse.class);

        if (!response.ok) {
            throw new StartSocketSessionException(response.error);
        }

        if (response.warning != null) {
            logger.warn(response.warning);
        }

        final String botMention = String.format("<@%s>", response.self.id);
        this.botUserIdPattern = Pattern.compile(botMention);
        this.botUserId = response.self.id;

        final WebSocketContainer socketClient = ContainerProvider.getWebSocketContainer();
        try {
            logger.info("Starting new web socket session with {}", response.url);
            this.socketSession = socketClient.connectToServer(this, URI.create(response.url));
        } catch (final DeploymentException | IOException e) {
            throw new StartSocketSessionException(e);
        }
    }

    @OnOpen
    public void onOpen(final Session session) {
    }

    @OnMessage
    public void onMessage(final String messageString, final Session session) throws IOException {
        if (!messageString.contains("\"type\":\"message\"")) {
            return;
        }

        final Message message = objectMapper.readValue(messageString, Message.class);

        if (message.text == null) {
            return;
        }
        final Matcher matcher = botUserIdPattern.matcher(message.text);

        if (message.type != null && message.type.equals("message") && matcher.find()) {
            final String command = message.text.substring(matcher.end()).trim();
            onCommand(command, message.channel, message.user);
        }
    }

    @OnClose
    public void onClose(final Session session, final CloseReason closeReason) {
        logger.warn("Session closed: {}", closeReason);
        try {
            startNewSession();
        } catch (final StartSocketSessionException e) {
            logger.error("Reopening closed session failed.", e);
        }
    }

    private void onCommand(final String command, final String channel, final String sender) {
        final Matcher matcher = COMMAND_PATTERN.matcher(command);
        if (matcher.find()) {
            final String action = matcher.group(1);
            String userId = matcher.group(3);

            if (userId == null) {
                userId = sender;
            }

            try {
                switch (action) {
                    case "add":
                    case "play":
                        final Player player = getUser(userId);
                        final String message = controller.addPlayer(player);
                        sendMessage(message, channel);
                        break;
                    case "reset":
                        controller.resetPlayers();
                        sendMessage("Cleared queue.", channel);
                        break;
                    case "remove":
                        final Player playerToRemove = getUser(userId);
                        controller.removePlayer(playerToRemove);
                        sendMessage(String.format("Removed %s from the queue", playerToRemove.name),
                                    channel);
                        break;
                    case "queue":
                        sendMessage(controller.getListOfPlayers(), channel);
                    case "stats":
                        break;
                }
            } catch (final Controller.TooManyUsersException |
                    Controller.PlayerAlreadyInQueueException |
                    UserExtractionFailedException e) {
                sendMessage(e.getMessage(), channel);
            }
        } else {
            sendMessage("That doesn't make any sense at all.", channel);
        }
    }

    private void sendMessage(final String text, final String channel) {
        final Message message = new Message(channel, text, botUserId);
        try {
            final String messageText = objectMapper.writeValueAsString(message);
            socketSession.getAsyncRemote().sendText(messageText);
        } catch (JsonProcessingException e) {
            logger.error("Failed to process message json.", e);
        }
    }

    private Player getUser(final String userId) throws UserExtractionFailedException {
        final Client client = ClientBuilder.newClient();
        final WebTarget target = client.target("https://slack.com")
                .path("/api/users.info")
                .queryParam("token", token)
                .queryParam("user", userId);
        try {
            final String userString = target.request(MediaType.APPLICATION_JSON).get(String.class);
            final SlackUser slackUser = objectMapper.readValue(userString, SlackUser.class);

            final Player player = new Player();
            player.id = slackUser.user.id;
            player.name = slackUser.user.name;
            player.avatarImage = slackUser.user.profile.image_192;
            return player;
        } catch (final ResponseProcessingException | IOException e) {
            throw new UserExtractionFailedException(userId, e);
        }
    }

    public static class StartSocketSessionException extends Exception {

        StartSocketSessionException(final Throwable cause) {
            super("Failed to establish web socket connection.", cause);
        }

        StartSocketSessionException(final String error) {
            super(String.format("Failed to establish web socket connection. Cause: %s", error));
        }
    }

    public static class UserExtractionFailedException extends Exception {

        UserExtractionFailedException(final String userId, final Throwable cause) {
            super(String.format("Failed to get user informations for '%s' from slack API.", userId),
                  cause);
        }
    }

}
