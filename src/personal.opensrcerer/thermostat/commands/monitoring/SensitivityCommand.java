package thermostat.commands.monitoring;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.commands.Command;
import thermostat.dispatchers.CommandDispatcher;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.embeds.Embeds;
import thermostat.mySQL.DataSource;
import thermostat.util.ArgumentParser;
import thermostat.util.entities.CommandArguments;
import thermostat.util.entities.CommandContext;
import thermostat.util.enumeration.CommandType;
import thermostat.util.enumeration.EmbedType;

import javax.annotation.Nonnull;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;

import static thermostat.util.ArgumentParser.hasArguments;

public class SensitivityCommand implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(SensitivityCommand.class);
    private final CommandContext data;

    public SensitivityCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) {
        this.data = new CommandContext(data, arguments, prefix);

        if (this.data.parameters == null) {
            ResponseDispatcher.commandFailed(
                    this,
                    Embeds.getEmbed(EmbedType.HELP_SENSITIVITY, this.data),
                    "Bad arguments.");
            return;
        }

        CommandDispatcher.checkPermissionsAndQueue(this);
    }

    /**
     * Command form: th!sensitivity
     * -s <sensitivity>
     * -c <channels/categories>
     */
    @Override
    public void run() {
        final List<String> channels = data.parameters.get("c");
        final List<String> sensitivity = data.parameters.get("s");
        final float offset;

        // Check that sensitivity has arguments
        if (!hasArguments(sensitivity)) {
            ResponseDispatcher.commandFailed(this,
                    Embeds.getEmbed(EmbedType.HELP_SENSITIVITY, data),
                    "User did not provide arguments.");
            return;
        }

        // Parse sensitivity argument
        try {
            offset = Float.parseFloat(sensitivity.get(0));
            if (offset <= 0f) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            ResponseDispatcher.commandFailed(this,
                    Embeds.getEmbed(EmbedType.ERR_INPUT, data,
                            "Sensitivity value must be larger than 0!"),
                    "Invalid sensitivity value.");
            return;
        }

        sensitivityAction(
                ArgumentParser.parseChannelArgument(data.event.getChannel(), channels),
                offset
        );
    }

    private void sensitivityAction(final CommandArguments arguments, final float offset) {
        StringBuilder complete = new StringBuilder();

        // Update sensitivity value on the database
        try {
            DataSource.demand(conn -> {
                for (final String channel : arguments.channels) {
                    PreparedStatement statement = conn.prepareStatement("UPDATE CHANNEL_SETTINGS SET SENSOFFSET = ? WHERE CHANNEL_ID = ?");
                    statement.setFloat(1, offset);
                    statement.setString(2, channel);
                    statement.executeUpdate();
                    complete.append("<#").append(channel).append("> ");
                }
                return null;
            });
        } catch (Exception ex) {
            ResponseDispatcher.commandFailed(this,
                    Embeds.getEmbed(EmbedType.ERR, data, ex.getMessage()),
                    ex);
            return;
        }

        // Send embed results to user
        ResponseDispatcher.commandSucceeded(this,
                Embeds.getEmbed(EmbedType.DYNAMIC, data,
                        Arrays.asList(
                                "Channels given a new sensitivity of " + offset + ":",
                                complete.toString(),
                                "Channels that were not valid or found:",
                                arguments.nonValid.toString(),
                                "Categories with no Text Channels:",
                                arguments.noText.toString()
                        )
                )
        );
    }

    @Override
    public CommandType getType() {
        return CommandType.SENSITIVITY;
    }

    @Override
    public Logger getLogger() {
        return lgr;
    }

    @Override
    public CommandContext getData() {
        return data;
    }
}
