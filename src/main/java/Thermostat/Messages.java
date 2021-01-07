package thermostat;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.exceptions.PermissionException;
import thermostat.preparedStatements.ErrorEmbeds;

import java.io.InputStream;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Deals with different InsufficientPermissionExceptions
 * thrown by the JDA library, along with ErrorResponses.
 * Controls timed message deletion to prevent spam.
 */
public final class Messages {
    /**
     * Sends an embed to a designated channel, runs
     * a success Consumer afterwards.
     *
     * @param channel Channel to send the embed in.
     * @param eb      The embed to send.
     * @param success The consumer to run after the .queue() call.
     */
    public static void sendMessage(TextChannel channel, EmbedBuilder eb, Consumer<Message> success) {
        Consumer<Throwable> throwableConsumer = throwable -> {
            if (throwable.toString().contains("MISSING_ACCESS")) {
                Messages.sendMessage(channel, ErrorEmbeds.errPermission(EnumSet.of(Permission.MESSAGE_READ)));
            }
        };

        if (channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE)) {
            // send message and run provided consumer
            try {
                channel.sendMessage(eb.build()).queue(success, throwableConsumer);
            } catch (InsufficientPermissionException ex) {
                sendMessage(channel, "Please add the \"**Embed Links**\" permission to the bot to see command results!");
            }
        }
    }

    /**
     * Sends an embed to a designated channel.
     *
     * @param channel Channel to send the embed in.
     * @param eb      The embed to send.
     */
    public static void sendMessage(TextChannel channel, EmbedBuilder eb) {
        Consumer<Throwable> throwableConsumer = throwable -> {
            if (throwable.toString().contains("MISSING_ACCESS")) {
                Messages.sendMessage(channel, ErrorEmbeds.errPermission(EnumSet.of(Permission.MESSAGE_READ)));
            }
        };

        if (channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE)) {
            try {
                channel.sendMessage(eb.build()).queue(null, throwableConsumer);
            } catch (InsufficientPermissionException ex) {
                sendMessage(channel, "Please add the \"**Embed Links**\" permission to the bot!");
            }
        }
    }

    /**
     * Sends a text message to a designated channel.
     *
     * @param channel Channel to send the message in.
     * @param msg     The message to send.
     */
    public static void sendMessage(TextChannel channel, String msg) {
        Consumer<Throwable> throwableConsumer = throwable -> {
            if (throwable.toString().contains("MISSING_ACCESS")) {
                Messages.sendMessage(channel, ErrorEmbeds.errPermission(EnumSet.of(Permission.MESSAGE_READ)));
            }
        };

        if (channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE)) {
            try {
                channel.sendMessage(msg).queue(null, throwableConsumer);
            } catch (InsufficientPermissionException ignored) {
            }
        }
    }

    /**
     * Sends a text message to a designated channel.
     *
     * @param channel     Channel to send the message in.
     * @param inputStream Chart to get sent.
     * @param embed       Embed that contains the chart.
     */
    public static void sendMessage(TextChannel channel, InputStream inputStream, EmbedBuilder embed) {
        Consumer<Throwable> throwableConsumer = throwable -> {
            if (throwable.toString().contains("MISSING_ACCESS")) {
                Messages.sendMessage(channel, ErrorEmbeds.errPermission(EnumSet.of(Permission.MESSAGE_READ)));
            }
        };

        if (
                channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE) &&
                        channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_ATTACH_FILES)
        ) {
            try {
                channel.sendFile(
                        inputStream,
                        "chart.png"
                )
                        .embed(embed.setImage("attachment://chart.png").build())
                        .queue(null, throwableConsumer);
            } catch (InsufficientPermissionException ignored) {
            }
        } else {
            Messages.sendMessage(channel, ErrorEmbeds.errPermission(EnumSet.of(Permission.MESSAGE_READ, Permission.MESSAGE_ATTACH_FILES)));
        }
    }

    /**
     * Edits this Message's content to the provided MessageEmbed.
     *
     * @param channel    TextChannel the message resides on.
     * @param msgId      Message to edit.
     * @param newContent New embed to place in the message.
     */
    public static void editMessage(TextChannel channel, String msgId, MessageEmbed newContent) throws InsufficientPermissionException {
        Consumer<Throwable> retrieveMessageConsumer = throwable -> {
            if (throwable.toString().contains("MISSING_ACCESS")) {
                Messages.sendMessage(channel, ErrorEmbeds.errPermission(EnumSet.of(Permission.MESSAGE_READ)));
            } else if (throwable.toString().contains("MISSING_PERMISSIONS")) {
                Messages.sendMessage(channel, ErrorEmbeds.errPermission(EnumSet.of(Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY)));
            }
        };

        Consumer<Message> onSuccessfulRetrieval = message -> message.editMessage(newContent).queue();

        try {
            channel.retrieveMessageById(msgId).queue(onSuccessfulRetrieval, retrieveMessageConsumer);
        } catch (InsufficientPermissionException ex) {
            Messages.sendMessage(channel, ErrorEmbeds.errPermission(EnumSet.of(Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY)));
        }
    }

    /**
     * Deletes a message from Discord.
     *
     * @param msgId ID of message to delete.
     */
    public static void deleteMessage(TextChannel channel, String msgId) {
        Consumer<Throwable> throwableRetrieveConsumer = throwable -> {
            if (throwable.toString().contains("MISSING_ACCESS")) {
                Messages.sendMessage(channel, ErrorEmbeds.errPermission(EnumSet.of(Permission.MESSAGE_READ)));
            } else if (throwable.toString().contains("MISSING_PERMISSIONS")) {
                Messages.sendMessage(channel, ErrorEmbeds.errPermission(EnumSet.of(Permission.MESSAGE_HISTORY)));
            }
        };

        Consumer<Throwable> throwableDeleteConsumer = throwable -> {
            if (throwable.toString().contains("MISSING_ACCESS")) {
                Messages.sendMessage(channel, ErrorEmbeds.errPermission(EnumSet.of(Permission.MESSAGE_READ)));
            } else if (throwable.toString().contains("MISSING_PERMISSIONS")) {
                Messages.sendMessage(channel, ErrorEmbeds.errPermission(EnumSet.of(Permission.MESSAGE_MANAGE)));
            }
        };

        Consumer<Message> onSuccessfulRetrieval = message -> {
            try {
                message.delete().queueAfter(500, TimeUnit.MILLISECONDS, null, throwableDeleteConsumer);
            } catch (InsufficientPermissionException ex) {
                Messages.sendMessage(channel, ErrorEmbeds.errPermission(EnumSet.of(Permission.MESSAGE_MANAGE)));
            }
        };

        try {
            channel.retrieveMessageById(msgId).queue(onSuccessfulRetrieval, throwableRetrieveConsumer);
        } catch (InsufficientPermissionException ex) {
            Messages.sendMessage(channel, ErrorEmbeds.errPermission(EnumSet.of(Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY)));
        }
    }

    /**
     * Deletes a message from Discord.
     *
     * @param msg Message to delete.
     */
    public static void deleteMessage(Message msg) {
        Consumer<Throwable> throwableConsumer = throwable -> {
            if (throwable.toString().contains("MISSING_ACCESS")) {
                Messages.sendMessage(msg.getTextChannel(), ErrorEmbeds.errPermission(EnumSet.of(Permission.MESSAGE_READ)));
            } else if (throwable.toString().contains("MISSING_PERMISSIONS")) {
                Messages.sendMessage(msg.getTextChannel(), ErrorEmbeds.errPermission(EnumSet.of(Permission.MESSAGE_MANAGE)));
            }
        };

        try {
            msg.delete().queue(null, throwableConsumer);
        } catch (InsufficientPermissionException ex) {
            Messages.sendMessage(msg.getTextChannel(), ErrorEmbeds.errPermission(EnumSet.of(Permission.MESSAGE_MANAGE)));
        }
    }

    /**
     * Adds a list of reactions to a given message.
     *
     * @param channel Channel where the target message resides in.
     * @param msgId   The id of the target message.
     * @param unicode The unicode emoji to add as a reaction.
     */
    public static void addReactions(TextChannel channel, String msgId, List<String> unicode) {
        Consumer<Throwable> throwableRetrievalConsumer = throwable -> {
            if (throwable.toString().contains("MISSING_ACCESS")) {
                Messages.sendMessage(channel, ErrorEmbeds.errPermission(EnumSet.of(Permission.MESSAGE_READ)));
            } else if (throwable.toString().contains("MISSING_PERMISSIONS")) {
                Messages.sendMessage(channel, ErrorEmbeds.errPermission(EnumSet.of(Permission.MESSAGE_HISTORY)));
            }
        };

        Consumer<Throwable> throwableReactionConsumer = throwable -> {
            if (throwable.toString().contains("Missing Permissions") || throwable.toString().contains("MESSAGE_HISTORY")) {
                Messages.sendMessage(channel, ErrorEmbeds.errPermission(EnumSet.of(Permission.MESSAGE_ADD_REACTION)));
            }
        };

        Consumer<Message> onSuccessfulRetrieval = message -> {
            try {
                long reactionsDuration = 750;
                for (String it : unicode) {
                    message.addReaction(it).queueAfter(reactionsDuration, TimeUnit.MILLISECONDS, null, throwableReactionConsumer);
                    reactionsDuration += 500;
                }
            } catch (InsufficientPermissionException ex) {
                if (ex.toString().contains("MESSAGE_ADD_REACTION")) {
                    Messages.sendMessage(message.getTextChannel(), ErrorEmbeds.errPermission(EnumSet.of(Permission.MESSAGE_ADD_REACTION)));
                } else if (ex.toString().contains("MESSAGE_HISTORY")) {
                    Messages.sendMessage(message.getTextChannel(), ErrorEmbeds.errPermission(EnumSet.of(Permission.MESSAGE_HISTORY)));
                }
                Messages.deleteMessage(message);
                throw new PermissionException("Missing Permissions to add Reaction.");
            } catch (ErrorResponseException ignored) {
            }
        };

        try {
            channel.retrieveMessageById(msgId).queue(onSuccessfulRetrieval, throwableRetrievalConsumer);
        } catch (InsufficientPermissionException ex) {
            Messages.sendMessage(channel, ErrorEmbeds.errPermission(EnumSet.of(Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY)));
        }
    }

    /**
     * Adds a list of reactions to a given message.
     *
     * @param message The message which will have reactions added to it.
     * @param unicode The unicode emoji to add as a reaction.
     */
    public static void addReactions(Message message, List<String> unicode) throws InsufficientPermissionException {
        Consumer<Throwable> throwableReactionConsumer = throwable -> {
            if (throwable.toString().contains("Missing Permissions") || throwable.toString().contains("MESSAGE_HISTORY")) {
                Messages.sendMessage(message.getTextChannel(), ErrorEmbeds.errPermission(EnumSet.of(Permission.MESSAGE_ADD_REACTION)));
            }
        };

        long reactionsDuration = 750;
        for (String it : unicode) {
            message.addReaction(it).queueAfter(reactionsDuration, TimeUnit.MILLISECONDS, null, throwableReactionConsumer);
            reactionsDuration += 500;
        }
    }

    /**
     * Adds a reaction to a given message.
     *
     * @param msg     The target message.
     * @param unicode The unicode emoji to add as a reaction.
     */
    public static void addReaction(Message msg, String unicode) throws InsufficientPermissionException {
        Consumer<Throwable> throwableConsumer = throwable -> {
            if (throwable.toString().contains("Missing Permissions") || throwable.toString().contains("MESSAGE_HISTORY")) {
                Messages.deleteMessage(msg);
                Messages.sendMessage(msg.getTextChannel(), ErrorEmbeds.errPermission(EnumSet.of(Permission.MESSAGE_ADD_REACTION)));
            }
        };

        msg.addReaction(unicode).queue(null, throwableConsumer);
    }

    /**
     * Clears all reactions from a target message.
     *
     * @param channel Channel that the message resides in.
     * @param msgId   The ID of message to have its' reactions cleared.
     */
    public static void clearReactions(TextChannel channel, String msgId) throws InsufficientPermissionException {
        Consumer<Throwable> throwableClearConsumer = throwable -> {
            if (throwable.toString().contains("MISSING_PERMISSIONS")) {
                Messages.sendMessage(channel, ErrorEmbeds.errPermission(EnumSet.of(Permission.MESSAGE_MANAGE)));
            }
        };

        Consumer<Throwable> throwableRetrievalConsumer = throwable -> {
            if (throwable.toString().contains("MISSING_ACCESS")) {
                Messages.sendMessage(channel, ErrorEmbeds.errPermission(EnumSet.of(Permission.MESSAGE_READ)));
            } else if (throwable.toString().contains("MISSING_PERMISSIONS")) {
                Messages.sendMessage(channel, ErrorEmbeds.errPermission(EnumSet.of(Permission.MESSAGE_HISTORY)));
            }
        };

        Consumer<Message> onSuccessfulRetrieval = message -> {
            try {
                message.clearReactions().queue(null, throwableClearConsumer);
            } catch (InsufficientPermissionException ex) {
                Messages.sendMessage(channel, ErrorEmbeds.errPermission(EnumSet.of(Permission.MESSAGE_MANAGE)));
                Messages.deleteMessage(channel, msgId);
            } catch (ErrorResponseException ignored) {
            }
        };

        channel.retrieveMessageById(msgId).queue(onSuccessfulRetrieval, throwableRetrievalConsumer);
    }
}