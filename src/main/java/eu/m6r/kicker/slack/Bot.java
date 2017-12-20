package eu.m6r.kicker.slack;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.m6r.kicker.Controller;
import eu.m6r.kicker.models.Player;
import eu.m6r.kicker.slack.models.Message;
import eu.m6r.kicker.slack.models.PresenceChange;
import eu.m6r.kicker.slack.models.RtmInitResponse;
import eu.m6r.kicker.slack.models.SlackUser;
import eu.m6r.kicker.utils.ZookeeperClient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.glassfish.jersey.client.ClientProperties;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
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
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

@ClientEndpoint
public class Bot implements Watcher {

    private final static Pattern COMMAND_PATTERN = Pattern.compile("\\w+");
    private final static Pattern USER_PATTERN = Pattern.compile("<@([^>]*)>");
    private final static String ZOO_KEEPER_PARENT_PATH =
            ZookeeperClient.ZOOKEEPER_ROOT_PATH + "/bot";
    private final static String ZOO_KEEPER_PATH = ZOO_KEEPER_PARENT_PATH + "/slack_";
    private final static String BOT_ID = UUID.randomUUID().toString();

    private final Logger logger;
    private final String token;
    private final ObjectMapper objectMapper;
    private final Controller controller;
    private final Client client;
    private final Map<Player, Timer> awayTimers;
    private final long inactiveTimeoutMinutes;
    private final ZookeeperClient zookeeperClient;
    private final String lockNode;

    private Session socketSession;
    private String botUserId;
    private Pattern botUserIdPattern;

    public Bot(final String token, final long inactiveTimeoutMinutes,
               final ZookeeperClient zookeeperClient)
            throws KeeperException, InterruptedException, StartSocketSessionException {
        this.inactiveTimeoutMinutes = inactiveTimeoutMinutes;
        this.logger = LogManager.getLogger();

        if (token == null) {
            logger.error("Slack token is not set.");
            throw new RuntimeException("Slack token cannot be null");
        }

        this.client = ClientBuilder.newClient();
        client.property(ClientProperties.CONNECT_TIMEOUT, 1000);
        client.property(ClientProperties.READ_TIMEOUT, 1000);

        this.token = token;
        this.objectMapper = new ObjectMapper();
        this.controller = Controller.INSTANCE;
        this.awayTimers = new HashMap<>();
        this.zookeeperClient = zookeeperClient;

        logger.info("Creating lock for ID {}", BOT_ID);

        zookeeperClient.createPath(ZOO_KEEPER_PARENT_PATH);
        this.lockNode = zookeeperClient.createEphemeralSequential(ZOO_KEEPER_PATH, BOT_ID);

        if (zookeeperClient.checkLock(lockNode, this)) {
            startNewSession();
        }
    }

    public void startNewSession() throws StartSocketSessionException {
        final WebTarget target = client.target("https://slack.com").path("/api/rtm.connect")
                .queryParam("token", token);

        final RtmInitResponse response =
                target.request(MediaType.APPLICATION_JSON).get(RtmInitResponse.class);

        if (response == null) {
            throw new StartSocketSessionException("Failed to parse response object");
        }

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

    @Override
    public void process(WatchedEvent event) {
        try {
            if (this.socketSession == null && zookeeperClient.checkLock(lockNode, this)) {
                startNewSession();
            }
        } catch (KeeperException | InterruptedException | StartSocketSessionException e) {
            logger.error("Failed to process zookeeper event.", e);
        }
    }

    @OnOpen
    public void onOpen(final Session session) {
    }

    @OnMessage
    public void onMessage(final String messageString, final Session session) throws IOException {
        if (messageString.startsWith("{\"type\":\"message\"")) {
            final Message message = objectMapper.readValue(messageString, Message.class);

            if (message.text == null) {
                return;
            }
            final Matcher matcher = botUserIdPattern.matcher(message.text);

            if (message.type != null && message.type.equals("message") && matcher.find()) {
                final String command = message.text.substring(matcher.end()).trim();
                onCommand(command, message.channel, message.user);
            }
        } else if (messageString.startsWith("{\"type\":\"presence_change\"")) {
            final PresenceChange presenceChange =
                    objectMapper.readValue(messageString, PresenceChange.class);

            if (presenceChange != null) {
                onPresenceChange(presenceChange);
            }
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
        final Matcher commandMatcher = COMMAND_PATTERN.matcher(command);
        if (commandMatcher.find()) {
            final String action = commandMatcher.group();
            final Matcher userMatcher = USER_PATTERN.matcher(command);

            List<String> userIds = new ArrayList<>();

            while (userMatcher.find()) {
                userIds.add(userMatcher.group(1));
            }

            if (userIds.isEmpty()) {
                userIds.add(sender);
            }

            try {
                switch (action) {
                    case "add":
                    case "play":
                        for (final String userId : userIds) {
                            try {
                                final Player player = getUser(userId);
                                controller.addPlayer(player);
                            } catch (Controller.PlayerAlreadyInQueueException |
                                    Controller.TournamentRunningException e) {
                                sendMessage(e.getMessage(), channel);
                            }
                        }

                        final String message;
                        if (!controller.hasRunningTournament()) {
                            message = String.format("Current queue: %s",
                                                    controller.getPlayersString());
                        } else {
                            message = controller.newTournamentMessage();
                        }
                        sendMessage(message, channel);

                        break;
                    case "reset":
                        controller.resetPlayers();
                        sendMessage("Cleared queue.", channel);
                        break;
                    case "remove":
                        for (final String userId : userIds) {
                            final Player playerToRemove = getUser(userId);
                            controller.removePlayer(playerToRemove);
                            sendMessage(String.format("Removed <@%s> from the queue",
                                                      playerToRemove.name), channel);
                        }
                        break;
                    case "queue":
                        final String queueMessage;
                        if (controller.getPlayersInQueue().isEmpty()) {
                            queueMessage = "Queue is empty!";
                        } else {
                            queueMessage = "Current queue: " + controller.getPlayersString();
                        }

                        sendMessage(queueMessage, channel);
                        break;
                    case "cancel":
                        if (controller.cancelRunningTournament()) {
                            sendMessage("Canceled the running match!", channel);
                        } else {
                            sendMessage("No match running!", channel);
                        }
                        break;
                    case "fixedMatch":
                        if (userIds.size() != 4) {
                            sendMessage("To start a game I need 4 players :(", channel);
                        } else {
                            try {
                                controller.resetPlayers();
                                for (final String userId : userIds) {
                                    final Player player = getUser(userId);
                                    controller.addPlayer(player, false);
                                }

                                controller.startTournament(false, 3);
                                sendMessage(controller.newTournamentMessage(), channel);

                            } catch (Controller.PlayerAlreadyInQueueException |
                                    Controller.TournamentRunningException e) {
                                sendMessage(e.getMessage(), channel);
                            }

                        }
                        break;
                    case "help":
                        sendHelpMessage(channel, sender);
                        break;

                    case "calcSkills":
                        controller.recalculateSkills();
                        sendMessage("done", channel);
                        break;
                    default:
                        sendMessage(String.format("I'm sorry <@%s>, I didn't understand that. "
                                                  + "If you need help just ask for it.", sender),
                                    channel);
                }
            } catch (final Controller.TooManyUsersException |
                    UserExtractionFailedException e) {
                sendMessage(e.getMessage(), sender);
            }
        } else {
            sendMessage("That doesn't make any sense at all.", channel);
        }
    }

    private void onPresenceChange(final PresenceChange presenceChange) {
        try {
            switch (presenceChange.presence) {
                case "away":
                    playerAway(getUser(presenceChange.user));
                    break;
                case "active":
                    playerActive(getUser(presenceChange.user));
            }
        } catch (UserExtractionFailedException e) {
            logger.debug("unknown user {}", presenceChange.user);
        }
    }

    private void playerAway(final Player player) {
        if (!awayTimers.containsKey(player) && controller.playerInQueue(player)) {
            final Timer timer = new Timer();
            timer.schedule(new AwayTimerTask(player), inactiveTimeoutMinutes * 60000);
            awayTimers.put(player, timer);
        }
    }

    private void playerActive(final Player player) {
        if (awayTimers.containsKey(player)) {
            final Timer timer = awayTimers.get(player);
            timer.cancel();
            awayTimers.remove(player);
        }
    }

    private void sendHelpMessage(final String channel, final String sender) {
        final String text = "Supported slack commands:";
        final Message message = new Message(channel, text, botUserId);
        message.user = sender;
        message.as_user = true;

        final Message.Attachment addCommand =
                new Message.Attachment("add", "_Adds new player(s) to the queue._");
        final List<Message.Attachment.Field> addFields = new ArrayList<>();
        addFields.add(new Message.Attachment.Field("Add yourself",
                                                   String.format("<@%s> add", botUserId)));
        addFields.add(new Message.Attachment.Field(
                "Add others",
                String.format("<@%s> add <@U12G6EUSZ> <@U12RUGB7E>", botUserId)));
        addCommand.fields = addFields;

        message.attachments.add(addCommand);

        final Message.Attachment removeCommand =
                new Message.Attachment("remove", "_Removes player(s) from the queue._");
        final List<Message.Attachment.Field> removeFields = new ArrayList<>();
        removeFields.add(new Message.Attachment.Field("Remove yourself",
                                                      String.format("<@%s> remove", botUserId)));
        removeFields.add(new Message.Attachment.Field(
                "Remove others",
                String.format("<@%s> remove <@U12GTAA49> <@U5GEP6RMM>", botUserId)));
        removeCommand.fields = removeFields;

        message.attachments.add(removeCommand);

        final Message.Attachment queueCommand =
                new Message.Attachment("queue", "_Shows the current queue._");
        final List<Message.Attachment.Field> queueFields = new ArrayList<>();
        queueFields.add(new Message.Attachment.Field(String.format("<@%s> queue", botUserId)));
        queueCommand.fields = queueFields;

        message.attachments.add(queueCommand);

        final Message.Attachment fixedMatchCommand =
                new Message.Attachment("fixedMatch",
                                       "_Creates a new match. Keeps the order of the players. "
                                       + "First and last two players will play together._");
        final List<Message.Attachment.Field> fixedMatchFields = new ArrayList<>();
        fixedMatchFields.add(new Message.Attachment.Field(
                String.format("<@%s> fixedMatch <@U6WRKPL6P> <@U12G6EUSZ> <@U3ZCMB9SR> "
                              + "<@U2D3PT6JK>", botUserId)));
        fixedMatchCommand.fields = fixedMatchFields;

        message.attachments.add(fixedMatchCommand);

        final Message.Attachment resetCommand =
                new Message.Attachment("reset", "_Reset the queue._");
        final List<Message.Attachment.Field> resetFields = new ArrayList<>();
        resetFields.add(new Message.Attachment.Field(String.format("<@%s> reset", botUserId)));
        resetCommand.fields = resetFields;

        message.attachments.add(resetCommand);

        final Message.Attachment cancelCommand =
                new Message.Attachment("cancel", "_Cancel a running match._");
        final List<Message.Attachment.Field> cancelFields = new ArrayList<>();
        cancelFields.add(new Message.Attachment.Field(String.format("<@%s> cancel", botUserId)));
        cancelCommand.fields = cancelFields;

        message.attachments.add(cancelCommand);
        postEphemeral(message);
    }

    private void postEphemeral(final Message message) {
        client.target("https://slack.com")
                .path("/api/chat.postEphemeral")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Bearer " + token).post(Entity.json(message));
    }

    private void sendMessage(final String text, final String channel) {
        final Message message = new Message(channel, text, botUserId);
        sendMessage(message);
    }

    private void sendMessage(final Message message) {
        try {
            final String messageText = objectMapper.writeValueAsString(message);
            socketSession.getAsyncRemote().sendText(messageText);
        } catch (JsonProcessingException e) {
            logger.error("Failed to process message json.", e);
        }
    }

    private Player getUser(final String userId) throws UserExtractionFailedException {
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

    private class AwayTimerTask extends TimerTask {

        private final Player player;


        AwayTimerTask(final Player player) {
            this.player = player;
        }

        @Override
        public void run() {
            if (controller.playerInQueue(player)) {
                controller.removePlayer(player);

            }
        }
    }


}
