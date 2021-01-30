package thermostat.commands.monitoring;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.commands.Command;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.embeds.Embeds;
import thermostat.mySQL.DataSource;
import thermostat.util.ArgumentParser;
import thermostat.util.entities.Arguments;
import thermostat.util.entities.CommandData;
import thermostat.util.enumeration.CommandType;
import thermostat.util.enumeration.EmbedType;

import javax.annotation.Nonnull;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;

import static thermostat.util.ArgumentParser.hasArguments;

public class SensitivityCommand implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(SensitivityCommand.class);
    private final CommandData data;

    public SensitivityCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) {
        this.data = new CommandData(data, arguments, prefix);

        if (this.data.parameters == null) {
            ResponseDispatcher.commandFailed(
                    this,
                    Embeds.getEmbed(EmbedType.ERR, this.data),
                    "Bad arguments.");
            return;
        }

        checkPermissionsAndQueue(this);
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
                    Embeds.getEmbed(EmbedType.ERR_INPUT, data,
                            "Please insert a sensitivity value."),
                    "User did not provide arguments.");
            return;
        }

        // Parse sensitivity argument
        try {
            offset = Float.parseFloat(sensitivity.get(0));

            if (offset <= -10 && offset >= 10) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            ResponseDispatcher.commandFailed(this,
                    Embeds.getEmbed(EmbedType.ERR_INPUT, data,
                            "Sensitivity value must be between -10 and 10 (inclusive)."),
                    "User provided an incorrect sensitivity value.");
            return;
        }

        sensitivityAction(channels, offset);
    }

    private void sensitivityAction(final List<String> channels, final float offset) {
        StringBuilder nonValid,
                noText,
                complete = new StringBuilder();

        // #1 - Retrieve target channels
        {
            Arguments results = ArgumentParser.parseChannelArgument(data.event.getChannel(), channels);
            channels.clear();

            nonValid = results.nonValid;
            noText = results.noText;
            channels.addAll(results.newArguments);
        }

        // #2 - Perform appropriate action
        try {
            DataSource.execute(conn -> {
                for (String channel : channels) {
                    PreparedStatement statement = conn.prepareStatement("UPDATE CHANNEL_SETTINGS SET SENSOFFSET = ? WHERE CHANNEL_ID = ?");
                    statement.setFloat(1, 1f + offset / 20f);
                    statement.setString(2, channel);
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

        // #3 - Send embed results to user
        ResponseDispatcher.commandSucceeded(this,

                Embeds.getEmbed(EmbedType.DYNAMIC, data,
                        Arrays.asList(
                                "Channels given a new sensitivity of " + offset + ":",
                                complete.toString(),
                                "Channels that were not valid or found:",
                                nonValid.toString(),
                                "Categories with no Text Channels:",
                                noText.toString()
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
    public CommandData getData() {
        return data;
    }
}
