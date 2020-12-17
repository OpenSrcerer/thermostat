package thermostat.thermoFunctions.commands.monitoring;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.mySQL.DataSource;
import thermostat.preparedStatements.DynamicEmbeds;
import thermostat.preparedStatements.ErrorEmbeds;
import thermostat.preparedStatements.HelpEmbeds;
import thermostat.thermoFunctions.Messages;
import thermostat.thermoFunctions.commands.Command;
import thermostat.thermoFunctions.entities.CommandType;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("ConstantConditions")
public class SensitivityCommand implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(SensitivityCommand.class);

    private final GuildMessageReceivedEvent data;
    private List<String> arguments;
    private final String prefix;

    public SensitivityCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) {
        this.data = data;
        this.arguments = arguments;
        this.prefix = prefix;

        if (validateEvent(data)) {
            checkPermissionsAndQueue(this);
        }
    }

    /**
     * Command form: th!sensitivity <sensitivity> [channel(s)/category(ies)]
     */
    @Override
    public void run() {
        if (arguments.isEmpty()) {
            Messages.sendMessage(data.getChannel(), HelpEmbeds.helpSensitivity(prefix));
            return;
        }

        StringBuilder nonValid,
                noText,
                complete = new StringBuilder(),
                badSensitivity = new StringBuilder();
        final float offset;

        // #1 - Parse sensitivity argument
        try {
            offset = Float.parseFloat(arguments.get(0));
            arguments.remove(0);
        } catch (NumberFormatException ex) {
            Messages.sendMessage(data.getChannel(), ErrorEmbeds.invalidSensitivity());
            return;
        }

        // #2 - Retrieve target channels
        {
            List<?> results = parseChannelArgument(data.getChannel(), arguments);

            nonValid = (StringBuilder) results.get(0);
            noText = (StringBuilder) results.get(1);
            //noinspection unchecked
            arguments = ((ArrayList<String>) results.get(2));
        }

        // #3 - Perform appropriate action
        for (String arg : arguments) {
            try {
                addIfNotInDb(data.getGuild().getId(), arg);
                if (offset >= -10 && offset <= 10) {
                    DataSource.update("UPDATE CHANNEL_SETTINGS SET SENSOFFSET = ? WHERE CHANNEL_ID = ?",
                            Arrays.asList(Float.toString(1f + offset / 20f), arg));
                    complete.append("<#").append(arg).append("> ");
                } else {
                    badSensitivity.append("<#").append(arg).append("> ");
                }

            } catch (SQLException ex) {
                Messages.sendMessage(data.getChannel(), ErrorEmbeds.errFatal("running the command again", ex.getLocalizedMessage()));
                lgr.warn("(" + data.getGuild().getName() + "/" + data.getGuild().getId() + ") - " + ex.toString());
                return;
            }
        }

        // #4 - Send embed results to user
        Messages.sendMessage(data.getChannel(), DynamicEmbeds.dynamicEmbed(
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
                data.getMember().getUser()
        ));
        lgr.info("Successfully executed on (" + data.getGuild().getName() + "/" + data.getGuild().getId() + ").");
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
}
