package thermostat;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import thermostat.util.PermissionComputer;
import thermostat.util.entities.InsufficientPermissionsException;
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
    public static void sendMessage(TextChannel channel, InputStream inputStream, EmbedBuilder embed) throws InsufficientPermissionsException {
        EnumSet<Permission> missingPermissions = PermissionComputer.getMissingPermissions(
                channel.getGuild().getSelfMember(), channel,
                CommandType.SEND_MESSAGE_ATTACHMENT.getThermoPerms()
        );

        if (missingPermissions.isEmpty()) {
            channel.sendFile(inputStream, "chart.png")
                    .embed(embed.setImage("attachment://chart.png").build())
                    .queue();
        } else {
            throw new InsufficientPermissionsException(missingPermissions);
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
    public static void sendMessage(TextChannel channel, EmbedBuilder eb, Consumer<Message> success) throws InsufficientPermissionsException {
        EnumSet<Permission> missingPermissions = PermissionComputer.getMissingPermissions(
                channel.getGuild().getSelfMember(), channel,
                CommandType.SEND_MESSAGE_EMBED.getThermoPerms()
        );

        if (missingPermissions.isEmpty()) {
            channel.sendMessage(eb.build()).queue(success);
        } else {
            throw new InsufficientPermissionsException(missingPermissions);
        }
    }

    /**
     * Sends an embed to a designated channel.
     *
     * @param channel Channel to send the embed in.
     * @param eb      The embed to send.
     */
    public static void sendMessage(TextChannel channel, EmbedBuilder eb) throws InsufficientPermissionsException {
        EnumSet<Permission> missingPermissions = PermissionComputer.getMissingPermissions(
                channel.getGuild().getSelfMember(), channel,
                CommandType.SEND_MESSAGE_EMBED.getThermoPerms()
        );

        if (missingPermissions.isEmpty()) {
            channel.sendMessage(eb.build()).queue();
        } else {
            throw new InsufficientPermissionsException(missingPermissions);
        }
    }

    /**
     * Sends a text message to a designated channel.
     *
     * @param channel Channel to send the message in.
     * @param msg     The message to send.
     */
    public static void sendMessage(TextChannel channel, String msg) {
        EnumSet<Permission> missingPermissions = PermissionComputer.getMissingPermissions(
                channel.getGuild().getSelfMember(), channel,
                CommandType.SEND_MESSAGE_TEXT.getThermoPerms()
        );

        if (missingPermissions.isEmpty()) {
            channel.sendMessage(msg).queue();
        }
    }

    /**
     * Edits a Message's content to the provided MessageEmbed.
     * @param channel    TextChannel the message resides on.
     * @param msgId      Message to edit.
     * @param newContent New embed to place in the message.
     */
    public static void editMessage(TextChannel channel, String msgId, MessageEmbed newContent) throws InsufficientPermissionsException {
        EnumSet<Permission> missingPermissions = PermissionComputer.getMissingPermissions(
                channel.getGuild().getSelfMember(), channel,
                CommandType.EDIT_MESSAGE.getThermoPerms()
        );

        if (missingPermissions.isEmpty()) {
            channel.retrieveMessageById(msgId).queue(message -> message.editMessage(newContent).queue());
        } else {
            throw new InsufficientPermissionsException(missingPermissions);
        }
    }

    /**
     * Deletes a message from Discord.
     * @param msgId ID of message to delete.
     */
    public static void deleteMessage(TextChannel channel, String msgId) throws InsufficientPermissionsException {
        EnumSet<Permission> missingPermissions = PermissionComputer.getMissingPermissions(
                channel.getGuild().getSelfMember(), channel,
                CommandType.DELETE_MESSAGE.getThermoPerms()
        );

        if (missingPermissions.isEmpty()) {
            channel.retrieveMessageById(msgId).queue(message -> message.delete().queueAfter(500, TimeUnit.MILLISECONDS));
        } else {
            throw new InsufficientPermissionsException(missingPermissions);
        }
    }

    /**
     * Adds a list of reactions to a given message.
     * @param channel Channel where the target message resides in.
     * @param msgId   The id of the target message.
     * @param unicode The unicode emoji to add as a reaction.
     */
    public static void addReactions(TextChannel channel, String msgId, List<String> unicode) throws InsufficientPermissionsException {
        EnumSet<Permission> missingPermissions = PermissionComputer.getMissingPermissions(
                channel.getGuild().getSelfMember(), channel,
                CommandType.ADD_REACTIONS.getThermoPerms()
        );

        if (missingPermissions.isEmpty()) {
            channel.retrieveMessageById(msgId).queue(message -> {
                long reactionsDuration = 750;
                for (String it : unicode) {
                    message.addReaction(it).queueAfter(reactionsDuration, TimeUnit.MILLISECONDS);
                    reactionsDuration += 500;
                }
            });
        } else {
            throw new InsufficientPermissionsException(missingPermissions);
        }
    }

    /**
     * Adds a list of reactions to a given message.
     * @param message The message which will have reactions added to it.
     * @param unicode The unicode emoji to add as a reaction.
     */
    public static void addReactions(Message message, List<String> unicode) throws InsufficientPermissionsException {
        EnumSet<Permission> missingPermissions = PermissionComputer.getMissingPermissions(
                message.getGuild().getSelfMember(), message.getTextChannel(),
                CommandType.ADD_REACTIONS.getThermoPerms()
        );

        if (missingPermissions.isEmpty()) {
            long reactionsDuration = 750;
            for (String it : unicode) {
                message.addReaction(it).queueAfter(reactionsDuration, TimeUnit.MILLISECONDS);
                // Timer to prevent RateLimiting
                reactionsDuration += 500;
            }
        } else {
            throw new InsufficientPermissionsException(missingPermissions);
        }
    }

    /**
     * Adds a reaction to a given message.
     * @param message  The target message.
     * @param unicode The unicode emoji to add as a reaction.
     */
    public static void addReaction(Message message, String unicode) throws InsufficientPermissionsException {
        EnumSet<Permission> missingPermissions = PermissionComputer.getMissingPermissions(
                message.getGuild().getSelfMember(), message.getTextChannel(),
                CommandType.ADD_REACTIONS.getThermoPerms()
        );

        if (missingPermissions.isEmpty()) {
            message.addReaction(unicode).queue();
        } else {
            throw new InsufficientPermissionsException(missingPermissions);
        }
    }

    /**
     * Clears all reactions from a target message.
     * @param channel Channel that the message resides in.
     * @param msgId   The ID of message to have its' reactions cleared.
     */
    public static void clearReactions(TextChannel channel, String msgId) throws InsufficientPermissionsException {
        EnumSet<Permission> missingPermissions = PermissionComputer.getMissingPermissions(
                channel.getGuild().getSelfMember(), channel,
                CommandType.DELETE_REACTIONS.getThermoPerms()
        );

        if (missingPermissions.isEmpty()) {
            channel.retrieveMessageById(msgId).queue(message -> message.clearReactions().queue());
        } else {
            throw new InsufficientPermissionsException(missingPermissions);
        }
    }
}