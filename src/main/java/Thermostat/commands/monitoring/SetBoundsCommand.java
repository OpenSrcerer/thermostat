package thermostat.commands.monitoring;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.commands.Command;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.mySQL.DataSource;
import thermostat.Embeds.DynamicEmbeds;
import thermostat.Embeds.ErrorEmbeds;
import thermostat.Embeds.HelpEmbeds;
import thermostat.util.Functions;
import thermostat.util.enumeration.CommandType;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static thermostat.mySQL.DataSource.getAction;
import static thermostat.util.ArgumentParser.hasArguments;
import static thermostat.util.ArgumentParser.parseArguments;
import static thermostat.util.Functions.parseSlowmode;

@SuppressWarnings("ConstantConditions")
public class SetBoundsCommand implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(SetBoundsCommand.class);

    private GuildMessageReceivedEvent data = null;
    private Map<String, List<String>> parameters = null;
    private final String prefix;
    private final long commandId;

    private enum ActionType {
        INVALID, MINIMUM, MAXIMUM
    }

    public SetBoundsCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) {
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
     * Command form: th!setbounds
     * Switches:
     * --max
     * --min
     * -b <bound>
     * -c [channels/categories]
     */
    @Override
    public void run() {
        List<String> channels = parameters.get("c");
        final List<String> bound = parameters.get("b");
        final List<String> minSwitch = parameters.get("-min");
        final List<String> maxSwitch = parameters.get("-max");

        if (minSwitch == null && maxSwitch == null) {
            ResponseDispatcher.commandFailed(this,
                    ErrorEmbeds.inputError("You need to insert a --min/--max switch.", this.commandId),
                    "User did not provide any on/off arguments.");
        } else if (!hasArguments(bound)) {
            ResponseDispatcher.commandFailed(this,
                    ErrorEmbeds.inputError("You need to insert a bound argument.", this.commandId),
                    "User did not provide a bound argument.");
        }

        setBoundsAction(channels, bound.get(0), isSetMaximum(maxSwitch, minSwitch));
    }

    public void setBoundsAction(List<String> channels, String parameterBound, boolean isSetMaximum) {
        StringBuilder nonValid,
                noText,
                minComplete = new StringBuilder(),
                maxComplete = new StringBuilder();

        // value given to us by user to assign as slowmode
        int bound;

        // #2 - Check the [slowmode] argument
        try {
            bound = parseSlowmode(parameterBound);

            if (bound > 21600) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            ResponseDispatcher.commandFailed(this,
                    ErrorEmbeds.inputError("Slowmode value \"" + parameterBound + "\" was incorrect.", commandId),
                    "User provided an incorrect sensitivity value.");
            return;
        }

        // #3 - Parse the optional <channels/categories> argument
        {
            Arguments results = parseChannelArgument(data.getChannel(), channels);

            nonValid = results.nonValid;
            noText = results.noText;
            channels = results.newArguments;
        }
        // args now remains as a list of target channel(s).

        // #4 - Perform the appropriate actions
        int minimumSlow, maximumSlow;

        for (String channel : channels) {
            try {
            {
                addIfNotInDb(data.getGuild().getId(), channel);
                List<Integer> channelSlowmodes = DataSource.queryInts("SELECT MIN_SLOW, MAX_SLOW FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = ?", channel);

                DataSource.execute(conn -> {
                    PreparedStatement statement = conn.prepareStatement("SELECT MIN_SLOW, MAX_SLOW FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = ?");
                    ResultSet rs = statement.executeQuery();

                    rs.next();
                    retVal.add(rs.getInt(1));
                    retVal.add(rs.getInt(2));
                    return
                });

                minimumSlow = channelSlowmodes.get(0);
                maximumSlow = channelSlowmodes.get(1);
            }
                DataSource.DatabaseAction<Void> action = null;

                // -- Setting a Maximum Slowmode --
                // if the argument < the minimum (cannot happen)
                // update both so they're equal
                if (bound < minimumSlow && isSetMaximum) {
                    action = getAction("UPDATE CHANNEL_SETTINGS SET MAX_SLOW = ?, MIN_SLOW = ? WHERE CHANNEL_ID = ?",
                            Integer.toString(bound), Integer.toString(bound), channel);
                    maxComplete.append("<#").append(channel).append("> ");
                }
                // if the argument >= the minimum
                // set maximum normally
                else if (bound >= minimumSlow && isSetMaximum) {
                    action = getAction("UPDATE CHANNEL_SETTINGS SET MAX_SLOW = ? WHERE CHANNEL_ID = ?",
                            Integer.toString(bound), channel);
                    maxComplete.append("<#").append(channel).append("> ");
                }
                // -- Setting a Minimum Slowmode --
                // if the argument > the maximum (cannot happen)
                // update both so they're equal
                else if (bound > maximumSlow) {
                    action = getAction("UPDATE CHANNEL_SETTINGS SET MIN_SLOW = ?, MAX_SLOW = ? WHERE CHANNEL_ID = ?",
                            Integer.toString(bound), Integer.toString(bound), channel);
                    minComplete.append("<#").append(channel).append("> ");
                }
                // if the argument <= the maximum
                // set minimum normally
                else if (bound <= maximumSlow) {
                    action = getAction("UPDATE CHANNEL_SETTINGS SET MIN_SLOW = ? WHERE CHANNEL_ID = ?",
                            Integer.toString(bound), channel);
                    minComplete.append("<#").append(channel).append("> ");
                }

                // Finally execute the action
                DataSource.execute(action);

            } catch (SQLException ex) {
                ResponseDispatcher.commandFailed(this,
                        ErrorEmbeds.error(ex.getLocalizedMessage(), Functions.getCommandId()),
                        ex);
                return;
            }
        }

        // #5 - Send the results embed to dispatch
        ResponseDispatcher.commandSucceeded(this,
                DynamicEmbeds.dynamicEmbed(
                        Arrays.asList(
                                "Channels given a maximum slowmode of " + bound + ":",
                                maxComplete.toString(),
                                "Channels given a minimum slowmode of " + bound + ":",
                                minComplete.toString(),
                                "Channels that were not valid or found:",
                                nonValid.toString(),
                                "Categories with no Text Channels:",
                                noText.toString()
                        ),
                        data.getMember().getUser(), commandId
                )
        );
    }

    /**
     * Get a boolean value representing the action.
     */
    private static boolean isSetMaximum(List<String> maxSwitch, List<String> minSwitch) {
        if (maxSwitch != null) {
            return true;
        } else if (minSwitch != null) {
            return false;
        }

        // Impossible
        throw new IllegalArgumentException("maxSwitch and minSwitch were null.");
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
