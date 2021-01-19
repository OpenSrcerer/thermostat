package thermostat.commands.monitoring;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.Embeds.DynamicEmbeds;
import thermostat.Embeds.ErrorEmbeds;
import thermostat.commands.Command;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.mySQL.DataSource;
import thermostat.util.Functions;
import thermostat.util.enumeration.CommandType;

import javax.annotation.Nonnull;
import java.sql.SQLException;
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
        this.commandId = Functions.getCommandId();
        this.prefix = prefix;

        try {
            this.parameters = parseArguments(arguments);
        } catch (Exception ex) {
            ResponseDispatcher.commandFailed(this, ErrorEmbeds.inputError(ex.getLocalizedMessage(), this.commandId), ex);
            return;
        }

        if (validateEvent(data)) {
            this.data = data;
        } else {
            ResponseDispatcher.commandFailed(this, ErrorEmbeds.error("Event was not valid. Please try again."), "Event had a null member.");
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
        List<String> channels = parameters.get("c");
        final List<String> sensitivity = parameters.get("s");

        if (!hasArguments(sensitivity)) {
            ResponseDispatcher.commandFailed(this,
                    ErrorEmbeds.inputError("Please insert a sensitivity value.", commandId),
                    "User did not provide arguments.");
            return;
        }

        sensitivityAction(channels, sensitivity.get(0));
    }

    private void sensitivityAction(List<String> channels, String sensitivity) {
        StringBuilder nonValid,
                noText,
                complete = new StringBuilder(),
                badSensitivity = new StringBuilder();
        final float offset;

        // #1 - Parse sensitivity argument
        try {
            offset = Float.parseFloat(sensitivity);
        } catch (NumberFormatException ex) {
            ResponseDispatcher.commandFailed(this,
                    ErrorEmbeds.inputError("Sensitivity value must be between -10 and 10 (inclusive).", commandId),
                    "User provided an incorrect sensitivity value.");
            return;
        }

        // #2 - Retrieve target channels
        {
            Arguments results = parseChannelArgument(data.getChannel(), channels);

            nonValid = results.nonValid;
            noText = results.noText;
            channels = results.newArguments;
        }

        // #3 - Perform appropriate action
        for (String channel : channels) {
            try {
                addIfNotInDb(data.getGuild().getId(), channel);
                if (offset >= -10 && offset <= 10) {
                    DataSource.update("UPDATE CHANNEL_SETTINGS SET SENSOFFSET = ? WHERE CHANNEL_ID = ?",
                            Float.toString(1f + offset / 20f), channel);
                    complete.append("<#").append(channel).append("> ");
                } else {
                    badSensitivity.append("<#").append(channel).append("> ");
                }

            } catch (SQLException ex) {
                ResponseDispatcher.commandFailed(this,
                        ErrorEmbeds.error(ex.getLocalizedMessage(), Functions.getCommandId()),
                        ex);
                return;
            }
        }

        // #4 - Send embed results to user
        ResponseDispatcher.commandSucceeded(this,
                DynamicEmbeds.dynamicEmbed(
                        Arrays.asList(
                                "Channels given a new sensitivity of " + offset + ":",
                                complete.toString(),
                                "Offset value was not appropriate:",
                                badSensitivity.toString(),
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
