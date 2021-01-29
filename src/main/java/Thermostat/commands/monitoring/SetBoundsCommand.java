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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static thermostat.util.ArgumentParser.hasArguments;
import static thermostat.util.ArgumentParser.parseSlowmode;

public class SetBoundsCommand implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(SetBoundsCommand.class);
    private final CommandData data;

    public SetBoundsCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) {
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
     * Command form: th!setbounds
     * Switches:
     * --max
     * --min
     * -b <bound>
     * -c [channels/categories]
     */
    @Override
    public void run() {
        final List<String> channels = data.parameters.get("c");
        final List<String> boundParameter = data.parameters.get("b");
        final List<String> minSwitch = data.parameters.get("-min");
        final List<String> maxSwitch = data.parameters.get("-max");

        if (minSwitch == null && maxSwitch == null) {
            ResponseDispatcher.commandFailed(this,
                    Embeds.getEmbed(EmbedType.ERR_INPUT, data, "You need to insert a --min/--max switch."),
                    "User did not provide a --min/--max switch.");
        }

        if (!hasArguments(boundParameter)) {
            ResponseDispatcher.commandFailed(this,
                    Embeds.getEmbed(EmbedType.ERR_INPUT, data, "You need to insert a bound argument."),
                    "User did not provide a bound argument.");
        }

        // #2 - Check the [slowmode] argument
        int bound;
        try {
            bound = parseSlowmode(boundParameter.get(0));

            if (bound > 21600) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            ResponseDispatcher.commandFailed(this,
                    Embeds.getEmbed(EmbedType.ERR_INPUT, data, "Slowmode value \"" + boundParameter.get(0) + "\" was incorrect."),
                    "User provided an incorrect sensitivity value.");
            return;
        }

        setBoundsAction(channels, bound, isSetMaximum(maxSwitch, minSwitch));
    }

    public void setBoundsAction(final List<String> channels, final int bound, final boolean isSetMaximum) {
        StringBuilder nonValid,
                noText,
                bothComplete = new StringBuilder(),
                minComplete = new StringBuilder(),
                maxComplete = new StringBuilder();

        // #1 - Retrieve target channels
        {
            Arguments results = ArgumentParser.parseChannelArgument(data.event.getChannel(), channels);
            channels.clear();

            nonValid = results.nonValid;
            noText = results.noText;
            channels.addAll(results.newArguments);
        }

        // #2 - Perform database changes
        try {
            DataSource.execute(conn -> {
                boolean threeArguments;
                int minimumSlow, maximumSlow;
                StringBuilder sql = new StringBuilder();

                for (String channel : channels) {
                    threeArguments = false;

                    {
                        PreparedStatement statement = conn.prepareStatement("SELECT MIN_SLOW, MAX_SLOW FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = ?");
                        ResultSet rs = statement.executeQuery();
                        rs.next();

                        minimumSlow = rs.getInt(1);
                        maximumSlow = rs.getInt(2);
                    }

                    PreparedStatement statement;

                    // ----- WARNING -----
                    // SQL is inserted in specific position
                    // Heads up if changing query
                    sql.setLength(0);
                    sql.append("UPDATE CHANNEL_SETTINGS SET  WHERE CHANNEL_ID = ?;");

                    // if the argument < the minimum OR argument > the maximum
                    // update both so they're equal
                    if (bound < minimumSlow && isSetMaximum || bound > maximumSlow && !isSetMaximum) {
                        threeArguments = true;
                        sql.insert(28, "MIN_SLOW = ?, MAX_SLOW = ?");
                        bothComplete.append("<#").append(channel).append("> ");
                    }
                    // if the argument >= the minimum
                    // set maximum normally
                    else if (bound >= minimumSlow && isSetMaximum) {
                        sql.insert(28, "MAX_SLOW = ?");
                        maxComplete.append("<#").append(channel).append("> ");
                    }
                    // if the argument <= the maximum
                    // set minimum normally
                    else if (bound <= maximumSlow) {
                        sql.insert(28, "MIN_SLOW = ?");
                        minComplete.append("<#").append(channel).append("> ");
                    }

                    // Prepare the statement using the compiled SQL
                    statement = conn.prepareStatement(sql.toString());

                    // Add arguments to the SQL statement
                    statement.setInt(1, bound);

                    if (threeArguments) {
                        statement.setInt(2, bound);
                        statement.setString(3, channel);
                    } else {
                        statement.setString(2, channel);
                    }

                    // Finally execute the action
                    statement.executeUpdate();
                }
                return null;
            });
        } catch (SQLException ex) {
            ResponseDispatcher.commandFailed(this,
                    Embeds.getEmbed(EmbedType.ERR, data, ex.getMessage()),
                    ex);
            return;
        }

        // #3 - Send the results embed to dispatch
        ResponseDispatcher.commandSucceeded(this,
                Embeds.getEmbed(EmbedType.DYNAMIC, data,
                        Arrays.asList(
                                "Channels that had both slowmode bounds changed to " + bound + ":",
                                bothComplete.toString(),
                                "Channels given a maximum slowmode of " + bound + ":",
                                maxComplete.toString(),
                                "Channels given a minimum slowmode of " + bound + ":",
                                minComplete.toString(),
                                "Channels that were not valid or found:",
                                nonValid.toString(),
                                "Categories with no Text Channels:",
                                noText.toString()
                        )
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

        // Impossible.
        throw new IllegalArgumentException("maxSwitch and minSwitch were null.");
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
    public CommandData getData() {
        return data;
    }
}
