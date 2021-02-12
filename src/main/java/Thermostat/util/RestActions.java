package thermostat.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import thermostat.util.entities.InsufficientPermissionsException;
import thermostat.util.enumeration.CommandType;

import java.io.InputStream;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A handy Message sender with static functions.
 */
public final class RestActions {
    @Contract("null -> fail")
    public static <X> void perform(@Nullable RestAction<X> action) throws RuntimeException {
        Objects.requireNonNull(action);
        action.queue();
    }

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
     * @param channel Channel to send the embed in.
     * @param eb      The embed to send.
     */
    /*public static void sendMessage(TextChannel channel, EmbedBuilder eb) throws InsufficientPermissionsException {
        EnumSet<Permission> missingPermissions = PermissionComputer.getMissingPermissions(
                channel.getGuild().getSelfMember(), channel,
                CommandType.SEND_MESSAGE_EMBED.getThermoPerms()
        );

        if (missingPermissions.isEmpty()) {
            channel.sendMessage(eb.build()).queue();
        } else {
            throw new InsufficientPermissionsException(missingPermissions);
        }
    }*/

    /**
     * Sends an embed to a designated channel.
     *
     * @param channel Channel to send the embed in.
     * @param eb      The embed to send.
     */
    public static RestAction<Message> sendMessage(TextChannel channel, EmbedBuilder eb) {
        return channel.sendMessage(eb.build());
    }

    /**
     * Edits a Message's content to the provided MessageEmbed.
     * @param channel    TextChannel the message resides on.
     * @param msgId      Message to edit.
     * @param newContent New embed to place in the message.
     */
    public static void editMessage(TextChannel channel, String msgId, EmbedBuilder newContent) throws InsufficientPermissionsException {
        EnumSet<Permission> missingPermissions = PermissionComputer.getMissingPermissions(
                channel.getGuild().getSelfMember(), channel,
                CommandType.EDIT_MESSAGE.getThermoPerms()
        );

        if (missingPermissions.isEmpty()) {
            channel.retrieveMessageById(msgId).queue(message ->
                    message.clearReactions().and(message.editMessage(newContent.build())).queue()
            );
        } else {
            throw new InsufficientPermissionsException(missingPermissions);
        }
    }

    /**
     * Edits a Message's content to the provided MessageEmbed.
     * @param newContent New embed to place in the message.
     */
    public static RestAction<Message> editMessage(Message message, EmbedBuilder newContent) throws InsufficientPermissionsException {
        return message.editMessage(newContent.build());
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
            channel.retrieveMessageById(msgId).queue(message -> message.delete().queue());
        } else {
            throw new InsufficientPermissionsException(missingPermissions);
        }
    }

    /**
     * Adds a list of reactions to a given message.
     * @param unicode The unicode emoji to add as a reaction.
     */
    public static RestAction<Void> addReactions(final Message message, List<String> unicode) throws InsufficientPermissionsException {
        RestAction<Void> action = null;
        for (final String it : unicode) {
            if (action == null) {
                action = message.addReaction(it);
            }
            action = action.and(message.addReaction(it));
        }
        return action;
    }
}