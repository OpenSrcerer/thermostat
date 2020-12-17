package thermostat.thermoFunctions.commands.requestFactories.utility;

import net.dv8tion.jda.api.entities.MessageEmbed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.mySQL.Create;
import thermostat.preparedStatements.DynamicEmbeds;
import thermostat.preparedStatements.ErrorEmbeds;
import thermostat.preparedStatements.HelpEmbeds;
import thermostat.thermoFunctions.Messages;
import thermostat.thermoFunctions.commands.CommandData;
import thermostat.thermoFunctions.commands.requestFactories.Command;
import thermostat.thermoFunctions.commands.requestFactories.monitoring.SetBoundsCommand;
import thermostat.thermoFunctions.entities.RequestType;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static thermostat.thermoFunctions.Functions.convertToBooleanInteger;


public class FilterCommand implements Command {

    private static final Logger lgr = LoggerFactory.getLogger(SetBoundsCommand.class);

    private final CommandData data;

    public FilterCommand(CommandData data) {
        this.data = data;

        checkPermissionsAndExecute(RequestType.FILTER, data.member(), data.channel(), lgr);
    }

    /**
     * Command form: th!filter <true/false> [channel(s)/category(ies)]
     * @return
     */
    @Override
    public MessageEmbed execute() {
        if (data.arguments().isEmpty()) {
            Messages.sendMessage(data.channel(), HelpEmbeds.helpFilter(data.prefix()));
            return;
        }

        int filtered = convertToBooleanInteger(data.arguments().get(0));
        String message;

        data.arguments().remove(0);
        StringBuilder nonValid,
                noText,
                complete;

        {
            List<?> results = parseChannelArgument(data.channel(), data.arguments());

            nonValid = (StringBuilder) results.get(0);
            noText = (StringBuilder) results.get(1);
            // Suppressing is okay because type for
            // results.get(3) is always ArrayList<String>
            //noinspection unchecked
            data.replaceArguments((ArrayList<String>) results.get(2));
        }
        // data.arguments() now remains as a list of target channel(s).

        // individually enable filtering in every channel
        // after checking whether the channel exists in the db
        try {
            addIfNotInDb(data.guild().getId(), data.arguments());
            complete = Create.setFilter(Integer.toString(filtered), data.arguments());
        } catch (SQLException ex) {
            Messages.sendMessage(data.channel(), ErrorEmbeds.errFatal("running the command again", ex.getLocalizedMessage()));
            lgr.warn("(" + data.guild().getName() + "/" + data.guild().getId() + ") - " + ex.toString());
            return;
        }

        // switch message depending on user action
        if (filtered == 1) {
            message = "Enabled filtering on:";
        } else {
            message = "Disabled filtering on:";
        }

        Messages.sendMessage(data.channel(), DynamicEmbeds.dynamicEmbed(
                Arrays.asList(
                        message,
                        complete.toString(),
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
