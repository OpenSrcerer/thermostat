package thermostat.commands.monitoring;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.commands.Command;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.mySQL.DataSource;
import thermostat.preparedStatements.DynamicEmbeds;
import thermostat.preparedStatements.ErrorEmbeds;
import thermostat.preparedStatements.HelpEmbeds;
import thermostat.util.Functions;
import thermostat.util.enumeration.CommandType;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static thermostat.util.Functions.parseSlowmode;

@SuppressWarnings("ConstantConditions")
public class SetBoundsCommand implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(SetBoundsCommand.class);

    private final GuildMessageReceivedEvent data;
    private List<String> arguments;
    private final String prefix;
    private final long commandId;

    private enum ActionType {
        INVALID, MINIMUM, MAXIMUM
    }

    public SetBoundsCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) {
        this.data = data;
        this.arguments = arguments;
        this.prefix = prefix;
        this.commandId = Functions.getCommandId();

        if (validateEvent(data)) {
            checkPermissionsAndQueue(this);
        }
    }

    /**
     * Command form: th!setbounds <min/max> <slowmode> [channel(s)/category(ies)]
     */
    @Override
    public void run() {
        if (arguments.size() < 2) {
            ResponseDispatcher.commandFailed(this,
                    HelpEmbeds.helpSetBounds(prefix),
                    "User did not provide enough arguments.");
            return;
        }

        StringBuilder nonValid,
                noText,
                minComplete = new StringBuilder(),
                maxComplete = new StringBuilder(),
                badSlowmode = new StringBuilder();

        // type represents the action being taken
        // setMaximum or setMinimum
        ActionType type;
        // value given to us by user to assign as slowmode
        int argumentSlow;

        // #1 - Check the [minimum/maximum] argument
        if (arguments.get(0).contains("max")) {
            type = ActionType.MAXIMUM;
        } else if (arguments.get(0).contains("min")) {
            type = ActionType.MINIMUM;
        } else {
            ResponseDispatcher.commandFailed(this,
                    HelpEmbeds.helpSetBounds(prefix),
                    "User provided a wrong minimum/maximum argument.");
            return;
        }

        // #2 - Check the [slowmode] argument
        try {
            argumentSlow = parseSlowmode(arguments.get(1));
        } catch (NumberFormatException ex) {
            ResponseDispatcher.commandFailed(this,
                    ErrorEmbeds.inputError("Slowmode value \"" + arguments.get(1) + "\" was incorrect.", commandId),
                    "User provided an incorrect sensitivity value.");
            return;
        }

        // #3 - Remove the [min/max] and [slowmode] arguments
        arguments.subList(0, 2).clear();

        // #4 - Parse the optional <channels/categories> argument
        {
            Arguments results = parseChannelArgument(data.getChannel(), arguments);

            nonValid = results.nonValid;
            noText = results.noText;
            arguments = results.newArguments;
        }
        // args now remains as a list of target channel(s).

        // #5 - Perform the appropriate actions
        int minimumSlow, maximumSlow;
        
        if (type != ActionType.INVALID) {
            for (String arg : arguments) {
                try {{
                        addIfNotInDb(data.getGuild().getId(), arg);
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
                                Integer.toString(argumentSlow), Integer.toString(argumentSlow), arg);
                        maxComplete.append("<#").append(arg).append("> ");
                    }
                    // if the argument >= the minimum
                    // set maximum normally
                    else if (argumentSlow >= minimumSlow && type == ActionType.MAXIMUM) {
                        DataSource.update("UPDATE CHANNEL_SETTINGS SET MAX_SLOW = ? WHERE CHANNEL_ID = ?",
                                Integer.toString(argumentSlow), arg);
                        maxComplete.append("<#").append(arg).append("> ");
                    }
                    // -- Setting a Minimum Slowmode --
                    // if the argument > the maximum (cannot happen)
                    // update both so they're equal
                    else if (argumentSlow > maximumSlow) {
                        DataSource.update("UPDATE CHANNEL_SETTINGS SET MIN_SLOW = ?, MAX_SLOW = ? WHERE CHANNEL_ID = ?",
                                Integer.toString(argumentSlow), Integer.toString(argumentSlow), arg);
                        minComplete.append("<#").append(arg).append("> ");
                    }
                    // if the argument <= the maximum
                    // set minimum normally
                    else if (argumentSlow <= maximumSlow) {
                        DataSource.update("UPDATE CHANNEL_SETTINGS SET MIN_SLOW = ? WHERE CHANNEL_ID = ?",
                                Integer.toString(argumentSlow), arg);
                        minComplete.append("<#").append(arg).append("> ");
                    }

                } catch (SQLException ex) {
                    ResponseDispatcher.commandFailed(this,
                            ErrorEmbeds.error(ex.getLocalizedMessage(), Functions.getCommandId()),
                            ex);
                    return;
                }
            }

            // #6 - Send the results embed to manager
            ResponseDispatcher.commandSucceeded(this,
                    DynamicEmbeds.dynamicEmbed(
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
                            data.getMember().getUser(), commandId
                    )
            );
        } else {
            ResponseDispatcher.commandFailed(this,
                    HelpEmbeds.helpSensitivity(prefix),
                    "User provided an incorrect bound type.");
        }
    }

    @Override
    public GuildMessageReceivedEvent getEvent() {
        return data;
    }

    @Override
    public CommandType getType() {
        return CommandType.SETBOUNDS;
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
