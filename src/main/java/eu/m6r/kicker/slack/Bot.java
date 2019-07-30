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

package eu.m6r.kicker.slack;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.glassfish.jersey.client.ClientProperties;

import eu.m6r.kicker.Controller;
import eu.m6r.kicker.models.Player;
import eu.m6r.kicker.models.PlayerQueue;
import eu.m6r.kicker.slack.models.ChannelJoined;
import eu.m6r.kicker.slack.models.Message;
import eu.m6r.kicker.slack.models.RtmInitResponse;
import eu.m6r.kicker.slack.models.SlackUser;
import eu.m6r.kicker.utils.JsonConverter;
import eu.m6r.kicker.utils.ZookeeperClient;

@ClientEndpoint
public class Bot implements Watcher {

    private static final Pattern COMMAND_PATTERN = Pattern.compile("\\w+");
    private static final Pattern USER_PATTERN = Pattern.compile("<@([^>]*)>");
    private static final String ZOO_KEEPER_PARENT_PATH =
            ZookeeperClient.ZOOKEEPER_ROOT_PATH + "/bot";
    private static final String ZOO_KEEPER_PATH = ZOO_KEEPER_PARENT_PATH + "/slack_";
    private static final String BOT_ID = UUID.randomUUID().toString();

    private final Logger logger;
    private final String token;
    private final JsonConverter jsonConverter;
    private final Controller controller;
    private final Client client;
    private final ZookeeperClient zookeeperClient;
    private final String lockNode;
    private final MessageWriter messageWriter;

    private Session socketSession;
    private String botUserId;
    private Pattern botUserIdPattern;

    public Bot(final String token, final ZookeeperClient zookeeperClient)
            throws KeeperException, InterruptedException, StartSocketSessionException, IOException {
        this.logger = LogManager.getLogger();

        if (token == null) {
            logger.error("Slack token is not set.");
            throw new RuntimeException("Slack token cannot be null");
        }

        this.client = ClientBuilder.newClient();
        client.property(ClientProperties.CONNECT_TIMEOUT, 30000);
        client.property(ClientProperties.READ_TIMEOUT, 30000);

        this.token = token;
        this.controller = Controller.getInstance();
        this.zookeeperClient = zookeeperClient;
        this.messageWriter = new MessageWriter(token);

        this.jsonConverter = new JsonConverter(Message.class, ChannelJoined.class,
                                               SlackUser.class);

        zookeeperClient.createPath(ZOO_KEEPER_PARENT_PATH);
        this.lockNode = zookeeperClient.createEphemeralSequential(ZOO_KEEPER_PATH, BOT_ID);

        logger.info("Creating lock {} for ID {}", lockNode, BOT_ID);

        if (zookeeperClient.checkLock(lockNode, this)) {
            logger.info("Obtained lock {} and starting new slack session.", lockNode);
            startNewSession();
        }
    }

    private void startNewSession() throws StartSocketSessionException {
        final var target = client.target("https://slack.com").path("/api/rtm.connect")
                .queryParam("token", token);

        final var response = target.request(MediaType.APPLICATION_JSON).get(RtmInitResponse.class);

        if (response == null) {
            throw new StartSocketSessionException("Failed to parse response object");
        }

        if (!response.ok) {
            throw new StartSocketSessionException(response.error);
        }

        if (response.warning != null) {
            logger.warn(response.warning);
        }

        final var botMention = String.format("<@%s>", response.self.id);
        this.botUserIdPattern = Pattern.compile(botMention);
        this.botUserId = response.self.id;

        final var socketClient = ContainerProvider.getWebSocketContainer();
        try {
            logger.info("Starting new web socket session with {}", response.url);
            this.socketSession = socketClient.connectToServer(this, URI.create(response.url));
        } catch (final DeploymentException | IOException e) {
            throw new StartSocketSessionException(e);
        }
    }

    @Override
    public void process(WatchedEvent event) {
        logger.info("Zookeeper event: {}", event);

        try {
            if (this.socketSession == null && zookeeperClient.checkLock(lockNode, this)) {
                logger.info("Obtained lock {} and starting new slack session.", lockNode);
                startNewSession();
            }
        } catch (KeeperException | InterruptedException | StartSocketSessionException e) {
            logger.error("Failed to process zookeeper event.", e);
        }
    }

    @OnMessage
    public void onMessage(final String messageString, final Session session) throws IOException {
        logger.info(messageString);

        if (messageString.contains("\"type\":\"channel_joined\"") ||
            messageString.contains("\"type\":\"group_joined\"")) {

            final var channelJoined =
                    jsonConverter.fromString(messageString, ChannelJoined.class);

            final var id = controller
                    .joinChannel(channelJoined.channel.id, channelJoined.channel.name);
            sendChannelJoinedMessage(channelJoined.channel.id, id);


        } else if (messageString.contains("\"type\":\"message\"")) {
            final var message = jsonConverter.fromString(messageString, Message.class);

            if (message.text == null) {
                return;
            }
            final var matcher = botUserIdPattern.matcher(message.text);

            if (message.type != null && message.type.equals("message") && matcher.find()) {
                final var command = message.text.substring(matcher.end()).trim();
                onCommand(command, message.channel, message.user);
            }
        }
    }

    @OnClose
    public void onClose(final Session session, final CloseReason closeReason) {
        logger.error("Session closed: {}", closeReason);
        System.exit(1);
    }

    @OnError
    public void onError(final Session session, final Throwable throwable) {
        logger.error("Websocket error.", throwable);
        System.exit(1);
    }

    private void onCommand(final String command, final String slackChannelId, final String sender)
            throws IOException {
        final var commandMatcher = COMMAND_PATTERN.matcher(command);
        if (commandMatcher.find()) {
            final var action = commandMatcher.group();
            final var userMatcher = USER_PATTERN.matcher(command);
            final var channelId = controller.getChannelId(slackChannelId);

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
                                final var player = getUser(userId);
                                controller.addPlayer(channelId, player);
                            } catch (PlayerQueue.PlayerAlreadyInQueueException e) {
                                sendMessage(e.getMessage(), slackChannelId);
                            } catch (final PlayerQueue.TooManyUsersException e) {
                                sendMessage(e.getMessage(), slackChannelId);
                                break;
                            }
                        }

                        final var playersInQueue = controller.getPlayersString(channelId);
                        if (!playersInQueue.isEmpty()) {
                            sendMessage(String.format("Current queue: %s", playersInQueue),
                                        slackChannelId);
                        }
                        break;
                    case "reset":
                        controller.resetPlayers(channelId);
                        sendMessage("Cleared queue.", slackChannelId);
                        break;
                    case "remove":
                        for (final var userId : userIds) {
                            final var playerToRemove = getUser(userId);
                            controller.removePlayer(channelId, playerToRemove);
                            sendMessage(String.format("Removed <@%s> from the queue",
                                                      playerToRemove.id), slackChannelId);
                        }
                        break;
                    case "queue":
                        final String queueMessage;
                        if (controller.getPlayersInQueue(channelId).isEmpty()) {
                            queueMessage = "Queue is empty!";
                        } else {
                            queueMessage = "Current queue: "
                                           + controller.getPlayersString(channelId);
                        }

                        sendMessage(queueMessage, slackChannelId);
                        break;
                    case "cancel":
                        if (controller.cancelRunningTournament(channelId)) {
                            sendMessage("Canceled the running match!", slackChannelId);
                        } else {
                            sendMessage("No match running!", slackChannelId);
                        }
                        break;
                    case "fixedMatch":
                        if (userIds.size() != 4) {
                            sendMessage("To start a game I need 4 players :(", slackChannelId);
                        } else {
                            try {
                                controller.resetPlayers(channelId);
                                for (final var userId : userIds) {
                                    final var player = getUser(userId);
                                    controller.addPlayer(channelId, player);
                                }

                                controller.startTournament(channelId, false, 3);
                            } catch (PlayerQueue.PlayerAlreadyInQueueException
                                    | Controller.TournamentRunningException e) {
                                sendMessage(e.getMessage(), slackChannelId);
                            }

                        }
                        break;
                    case "url":
                        sendChannelUrlMessage(channelId, slackChannelId, sender);
                        break;
                    case "help":
                        sendHelpMessage(slackChannelId, sender);
                        break;
                    default:
                        sendMessage(String.format("I'm sorry <@%s>, I didn't understand that. "
                                                  + "If you need help just ask for it.", sender),
                                    slackChannelId);
                }
            } catch (final PlayerQueue.TooManyUsersException
                    | UserExtractionFailedException e) {
                sendMessage(e.getMessage(), sender);
            }
        } else {
            sendMessage("That doesn't make any sense at all.", slackChannelId);
        }
    }

    private void sendHelpMessage(final String channel, final String sender) {
        final var text = "Supported slack commands:";
        final var message = new Message(channel, text, sender);

        final var addCommand = new Message.Attachment("add", "_Adds new player(s) to the queue._");
        final List<Message.Attachment.Field> addFields = new ArrayList<>();
        addFields.add(new Message.Attachment.Field("Add yourself",
                                                   String.format("<@%s> add", botUserId)));
        addFields.add(new Message.Attachment.Field(
                "Add others",
                String.format("<@%s> add <@U12G6EUSZ> <@U12RUGB7E>", botUserId)));
        addCommand.fields = addFields;

        message.attachments.add(addCommand);

        final var removeCommand =
                new Message.Attachment("remove", "_Removes player(s) from the queue._");
        final List<Message.Attachment.Field> removeFields = new ArrayList<>();
        removeFields.add(new Message.Attachment.Field("Remove yourself",
                                                      String.format("<@%s> remove", botUserId)));
        removeFields.add(new Message.Attachment.Field(
                "Remove others",
                String.format("<@%s> remove <@U12GTAA49> <@U5GEP6RMM>", botUserId)));
        removeCommand.fields = removeFields;

        message.attachments.add(removeCommand);

        final var queueCommand = new Message.Attachment("queue", "_Shows the current queue._");
        final List<Message.Attachment.Field> queueFields = new ArrayList<>();
        queueFields.add(new Message.Attachment.Field(String.format("<@%s> queue", botUserId)));
        queueCommand.fields = queueFields;

        message.attachments.add(queueCommand);

        final var fixedMatchCommand =
                new Message.Attachment("fixedMatch",
                                       "_Creates a new match. Keeps the order of the players. "
                                       + "First and last two players will play together._");
        final List<Message.Attachment.Field> fixedMatchFields = new ArrayList<>();
        fixedMatchFields.add(new Message.Attachment.Field(
                String.format("<@%s> fixedMatch <@U6WRKPL6P> <@U12G6EUSZ> <@U3ZCMB9SR> "
                              + "<@U2D3PT6JK>", botUserId)));
        fixedMatchCommand.fields = fixedMatchFields;

        message.attachments.add(fixedMatchCommand);

        final var resetCommand = new Message.Attachment("reset", "_Reset the queue._");
        final List<Message.Attachment.Field> resetFields = new ArrayList<>();
        resetFields.add(new Message.Attachment.Field(String.format("<@%s> reset", botUserId)));
        resetCommand.fields = resetFields;

        message.attachments.add(resetCommand);

        final var cancelCommand = new Message.Attachment("cancel", "_Cancel a running match._");
        final List<Message.Attachment.Field> cancelFields = new ArrayList<>();
        cancelFields.add(new Message.Attachment.Field(String.format("<@%s> cancel", botUserId)));
        cancelCommand.fields = cancelFields;

        message.attachments.add(cancelCommand);
        messageWriter.postEphemeral(message);
    }

    private Message.Attachment getQRCodeAttachment(final String channelId) {
        final var attachment = new Message.Attachment("Your Channel QR-Code",
                                                      "You can scan this code from on "
                                                      + controller.getBaseUrl());
        attachment.imageUrl = controller.getChannelQRCodeUrl(channelId);
        return attachment;
    }

    private void sendMessage(final String text, final String channel) {
        final var message = new Message(channel, text, botUserId);
        sendMessage(message);
    }

    private void sendMessage(final Message message) {
        try {
            socketSession.getAsyncRemote().sendText(jsonConverter.toString(message));
        } catch (final IOException e) {
            logger.error("Failed to process message json.", e);
        }
    }

    private Player getUser(final String userId) throws UserExtractionFailedException {
        final var target = client.target("https://slack.com")
                .path("/api/users.info")
                .queryParam("token", token)
                .queryParam("user", userId);
        try {
            final var userString = target.request(MediaType.APPLICATION_JSON).get(String.class);
            final var slackUser = jsonConverter.fromString(userString, SlackUser.class);

            final var player = new Player();
            player.id = slackUser.user.id;
            player.name = slackUser.user.name;
            player.avatarImage = slackUser.user.profile.image192;
            return player;
        } catch (final ResponseProcessingException | IOException e) {
            throw new UserExtractionFailedException(userId, e);
        }
    }

    private void sendChannelJoinedMessage(final String channel, final String id) {
        final var url = controller.getChannelUrl(id);
        final var messageText =
                String.format("Nice to meet you! I'm your new favourite kicker-bot. Go to %s to "
                              + "find your team stats and to enter your results.", url);
        final var message = new Message(channel, messageText, botUserId);
        message.attachments.add(getQRCodeAttachment(channel));
        messageWriter.postMessage(message);
    }

    private void sendChannelUrlMessage(final String channel, final String id, final String userId) {
        final var url = controller.getChannelUrl(channel);
        final var message = new Message(id, url, userId);
        message.attachments.add(getQRCodeAttachment(channel));
        messageWriter.postEphemeral(message);
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
