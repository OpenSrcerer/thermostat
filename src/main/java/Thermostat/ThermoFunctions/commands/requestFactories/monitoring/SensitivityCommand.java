package thermostat.thermoFunctions.commands.requestFactories.monitoring;

import net.dv8tion.jda.api.entities.MessageEmbed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.mySQL.DataSource;
import thermostat.preparedStatements.DynamicEmbeds;
import thermostat.preparedStatements.ErrorEmbeds;
import thermostat.preparedStatements.HelpEmbeds;
import thermostat.thermoFunctions.Messages;
import thermostat.thermoFunctions.commands.CommandData;
import thermostat.thermoFunctions.commands.requestFactories.Command;
import thermostat.thermoFunctions.entities.RequestType;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SensitivityCommand implements Command {

    private static final Logger lgr = LoggerFactory.getLogger(SensitivityCommand.class);

    private final CommandData data;

    public SensitivityCommand(CommandData data) {
        this.data = data;

        checkPermissionsAndExecute(RequestType.SENSITIVITY, data.member(), data.channel(), lgr);
    }

    /**
     * Command form: th!sensitivity <sensitivity> [channel(s)/category(ies)]
     * @return
     */
    @Override
    public MessageEmbed execute() {
        if (data.arguments().isEmpty()) {
            Messages.sendMessage(data.channel(), HelpEmbeds.helpSensitivity(data.prefix()));
            return;
        }

        StringBuilder nonValid,
                noText,
                complete = new StringBuilder(),
                badSensitivity = new StringBuilder();
        final float offset;

        // #1 - Parse sensitivity argument
        try {
            offset = Float.parseFloat(data.arguments().get(0));
            data.arguments().remove(0);
        } catch (NumberFormatException ex) {
            Messages.sendMessage(data.channel(), ErrorEmbeds.invalidSensitivity());
            return;
        }

        // #2 - Retrieve target channels
        {
            List<?> results = parseChannelArgument(data.channel(), data.arguments());

            nonValid = (StringBuilder) results.get(0);
            noText = (StringBuilder) results.get(1);
            //noinspection unchecked
            data.replaceArguments((ArrayList<String>) results.get(2));
        }

        // #3 - Perform appropriate action
        for (String arg : data.arguments()) {
            try {
                addIfNotInDb(data.guild().getId(), arg);
                if (offset >= -10 && offset <= 10) {
                    DataSource.update("UPDATE CHANNEL_SETTINGS SET SENSOFFSET = ? WHERE CHANNEL_ID = ?",
                            Arrays.asList(Float.toString(1f + offset / 20f), arg));
                    complete.append("<#").append(arg).append("> ");
                } else {
                    badSensitivity.append("<#").append(arg).append("> ");
                }

            } catch (SQLException ex) {
                Messages.sendMessage(data.channel(), ErrorEmbeds.errFatal("running the command again", ex.getLocalizedMessage()));
                lgr.warn("(" + data.guild().getName() + "/" + data.guild().getId() + ") - " + ex.toString());
                return;
            }
        }

        // #4 - Send embed results to user
        Messages.sendMessage(data.channel(), DynamicEmbeds.dynamicEmbed(
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
                data.member().getUser()
        ));
        lgr.info("Successfully executed on (" + data.guild().getName() + "/" + data.guild().getId() + ").");
    }

    @Override
    public CommandData getData() {
        return data;
    }
}
