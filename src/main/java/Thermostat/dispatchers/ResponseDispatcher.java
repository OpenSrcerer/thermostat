package thermostat.dispatchers;

import org.jetbrains.annotations.Contract;
import thermostat.commands.Command;
import thermostat.embeds.ThermoEmbed;
import thermostat.util.RestActions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.Objects;

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
    @Contract("null, _ -> fail")
    public static void commandSucceeded(final Command command, @Nullable ThermoEmbed embed) {
        command.getLogger().info("Command with ID [" + command.getData().commandId + "] was successful.");
        if (embed != null)
            RestActions.sendMessage(command.getData().event.getChannel(), embed).queue();
    }

    /**
     * Notify the command was completed successfully by sending an embed
     * with an inputStream (usually for files)
     * to the command event channel & logging it.
     * @param command Command that was completed.
     * @param embed Embed to send.
     * @param inputStream Stream to attach as a file to the embed.
     */
    @Contract("null, _, _ -> fail")
    public static void commandSucceeded(final Command command, @Nonnull ThermoEmbed embed, @Nonnull InputStream inputStream) {
        command.getLogger().info("Command with ID [" + command.getData().commandId + "] was successful.");
        command.getData().event.getChannel()
                .sendFile(inputStream, "chart.png")
                .embed(embed.setImage("attachment://chart.png").build())
                .queue();
    }

    /**
     * Notify the command failed by sending an embed, reason for failure,
     * and logging it.
     * @param command Command that failed.
     * @param embed Embed to send.
     * @param reason Reason for failure.
     */
    @Contract("null, _, _ -> fail")
    public static void commandFailed(final Command command, @Nullable ThermoEmbed embed, @Nonnull String reason) {
        command.getLogger().info("Command with ID [" + command.getData().commandId + "] has failed. Reason: " + reason);
        if (embed != null)
            RestActions.sendMessage(command.getData().event.getChannel(), embed).queue();
    }

    /**
     *
     * @param command Command that failed.
     * @param embed Embed to send.
     * @param throwable Details of the error.
     */
    @Contract("null, _, _ -> fail")
    public static void commandFailed(final Command command, @Nullable ThermoEmbed embed, @Nonnull Throwable throwable) {
        command.getLogger().info("Command with ID [" + command.getData().commandId + "] has failed. Details: ", throwable);
        if (embed != null)
            RestActions.sendMessage(command.getData().event.getChannel(), embed).queue();
    }

    /**
     *
     * @param command Command that failed.
     * @param embed Embed to send.
     */
    @Contract("null, _ -> fail")
    public static void commandFailed(final Command command, @Nullable ThermoEmbed embed) {
        Objects.requireNonNull(command);
        command.getLogger().info("Command with ID [" + command.getData().commandId + "] has failed. (Replied with help Embed).");
        if (embed != null)
            RestActions.sendMessage(command.getData().event.getChannel(), embed).queue();
    }
}
