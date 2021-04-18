package thermostat.commands.monitoring;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.commands.Command;
import thermostat.dispatchers.CommandDispatcher;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.embeds.Embeds;
import thermostat.mySQL.DataSource;
import thermostat.mySQL.PreparedActions;
import thermostat.util.ArgumentParser;
import thermostat.util.entities.CommandArguments;
import thermostat.util.entities.CommandContext;
import thermostat.util.enumeration.CommandType;
import thermostat.util.enumeration.EmbedType;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static thermostat.util.ArgumentParser.hasArguments;
import static thermostat.util.ArgumentParser.parseSlowmode;

/**
 * Sets the upper and lower bounds for the slowmode of the channel.
 */
public class SetBoundsCommand implements Command {
    /**
     * Logger for this class.
     */
    private static final Logger lgr = LoggerFactory.getLogger(SetBoundsCommand.class);

    /**
     * Data for this command.
     */
    private final CommandContext data;

    public SetBoundsCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) {
        this.data = new CommandContext(data, arguments, prefix);

        if (this.data.parameters == null) {
            ResponseDispatcher.commandFailed(
                    this,
                    Embeds.getEmbed(EmbedType.HELP_SETBOUNDS, this.data),
                    "Bad Arguments / None were provided.");
            return;
        }

        CommandDispatcher.checkPermissionsAndQueue(this);
    }

    /**
     * Command form: th!setbounds
     * Switches:
     * -m [bound]
     * -M [bound]
     * -c [channels/categories]
     */
    @Override
    public void run() {
        final List<String> channels = data.parameters.get("c");
        final List<String> minSwitch = data.parameters.get("m");
        final List<String> maxSwitch = data.parameters.get("M");

        if (!hasArguments(minSwitch) && !hasArguments(maxSwitch)) {
            ResponseDispatcher.commandFailed(this,
                    Embeds.getEmbed(EmbedType.HELP_SETBOUNDS, data),
                    "User did not provide a --min/--max switch.");
            return;
        }

        // Check the [slowmode] argument
        int minBound = -1, maxBound = -1;
        try {
            if (hasArguments(minSwitch)) {
                minBound = parseSlowmode(minSwitch.get(0));
            }

            if (hasArguments(maxSwitch)) {
                maxBound = parseSlowmode(maxSwitch.get(0));
            }

            if (minBound > 21600 || maxBound > 21600) {
                throw new NumberFormatException("One of the bounds exceeded the maximum Discord value.");
            }

            if (hasArguments(minSwitch, maxSwitch) && minBound > maxBound) {
                throw new NumberFormatException("The minimum bound must be smaller or equal to the maximum bound.");
            }
        } catch (NumberFormatException ex) {
            ResponseDispatcher.commandFailed(this,
                    Embeds.getEmbed(EmbedType.ERR_INPUT, data, ex.getMessage()),
                    "Incorrect sensitivity value.");
            return;
        }

        setBoundsAction(ArgumentParser.parseChannelArgument(data.event.getChannel(), channels), minBound, maxBound);
    }

    public void setBoundsAction(final CommandArguments arguments, final int minBound, final int maxBound) {
        final StringBuilder complete;

        // Perform database changes
        try {
            complete = DataSource.demand(conn -> PreparedActions.modifyBounds(
                    conn, data.event.getGuild().getId(),
                    minBound, maxBound, arguments.channels
            ));
        } catch (SQLException ex) {
            ResponseDispatcher.commandFailed(this,
                    Embeds.getEmbed(EmbedType.ERR, data, ex.getMessage()),
                    ex);
            return;
        }

        // Send the results embed to dispatch
        ResponseDispatcher.commandSucceeded(this,
                Embeds.getEmbed(EmbedType.DYNAMIC, data,
                        Arrays.asList(
                                "Successfully updated bounds on:",
                                complete.toString(),
                                "Channels that were not valid or found:",
                                arguments.nonValid.toString(),
                                "Categories with no Text Channels:",
                                arguments.noText.toString()
                        )
                )
        );
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
    public CommandContext getData() {
        return data;
    }
}
