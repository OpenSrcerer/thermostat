package thermostat.dispatchers;

import net.dv8tion.jda.api.entities.Message;
import thermostat.Messages;
import thermostat.commands.Command;
import thermostat.embeds.ThermoEmbed;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.function.Consumer;

/**
 * Organizes all types of responses from Commands into a comprehensible
 * set of functions to output data to the end user.
 */
public final class ResponseDispatcher {

    /**
     * Notify the command was completed successfully by sending an embed
     * to the command event channel & logging it.
     * @param command Command that was completed.
     * @param embed Embed to send.
     */
    public static void commandSucceeded(@Nonnull Command command, @Nullable ThermoEmbed embed) {
        command.getLogger().info("Command with ID [" + command.getId() + "] was successful.");
        if (embed != null)
            Messages.sendMessage(command.getEvent().getChannel(), embed);
    }

    /**
     * Notify the command was completed successfully by sending an embed
     * with an inputStream (usually for files)
     * to the command event channel & logging it.
     * @param command Command that was completed.
     * @param embed Embed to send.
     * @param inputStream Stream to attach as a file to the embed.
     */
    public static void commandSucceeded(@Nonnull Command command, @Nonnull ThermoEmbed embed, @Nonnull InputStream inputStream) {
        command.getLogger().info("Command with ID [" + command.getId() + "] was successful.");
        Messages.sendMessage(command.getEvent().getChannel(), inputStream, embed);
    }

    /**
     * Notify the command was completed successfully by sending an embed
     * to the command event channel & logging it. Runs a given consumer on
     * completion.
     * @param command Command that was completed.
     * @param embed Embed to send.
     * @param consumer Consumer to run after message is sent.
     */
    public static void commandSucceeded(@Nonnull Command command, @Nonnull ThermoEmbed embed, @Nonnull Consumer<Message> consumer) {
        command.getLogger().info("Command with ID [" + command.getId() + "] was successful.");
        Messages.sendMessage(command.getEvent().getChannel(), embed, consumer);
    }

    /**
     * Notify the command failed by sending an embed, reason for failure,
     * and logging it.
     * @param command Command that failed.
     * @param embed Embed to send.
     * @param reason Reason for failure.
     */
    public static void commandFailed(@Nonnull Command command, @Nullable ThermoEmbed embed, @Nonnull String reason) {
        command.getLogger().info("Command with ID [" + command.getId() + "] has failed. Reason:" + reason);
        if (embed != null)
            Messages.sendMessage(command.getEvent().getChannel(), embed);
    }

    /**
     *
     * @param command Command that failed.
     * @param embed Embed to send.
     * @param throwable Details of the error.
     */
    public static void commandFailed(@Nonnull Command command, @Nullable ThermoEmbed embed, @Nonnull Throwable throwable) {
        command.getLogger().info("Command with ID [" + command.getId() + "] has failed. Details:", throwable);
        if (embed != null)
            Messages.sendMessage(command.getEvent().getChannel(), embed);
    }

    /**
     *
     * @param command Command that failed.
     * @param embed Embed to send.
     */
    public static void commandFailed(@Nonnull Command command, @Nullable ThermoEmbed embed) {
        command.getLogger().info("Command with ID [" + command.getId() + "] has failed. (Replied with help Embed).");
        if (embed != null)
            Messages.sendMessage(command.getEvent().getChannel(), embed);
    }
}
