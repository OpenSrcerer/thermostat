package Thermostat.ThermoFunctions;

import Thermostat.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.exceptions.PermissionException;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Deals with  different InsufficientPermissionExceptions
 * thrown by the JDA library, along with ErrorResponses.
 * Also controls timed message deletion to prevent spam.
 * Consider this class a "wrapper" for JDA Message Functions.
 */
public class Messages {
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
                Messages.sendMessage(channel, Embeds.simpleInsufficientPerm("MESSAGE_READ"));
            }
        };

        if (channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE)) {
            // send message and run custom provided consumer
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
                Messages.sendMessage(channel, Embeds.simpleInsufficientPerm("MESSAGE_READ"));
            }
        };

        if (channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE)) {
            // send message and delete after 100 seconds
            try {
                Consumer<Message> consumer = message -> Messages.deleteMessage(message, 100, TimeUnit.SECONDS);
                channel.sendMessage(eb.build()).queue(consumer, throwableConsumer);
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
                Messages.sendMessage(channel, Embeds.simpleInsufficientPerm("MESSAGE_READ"));
            }
        };

        if (channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE)) {
            try {
                Consumer<Message> consumer = message -> Messages.deleteMessage(message, 100, TimeUnit.SECONDS);
                channel.sendMessage(msg).queue(consumer, throwableConsumer);
            } catch (InsufficientPermissionException ignored) {
            }
        }
    }


    /**
     * Edits this Message's content to the provided MessageEmbed.
     *
     * @param msg        Message to edit.
     * @param newContent New embed to place in the message.
     */
    public static void editMessage(Message msg, MessageEmbed newContent) {
        Consumer<Throwable> throwableConsumer = throwable -> {
            if (throwable.toString().contains("UNKNOWN_MESSAGE")) {
                // tba
            }
        };

        msg.editMessage(newContent).queue(null, throwableConsumer);
    }

    /**
     * Deletes a message from Discord.
     *
     * @param msg Message to delete.
     */
    public static void deleteMessage(Message msg) {
        Consumer<Throwable> throwableConsumer = throwable -> {
            if (throwable.toString().contains("MISSING_ACCESS")) {
                Messages.sendMessage(msg.getTextChannel(), Embeds.simpleInsufficientPerm("MESSAGE_READ"));
            } else if (throwable.toString().contains("MISSING_PERMISSIONS")) {
                Messages.sendMessage(msg.getTextChannel(), Embeds.simpleInsufficientPerm("MANAGE_MESSAGES"));
            }
        };

        try {
            msg.delete().queue(null, throwableConsumer);
        } catch (InsufficientPermissionException ex) {
            Messages.sendMessage(msg.getTextChannel(), Embeds.simpleInsufficientPerm("MANAGE_MESSAGES"));
        }
    }

    /**
     * Deletes a message from discord after an amount of time.
     *
     * @param msg      Message to delete.
     * @param delay    Numerical waiting value.
     * @param timeUnit The unit of time to wait.
     * @return A ScheduledFuture representing the delayed operation.
     */
    public static ScheduledFuture<?> deleteMessage(Message msg, long delay, TimeUnit timeUnit) {
        Consumer<Throwable> throwableConsumer = throwable -> {
            if (throwable.toString().contains("MISSING_ACCESS")) {
                Messages.sendMessage(msg.getTextChannel(), Embeds.simpleInsufficientPerm("MESSAGE_READ"));
            } else if (throwable.toString().contains("MISSING_PERMISSIONS")) {
                Messages.sendMessage(msg.getTextChannel(), Embeds.simpleInsufficientPerm("MANAGE_MESSAGES"));
            }
        };

        ScheduledFuture<?> msgFuture = null;
        try {
            msgFuture = msg.delete().queueAfter(delay, timeUnit, null, throwableConsumer);
        } catch (InsufficientPermissionException ex) {
            Messages.sendMessage(msg.getTextChannel(), Embeds.simpleInsufficientPerm("MANAGE_MESSAGES"));
        }
        return msgFuture;
    }

    /**
     * Adds a reaction to a given message.
     *
     * @param msg     The target message.
     * @param unicode The unicode emoji to add as a reaction.
     */
    public static void addReaction(Message msg, String unicode) {
        Consumer<Throwable> throwableConsumer = throwable -> {
            if (throwable.toString().contains("Missing Permissions") || throwable.toString().contains("MESSAGE_HISTORY")) {
                Messages.sendMessage(msg.getTextChannel(), Embeds.simpleInsufficientPerm("MESSAGE_READ or READ_MESSAGE_HISTORY"));
            }
            throw new PermissionException("Missing Permissions to add Reaction.");
        };

        try {
            msg.addReaction(unicode).queue(null, null);
        } catch (InsufficientPermissionException ex) {
            if (ex.toString().contains("MESSAGE_ADD_REACTION")) {
                Messages.sendMessage(msg.getTextChannel(), Embeds.simpleInsufficientPerm("ADD_REACTIONS"));
            } else if (ex.toString().contains("MESSAGE_HISTORY")) {
                Messages.sendMessage(msg.getTextChannel(), Embeds.simpleInsufficientPerm("READ_MESSAGE_HISTORY"));
            }
            Messages.deleteMessage(msg);
            throw new PermissionException("Missing Permissions to add Reaction.");
        } catch (ErrorResponseException ignored) {
        }
    }

    /**
     * Adds a reaction to a given message after
     * a given amount of time.
     *
     * @param msg      The target message.
     * @param unicode  The unicode emoji to add as a reaction.
     * @param delay    Numerical waiting value.
     * @param timeUnit The unit of time to wait.
     */
    public static void addReaction(Message msg, String unicode, long delay, TimeUnit timeUnit) {
        Consumer<Throwable> throwableConsumer = throwable -> {
            if (throwable.toString().contains("MESSAGE_READ") || throwable.toString().contains("MESSAGE_HISTORY")) {
                Messages.sendMessage(msg.getTextChannel(), Embeds.simpleInsufficientPerm("MESSAGE_READ or READ_MESSAGE_HISTORY"));
            }
            throw new PermissionException("Missing Permissions to add Reaction.");
        };

        try {
            msg.addReaction(unicode).queueAfter(delay, timeUnit, null, throwableConsumer);
        } catch (InsufficientPermissionException ex) {
            if (ex.toString().contains("MESSAGE_ADD_REACTION")) {
                Messages.sendMessage(msg.getTextChannel(), Embeds.simpleInsufficientPerm("ADD_REACTIONS"));
            } else if (ex.toString().contains("MESSAGE_HISTORY")) {
                Messages.sendMessage(msg.getTextChannel(), Embeds.simpleInsufficientPerm("READ_MESSAGE_HISTORY"));
            }
            Messages.deleteMessage(msg);
            throw new PermissionException("Missing Permissions to add Reaction.");
        } catch (ErrorResponseException ignored) {
        }
    }

    /**
     * Clears all reactions from a target message.
     *
     * @param msg The message to have its' reactions cleared.
     */
    public static void clearReactions(Message msg) {
        Consumer<Throwable> throwableConsumer = throwable -> {
            if (throwable.toString().contains("MISSING_PERMISSIONS")) {
                Messages.sendMessage(msg.getTextChannel(), Embeds.simpleInsufficientPerm("MANAGE_MESSAGES"));
            }
        };

        try {
            msg.clearReactions().queue(null, throwableConsumer);
        } catch (InsufficientPermissionException ex) {
            Messages.sendMessage(msg.getTextChannel(), Embeds.simpleInsufficientPerm("MANAGE_MESSAGES"));
            Messages.deleteMessage(msg);
        } catch (ErrorResponseException ignored) {
        }
    }

    /**
     * Attempts to get a Message from the Discord's
     * servers that has the same id as the id provided.
     * Runs a consumer after completion.
     *
     * @param channel The channel to search the message on.
     * @param msgId   The id of the sought after Message
     * @param success The consumer to run after completion.
     */
    public static void retrieveMessageById(TextChannel channel, String msgId, Consumer<Message> success) {
        Consumer<Throwable> throwableConsumer = throwable -> {
            if (throwable.toString().contains("MISSING_ACCESS")) {
                Messages.sendMessage(channel, Embeds.simpleInsufficientPerm("MESSAGE_READ"));
            } else if (throwable.toString().contains("MISSING_PERMISSIONS")) {
                Messages.sendMessage(channel, Embeds.simpleInsufficientPerm("READ_MESSAGE_HISTORY"));
            }
        };

        try {
            channel.retrieveMessageById(msgId).queue(success, throwableConsumer);
        } catch (InsufficientPermissionException ex) {
            Messages.sendMessage(channel, Embeds.simpleInsufficientPerm("MESSAGE_READ and READ_MESSAGE_HISTORY"));
        }
    }

    /**
     * Attempts to get a Message from the Discord's
     * servers that has the same id as the id provided.
     *
     * @param channel The channel to search the message on.
     * @param msgId   The id of the sought after Message
     */
    public static boolean checkRetrieveMessageById(TextChannel channel, String msgId) {

        AtomicBoolean accessible = new AtomicBoolean(true);

        Consumer<Throwable> throwableConsumer = throwable -> {
            if (throwable.toString().contains("MISSING_ACCESS")) {
                Messages.sendMessage(channel, Embeds.simpleInsufficientPerm("MESSAGE_READ"));
            } else if (throwable.toString().contains("MISSING_PERMISSIONS")) {
                Messages.sendMessage(channel, Embeds.simpleInsufficientPerm("READ_MESSAGE_HISTORY"));
            }
            accessible.set(false);
        };

        try {
            channel.retrieveMessageById(msgId).queue(null, throwableConsumer);
        } catch (InsufficientPermissionException ex) {
            return false;
        }
        return accessible.get();
    }
}