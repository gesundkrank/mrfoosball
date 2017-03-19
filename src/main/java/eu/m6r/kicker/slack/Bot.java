package eu.m6r.kicker.slack;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.m6r.kicker.Controller;
import eu.m6r.kicker.models.User;
import eu.m6r.kicker.slack.models.Message;
import eu.m6r.kicker.slack.models.RtmInitResponse;
import eu.m6r.kicker.slack.models.SlackUser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import javax.xml.bind.JAXBException;

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

    public Bot(final String token) throws IOException, JAXBException {
        this.logger = LogManager.getLogger();
        this.token = token;
        this.objectMapper = new ObjectMapper();
        this.controller = Controller.INSTANCE;
        openSession();
    }

    private void openSession() throws IOException {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("https://slack.com").path("/api/rtm.start")
                .queryParam("token", token);
        final RtmInitResponse response =
                target.request(MediaType.APPLICATION_JSON).get(RtmInitResponse.class);

        if (!response.ok) {
            throw new RuntimeException(response.error);
        }

        if (response.warning != null) {
            logger.warn(response.warning);
        }

        String botMention = String.format("<@%s>", response.self.id);
        this.botUserIdPattern = Pattern.compile(botMention);
        this.botUserId = response.self.id;

        WebSocketContainer socketClient = ContainerProvider.getWebSocketContainer();
        try {
            this.socketSession = socketClient.connectToServer(this, URI.create(response.url));
        } catch (DeploymentException e) {
            throw new IOException(e);
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

        Message message = objectMapper.readValue(messageString, Message.class);

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
    public void onClose(Session session, CloseReason closeReason) {
        logger.warn("Session closed: {}", closeReason);
        try {
            openSession();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onCommand(String command, String channel, String sender) {
        Matcher matcher = COMMAND_PATTERN.matcher(command);
        if (matcher.find()) {
            String action = matcher.group(1);
            String userId = matcher.group(3);

            if (userId == null) {
                userId = sender;
            }

            try {
                switch (action) {
                    case "add":
                    case "play":
                        User user = getUser(userId);
                        String message = controller.addPlayer(user);
                        sendMessage(message, channel);
                        break;
                    case "reset":
                        controller.resetPlayers();
                        sendMessage("Cleared queue.", channel);
                        break;
                    case "remove":
                        User userToRemove = getUser(userId);
                        controller.removePlayer(userToRemove);
                        sendMessage(String.format("Removed %s from the queue", userToRemove.name),
                                    channel);
                        break;
                    case "queue":
                        sendMessage(controller.getListOfPlayers(), channel);
                    case "stats":
                        break;
                }
            } catch (Controller.TooManyUsersException |
                    Controller.PlayerAlreadyInQueueException |
                    UserExtractionFailedException e) {
                sendMessage(e.getMessage(), channel);
            }
        } else {
            sendMessage("That doesn't make any sense at all.", channel);
        }
    }

    private void sendMessage(String text, String channel) {
        Message message = new Message(channel, text, botUserId);
        try {
            String messageText = objectMapper.writeValueAsString(message);
            socketSession.getAsyncRemote().sendText(messageText);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private User getUser(String userId) throws UserExtractionFailedException {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("https://slack.com")
                .path("/api/users.info")
                .queryParam("token", token)
                .queryParam("user", userId);
        try {
            String userString = target.request(MediaType.APPLICATION_JSON).get(String.class);
            SlackUser slackUser = objectMapper.readValue(userString, SlackUser.class);

            User user = new User();
            user.id = slackUser.user.id;
            user.name = slackUser.user.name;
            user.avatarImage = slackUser.user.profile.image_192;
            return user;
        } catch (ResponseProcessingException | IOException e) {
            throw new UserExtractionFailedException(userId, e);
        }
    }

    public static class UserExtractionFailedException extends Exception {

        UserExtractionFailedException(String userId, Throwable cause) {
            super(String.format("Failed to get user informations for '%s' from slack API.", userId),
                  cause);
        }
    }

}
