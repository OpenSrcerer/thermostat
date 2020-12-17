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

import static thermostat.thermoFunctions.Functions.parseSlowmode;

public class SetBoundsCommand implements Command {

    private static final Logger lgr = LoggerFactory.getLogger(SetBoundsCommand.class);

    private final CommandData data;

    private enum ActionType {
        INVALID, MINIMUM, MAXIMUM
    }

    public SetBoundsCommand(CommandData data) {
        this.data = data;

        checkPermissionsAndExecute(RequestType.SETBOUNDS, data.member(), data.channel(), lgr);
    }

    /**
     * Command form: th!setbounds <min/max> <slowmode> [channel(s)/category(ies)]
     * @return
     */
    @Override
    public MessageEmbed execute() {
        if (data.arguments().size() < 2) {
            Messages.sendMessage(data.channel(), HelpEmbeds.helpSetBounds(data.prefix()));
            return;
        }

        StringBuilder nonValid,
                noText,
                minComplete = new StringBuilder(),
                maxComplete = new StringBuilder(),
                badSlowmode = new StringBuilder();

        // type represents the action being taken
        // setMaximum or setMinimum
        ActionType type = ActionType.INVALID;
        // value given to us by user to assign as slowmode
        int argumentSlow;

        // #1 - Check the [minimum/maximum] argument
        if (data.arguments().get(0).contains("max")) {
            type = ActionType.MAXIMUM;
        } else if (data.arguments().get(0).contains("min")) {
            type = ActionType.MINIMUM;
        }

        // #2 - Check the [slowmode] argument
        try {
            argumentSlow = parseSlowmode(data.arguments().get(1));
        } catch (NumberFormatException ex) {
            Messages.sendMessage(data.channel(), ErrorEmbeds.invalidSlowmode());
            return;
        }

        // #3 - Remove the [min/max] and [slowmode] arguments
        data.arguments().subList(0, 2).clear();

        // #4 - Parse the optional <channels/categories> argument
        {
            List<?> results = parseChannelArgument(data.channel(), data.arguments());

            nonValid = (StringBuilder) results.get(0);
            noText = (StringBuilder) results.get(1);
            // Suppressing is okay because type for
            // results.get(3) is always ArrayList<String>
            //noinspection unchecked
            data.replaceArguments((ArrayList<String>) results.get(2));
        }
        // args now remains as a list of target channel(s).

        // #5 - Perform the appropriate actions
        int minimumSlow, maximumSlow;
        
        if (type != ActionType.INVALID) {
            for (String arg : data.arguments()) {
                try {{
                        addIfNotInDb(data.guild().getId(), arg);
                        List<Integer> channelSlowmodes = DataSource.queryInts("SELECT MIN_SLOW, MAX_SLOW FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = ?", arg);

                        minimumSlow = channelSlowmodes.get(0);
                        maximumSlow = channelSlowmodes.get(1);
                    }

                    // if slowmode is over 6 hour limit, invalid
                    if (argumentSlow > 21600) {
                        badSlowmode.append("<#").append(arg).append("> ");
                    }
                    // -- Setting a Maximum Slowmode --
                    // if the argument < the minimum (cannot happen)
                    // update both so they're equal
                    else if (argumentSlow < minimumSlow && type == ActionType.MAXIMUM) {
                        DataSource.update("UPDATE CHANNEL_SETTINGS SET MAX_SLOW = ?, MIN_SLOW = ? WHERE CHANNEL_ID = ?",
                                Arrays.asList(Integer.toString(argumentSlow), Integer.toString(argumentSlow), arg));
                        maxComplete.append("<#").append(arg).append("> ");
                    }
                    // if the argument >= the minimum
                    // set maximum normally
                    else if (argumentSlow >= minimumSlow && type == ActionType.MAXIMUM) {
                        DataSource.update("UPDATE CHANNEL_SETTINGS SET MAX_SLOW = ? WHERE CHANNEL_ID = ?",
                                Arrays.asList(Integer.toString(argumentSlow), arg));
                        maxComplete.append("<#").append(arg).append("> ");
                    }
                    // -- Setting a Minimum Slowmode --
                    // if the argument > the maximum (cannot happen)
                    // update both so they're equal
                    else if (argumentSlow > maximumSlow) {
                        DataSource.update("UPDATE CHANNEL_SETTINGS SET MIN_SLOW = ?, MAX_SLOW = ? WHERE CHANNEL_ID = ?",
                                Arrays.asList(Integer.toString(argumentSlow), Integer.toString(argumentSlow), arg));
                        minComplete.append("<#").append(arg).append("> ");
                    }
                    // if the argument <= the maximum
                    // set minimum normally
                    else if (argumentSlow <= maximumSlow) {
                        DataSource.update("UPDATE CHANNEL_SETTINGS SET MIN_SLOW = ? WHERE CHANNEL_ID = ?",
                                Arrays.asList(Integer.toString(argumentSlow), arg));
                        minComplete.append("<#").append(arg).append("> ");
                    }

                } catch (SQLException ex) {
                    Messages.sendMessage(data.channel(), ErrorEmbeds.errFatal("running the command again", ex.getLocalizedMessage()));
                    lgr.warn("(" + data.guild().getName() + "/" + data.guild().getId() + ") - " + ex.toString());
                    return;
                }
            }
        } else {
            Messages.sendMessage(data.channel(), HelpEmbeds.helpSetBounds(data.prefix()));
        }

        // #6 - Send the results embed
        Messages.sendMessage(data.channel(), DynamicEmbeds.dynamicEmbed(
                Arrays.asList(
                        "Channels given a maximum slowmode of " + argumentSlow + ":",
                        maxComplete.toString(),
                        "Channels given a minimum slowmode of " + argumentSlow + ":",
                        minComplete.toString(),
                        "Channels for which the given slowmode value was not appropriate:",
                        badSlowmode.toString(),
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
