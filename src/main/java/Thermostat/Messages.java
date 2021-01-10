package thermostat;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import thermostat.preparedStatements.ErrorEmbeds;
import thermostat.util.PermissionComputer;
import thermostat.util.enumeration.CommandType;

import java.io.InputStream;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * A handy Message sender with static functions.
 */
public final class Messages {

    /**
     * Sends a text message to a designated channel with an
     * image attachment.
     * @param channel     Channel to send the message in.
     * @param inputStream Attachment to get sent.
     * @param embed       Embed that contains the attachment.
     */
    public static void sendMessage(TextChannel channel, InputStream inputStream, EmbedBuilder embed) {
        if (PermissionComputer.getMissingPermissions(channel.getGuild().getSelfMember(), channel, CommandType.SEND_MESSAGE_ATTACHMENT.getThermoPerms()).isEmpty()) {
            channel.sendFile(inputStream, "chart.png")
                    .embed(embed.setImage("attachment://chart.png").build())
                    .queue();
        } else {
            Messages.sendMessage(channel, ErrorEmbeds.errPermission(EnumSet.of(Permission.MESSAGE_READ, Permission.MESSAGE_ATTACH_FILES)));
        }
    }

    /**
     * Sends an embed to a designated channel, runs
     * a success Consumer afterwards.
     *
     * @param channel Channel to send the embed in.
     * @param eb      The embed to send.
     * @param success The consumer to run after the .queue() call.
     */
    public static void sendMessage(TextChannel channel, EmbedBuilder eb, Consumer<Message> success) {
        if (PermissionComputer.getMissingPermissions(channel.getGuild().getSelfMember(), channel, CommandType.SEND_MESSAGE_EMBED.getThermoPerms()).isEmpty()) {
            channel.sendMessage(eb.build()).queue(success);
        } else {
            sendMessage(channel, "Please add the \"**Embed Links**\" permission to the bot!");
        }
    }

    /**
     * Sends an embed to a designated channel.
     *
     * @param channel Channel to send the embed in.
     * @param eb      The embed to send.
     */
    public static void sendMessage(TextChannel channel, EmbedBuilder eb) {
        if (PermissionComputer.getMissingPermissions(channel.getGuild().getSelfMember(), channel, CommandType.SEND_MESSAGE_EMBED.getThermoPerms()).isEmpty()) {
            channel.sendMessage(eb.build()).queue();
        } else {
            sendMessage(channel, "Please add the \"**Embed Links**\" permission to the bot!");
        }
    }

    /**
     * Sends a text message to a designated channel.
     *
     * @param channel Channel to send the message in.
     * @param msg     The message to send.
     */
    public static void sendMessage(TextChannel channel, String msg) {
        if (PermissionComputer.getMissingPermissions(channel.getGuild().getSelfMember(), channel, CommandType.SEND_MESSAGE_TEXT.getThermoPerms()).isEmpty()) {
            channel.sendMessage(msg).queue();
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
        if (PermissionComputer.getMissingPermissions(channel.getGuild().getSelfMember(), channel, CommandType.EDIT_MESSAGE.getThermoPerms()).isEmpty()) {
            channel.retrieveMessageById(msgId).queue(message -> message.editMessage(newContent).queue());
        } else {
            Messages.sendMessage(channel, ErrorEmbeds.errPermission(EnumSet.of(Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY)));
        }
    }

    /**
     * Deletes a message from Discord.
     *
     * @param msgId ID of message to delete.
     */
    public static void deleteMessage(TextChannel channel, String msgId) {
        try {
            channel.retrieveMessageById(msgId).queue(message -> {
                try {
                    message.delete().queueAfter(500, TimeUnit.MILLISECONDS);
                } catch (InsufficientPermissionException ex) {
                    Messages.sendMessage(channel, ErrorEmbeds.errPermission(EnumSet.of(Permission.MESSAGE_MANAGE)));
                }
            });
        } catch (InsufficientPermissionException ex) {
            Messages.sendMessage(channel, ErrorEmbeds.errPermission(EnumSet.of(Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY)));
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
        try {
            channel.retrieveMessageById(msgId).queue(message -> {
                long reactionsDuration = 750;
                for (String it : unicode) {
                    message.addReaction(it).queueAfter(reactionsDuration, TimeUnit.MILLISECONDS);
                    reactionsDuration += 500;
                }
            });
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
        long reactionsDuration = 750;
        for (String it : unicode) {
            message.addReaction(it).queueAfter(reactionsDuration, TimeUnit.MILLISECONDS);
            // Timer to prevent RateLimiting
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
        msg.addReaction(unicode).queue();
    }

    /**
     * Clears all reactions from a target message.
     *
     * @param channel Channel that the message resides in.
     * @param msgId   The ID of message to have its' reactions cleared.
     */
    public static void clearReactions(TextChannel channel, String msgId) throws InsufficientPermissionException {
        channel.retrieveMessageById(msgId).queue(message -> {
            try {
                message.clearReactions().queue();
            } catch (InsufficientPermissionException ex) {
                Messages.sendMessage(channel, ErrorEmbeds.errPermission(EnumSet.of(Permission.MESSAGE_MANAGE)));
                Messages.deleteMessage(channel, msgId);
            } catch (ErrorResponseException ignored) {
            }
        });
    }
}