package thermostat.thermoFunctions.commands.monitoring;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.mySQL.DataSource;
import thermostat.preparedStatements.DynamicEmbeds;
import thermostat.preparedStatements.ErrorEmbeds;
import thermostat.preparedStatements.HelpEmbeds;
import thermostat.thermoFunctions.Messages;
import thermostat.thermoFunctions.commands.CommandEvent;
import thermostat.thermoFunctions.entities.CommandType;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static thermostat.thermoFunctions.Functions.parseSlowmode;

public class SetBounds implements CommandEvent {

    private static final Logger lgr = LoggerFactory.getLogger(SetBounds.class);

    private final Guild eventGuild;
    private final TextChannel eventChannel;
    private final Member eventMember;
    private final String eventPrefix;
    private ArrayList<String> args;

    private enum ActionType {
        INVALID, MINIMUM, MAXIMUM
    }

    public SetBounds(Guild eg, TextChannel tc, Member em, String px, ArrayList<String> ag) {
        eventGuild = eg;
        eventChannel = tc;
        eventMember = em;
        eventPrefix = px;
        args = ag;

        checkPermissionsAndExecute(CommandType.SETBOUNDS, eventMember, eventChannel, lgr);
    }

    /**
     * Command form: th!setbounds <min/max> <slowmode> [channel(s)/category(ies)]
     */
    @Override
    public void execute() {
        if (args.size() < 2) {
            Messages.sendMessage(eventChannel, HelpEmbeds.helpSetBounds(eventPrefix));
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
        if (args.get(0).contains("max")) {
            type = ActionType.MAXIMUM;
        } else if (args.get(0).contains("min")) {
            type = ActionType.MINIMUM;
        }

        // #2 - Check the [slowmode] argument
        try {
            argumentSlow = parseSlowmode(args.get(1));
        } catch (NumberFormatException ex) {
            Messages.sendMessage(eventChannel, ErrorEmbeds.invalidSlowmode());
            return;
        }

        // #3 - Remove the [min/max] and [slowmode] arguments
        args.subList(0, 2).clear();

        // #4 - Parse the optional <channels/categories> argument
        {
            List<?> results = parseChannelArgument(eventChannel, args);

            nonValid = (StringBuilder) results.get(0);
            noText = (StringBuilder) results.get(1);
            // Suppressing is okay because type for
            // results.get(3) is always ArrayList<String>
            //noinspection unchecked
            args = (ArrayList<String>) results.get(2);
        }
        // args now remains as a list of target channel(s).

        // #5 - Perform the appropriate actions
        int minimumSlow, maximumSlow;
        
        if (type != ActionType.INVALID) {
            for (String arg : args) {
                try {{
                        addIfNotInDb(eventGuild.getId(), arg);
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
                    Messages.sendMessage(eventChannel, ErrorEmbeds.errFatal("running the command again", ex.getLocalizedMessage()));
                    lgr.warn("(" + eventGuild.getName() + "/" + eventGuild.getId() + ") - " + ex.toString());
                    return;
                }
            }
        } else {
            Messages.sendMessage(eventChannel, HelpEmbeds.helpSetBounds(eventPrefix));
        }

        // #6 - Send the results embed
        Messages.sendMessage(eventChannel, DynamicEmbeds.dynamicEmbed(
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
                eventMember.getUser()
        ));
        lgr.info("Successfully executed on (" + eventGuild.getName() + "/" + eventGuild.getId() + ").");
    }
}
