package thermostat.commands.monitoring;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.embeds.Embeds;
import thermostat.commands.Command;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.mySQL.DataSource;
import thermostat.util.ArgumentParser;
import thermostat.util.MiscellaneousFunctions;
import thermostat.util.enumeration.CommandType;

import javax.annotation.Nonnull;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static thermostat.util.ArgumentParser.hasArguments;
import static thermostat.util.ArgumentParser.parseArguments;

@SuppressWarnings("ConstantConditions")
public class SensitivityCommand implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(SensitivityCommand.class);

    private GuildMessageReceivedEvent data = null;
    private Map<String, List<String>> parameters = null;
    private final String prefix;
    private final long commandId;

    public SensitivityCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) {
        this.commandId = MiscellaneousFunctions.getCommandId();
        this.prefix = prefix;

        try {
            this.parameters = parseArguments(arguments);
        } catch (Exception ex) {
            ResponseDispatcher.commandFailed(this, Embeds.inputError(ex.getLocalizedMessage(), this.commandId), ex);
            return;
        }

        if (validateEvent(data)) {
            this.data = data;
        } else {
            ResponseDispatcher.commandFailed(this, Embeds.error("Event was not valid. Please try again."), "Event had a null member.");
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
        final List<String> channels = parameters.get("c");
        final List<String> sensitivity = parameters.get("s");
        final float offset;

        // Check that sensitivity has arguments
        if (!hasArguments(sensitivity)) {
            ResponseDispatcher.commandFailed(this,
                    Embeds.inputError("Please insert a sensitivity value.", commandId),
                    "User did not provide arguments.");
            return;
        }

        // Parse sensitivity argument
        try {
            offset = Float.parseFloat(sensitivity.get(0));

            if (offset >= -10 && offset <= 10) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            ResponseDispatcher.commandFailed(this,
                    Embeds.inputError("Sensitivity value must be between -10 and 10 (inclusive).", commandId),
                    "User provided an incorrect sensitivity value.");
            return;
        }

        sensitivityAction(channels, offset);
    }

    private void sensitivityAction(final List<String> channels, final float offset) {
        StringBuilder nonValid,
                noText,
                complete = new StringBuilder(),
                badSensitivity = new StringBuilder();

        // #1 - Retrieve target channels
        {
            ArgumentParser.Arguments results = ArgumentParser.parseChannelArgument(data.getChannel(), channels);
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
                    Embeds.error(ex.getLocalizedMessage(), MiscellaneousFunctions.getCommandId()),
                    ex);
            return;
        }

        // #3 - Send embed results to user
        ResponseDispatcher.commandSucceeded(this,
                Embeds.dynamicEmbed(
                        Arrays.asList(
                                "Channels given a new sensitivity of " + offset + ":",
                                complete.toString(),
                                "Channels that were not valid or found:",
                                nonValid.toString(),
                                "Categories with no Text Channels:",
                                noText.toString()
                        ),
                        data.getMember().getUser(), commandId
                )
        );
    }

    @Override
    public GuildMessageReceivedEvent getEvent() {
        return data;
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
    public long getId() {
        return commandId;
    }
}
