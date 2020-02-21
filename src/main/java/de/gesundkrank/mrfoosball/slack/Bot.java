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

package de.gesundkrank.mrfoosball.slack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.gesundkrank.mrfoosball.Controller;
import de.gesundkrank.mrfoosball.models.Player;
import de.gesundkrank.mrfoosball.models.PlayerQueue;
import de.gesundkrank.mrfoosball.slack.models.EventWrapper;
import de.gesundkrank.mrfoosball.slack.models.Message;

public class Bot {

    private static final Pattern COMMAND_PATTERN = Pattern.compile("\\w+");
    private static final Pattern USER_PATTERN = Pattern.compile("<@([^>]*)>");

    private final Logger logger;
    private final Controller controller;


    public Bot() throws IOException {
        this.logger = LogManager.getLogger();

        this.controller = Controller.getInstance();
    }

    public void onAppMention(final EventWrapper wrappedEvent)
            throws IOException, Controller.SlackWorkspaceNotFoundException {
        final var text = wrappedEvent.event.text;
        final var sender = wrappedEvent.event.user;
        final var slackChannelId = wrappedEvent.event.channel;
        final var workspace = controller.getSlackWorkspace(wrappedEvent.teamId);
        final var accessToken = workspace.accessToken;

        final var botUserId = wrappedEvent.authedUsers.get(0);
        final var matcher = USER_PATTERN.matcher(text);

        if (!matcher.find()) {
            logger.warn("Couldn't remove bot user from message \"{}\"", text);
            return;
        }

        final var message = text.substring(matcher.end()).trim();
        final var commandMatcher = COMMAND_PATTERN.matcher(message);

        if (commandMatcher.find()) {
            final var action = commandMatcher.group();
            logger.info("Received command {}", action);
            final var userMatcher = USER_PATTERN.matcher(message);
            final String channelId;
            try {
                channelId = controller.getChannelId(slackChannelId);
            } catch (Controller.ChannelNotFoundException e) {
                logger.warn(e, e);
                sendMessage("I didn't know that I'm part of this channel. Please remove me "
                            + "from this channel and invite me again!",
                            slackChannelId, botUserId, accessToken);
                return;
            }

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
                                controller.addPlayer(channelId, userId);
                            } catch (PlayerQueue.PlayerAlreadyInQueueException e) {
                                sendMessage(e.getMessage(), slackChannelId, botUserId, accessToken);
                            } catch (final PlayerQueue.TooManyUsersException e) {
                                sendMessage(e.getMessage(), slackChannelId, botUserId, accessToken);
                                break;
                            }
                        }

                        final var playersInQueue = controller.getPlayersString(channelId);
                        if (!playersInQueue.isEmpty()) {
                            sendMessage(String.format("Current queue: %s", playersInQueue),
                                        slackChannelId, botUserId, accessToken);
                        }
                        break;
                    case "reset":
                        controller.resetPlayers(channelId);
                        sendMessage("Cleared queue.", slackChannelId, botUserId, accessToken);
                        break;
                    case "remove":
                        for (final var userId : userIds) {
                            controller.removePlayer(channelId, userId);
                            sendMessage(String.format("Removed <@%s> from the queue",
                                                      userId),
                                        slackChannelId, botUserId, accessToken);
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

                        sendMessage(queueMessage, slackChannelId, botUserId, accessToken);
                        break;
                    case "cancel":
                        if (controller.cancelRunningTournament(channelId)) {
                            sendMessage("Canceled the running match!", slackChannelId, botUserId,
                                        accessToken);
                        } else {
                            sendMessage("No match running!", slackChannelId, botUserId,
                                        accessToken);
                        }
                        break;
                    case "fixedMatch":
                        if (userIds.size() != 4) {
                            sendMessage("To start a game I need 4 players :(", slackChannelId,
                                        botUserId, accessToken);
                            break;
                        }

                        try {
                            final var players = userIds.stream().map(Player::new)
                                    .collect(Collectors.toList());
                            controller
                                    .startTournament(channelId, false, Controller.DEFAULT_BEST_OF_N,
                                                     players);
                        } catch (Controller.TournamentRunningException e) {
                            sendMessage(e.getMessage(), slackChannelId, botUserId, accessToken);
                        }

                        break;
                    case "url":
                        sendChannelUrlMessage(channelId, slackChannelId, sender, accessToken);
                        break;
                    case "help":
                        sendHelpMessage(slackChannelId, sender, botUserId, accessToken);
                        break;
                    default:
                        sendMessage(String.format("I'm sorry <@%s>, I didn't understand that. "
                                                  + "If you need help just ask for it.", sender),
                                    slackChannelId, botUserId, accessToken);
                }
            } catch (final UserFetcher.FetchUserFailedException e) {
                sendMessage(e.getMessage(), sender, botUserId, accessToken);
            }
        } else {
            sendMessage("That doesn't make any sense at all.", slackChannelId, botUserId,
                        accessToken);
        }
    }

    private Message.Attachment getQRCodeAttachment(final String channelId) {
        final var attachment = new Message.Attachment("Your Channel QR-Code",
                                                      "You can scan this code on "
                                                      + controller.getBaseUrl());
        attachment.imageUrl = controller.getChannelQRCodeUrl(channelId);
        return attachment;
    }

    private void sendChannelUrlMessage(final String channel, final String id, final String userId,
                                       final String accessToken) {
        final var messageWriter = new MessageWriter(accessToken);
        final var url = controller.getChannelUrl(channel);
        final var message = new Message(id, url, userId);
        message.attachments.add(getQRCodeAttachment(channel));
        messageWriter.postEphemeral(message);
    }

    public void onChannelJoined(final EventWrapper eventWrapper)
            throws Controller.SlackWorkspaceNotFoundException {
        final var teamId = eventWrapper.teamId;
        final var slackWorkspace = controller.getSlackWorkspace(teamId);
        final var botUserId = slackWorkspace.botUserId;
        final var accessToken = slackWorkspace.accessToken;
        final var joinedUserId = eventWrapper.event.user;

        if (botUserId.equals(joinedUserId)) {

            final var channelId = eventWrapper.event.channel;
            String newChannel;
            try {
                newChannel = controller.getChannelId(channelId);
            } catch (Controller.ChannelNotFoundException e) {
                newChannel = controller.joinChannel(channelId, slackWorkspace);
            }
            sendChannelJoinedMessage(channelId, newChannel, botUserId, accessToken);
            logger.info("Joined channel \"{}\" in workspace \"{}\"", channelId, teamId);
        }
    }

    private void sendMessage(final String text, final String channel, final String botUserId,
                             final String accessToken) {
        final var message = new Message(channel, text, botUserId);
        sendMessage(message, accessToken);
    }

    private void sendMessage(final Message message, final String accessToken) {
        final var messageWriter = new MessageWriter(accessToken);
        messageWriter.postMessage(message);
    }

    private void sendChannelJoinedMessage(final String slackId, final String id,
                                          final String botUserId, final String accessToken) {
        final var url = controller.getChannelUrl(id);
        final var messageText =
                String.format("Nice to meet you! I'm your new favourite kicker-bot. Go to %s to "
                              + "find your team stats and to enter your results.", url);
        final var message = new Message(slackId, messageText, botUserId);
        message.attachments.add(getQRCodeAttachment(slackId));
        final var messageWriter = new MessageWriter(accessToken);
        messageWriter.postMessage(message);
    }

    private void sendHelpMessage(final String channel, final String sender,
                                 final String botUserId, final String accessToken) {
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
        final var messageWriter = new MessageWriter(accessToken);
        messageWriter.postEphemeral(message);
    }
}
