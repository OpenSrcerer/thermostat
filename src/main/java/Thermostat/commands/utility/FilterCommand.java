package thermostat.commands.utility;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.Messages;
import thermostat.commands.Command;
import thermostat.dispatchers.CommandDispatcher;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.embeds.Embeds;
import thermostat.mySQL.DataSource;
import thermostat.mySQL.PreparedActions;
import thermostat.util.ArgumentParser;
import thermostat.util.MiscellaneousFunctions;
import thermostat.util.entities.CommandArguments;
import thermostat.util.entities.CommandData;
import thermostat.util.enumeration.CommandType;
import thermostat.util.enumeration.DBActionType;
import thermostat.util.enumeration.EmbedType;
import thermostat.util.enumeration.MenuType;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class FilterCommand implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(FilterCommand.class);
    private final CommandData data;

    public FilterCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) {
        this.data = new CommandData(data, arguments, prefix);

        if (this.data.parameters == null) {
            ResponseDispatcher.commandFailed(
                    this,
                    Embeds.getEmbed(EmbedType.ERR, this.data),
                    "Bad arguments.");
            return;
        }

        CommandDispatcher.checkPermissionsAndQueue(this);
    }

    /**
     * Command form: th!filter
     */
    @Override
    public void run() {
        final List<String> channels = data.parameters.get("c");
        final List<String> onSwitch = data.parameters.get("-on");
        final List<String> offSwitch = data.parameters.get("-off");
        final List<String> allSwitch = data.parameters.get("-all");

        if (offSwitch == null && onSwitch == null) {
            ResponseDispatcher.commandFailed(this, Embeds.getEmbed(EmbedType.HELP_FILTER, data),
                    "User did not provide arguments.");
            return;
        }

        if (allSwitch != null) {
            filterAll(onSwitch != null);
            return;
        }

        filterAction(
                ArgumentParser.parseChannelArgument(data.event.getChannel(), channels),
                MiscellaneousFunctions.getMonitorValue(onSwitch, offSwitch)
        );
    }

    private void filterAction(final CommandArguments arguments, final int filter) {
        DBActionType type = filter == 1 ? DBActionType.FILTER : DBActionType.UNFILTER;
        final StringBuilder complete;

        // Filter Target Channels
        try {
            complete = DataSource.demand(conn -> PreparedActions.modifyChannel(
                    conn, type, filter, data.event.getGuild().getId(), arguments.channels)
            );
        } catch (SQLException ex) {
            ResponseDispatcher.commandFailed(this,
                    Embeds.getEmbed(EmbedType.ERR, data, ex.getMessage()),
                    ex);
            return;
        }

        // Switch message depending on user action
        final String message;
        if (filter == 1) {
            message = "Enabled filtering on:";
        } else {
            message = "Disabled filtering on:";
        }

        // Send the results embed to dispatch
        ResponseDispatcher.commandSucceeded(this,
                Embeds.getEmbed(EmbedType.DYNAMIC, data,
                        Arrays.asList(
                                message,
                                complete.toString(),
                                "Channels that were not valid or found:",
                                arguments.nonValid.toString(),
                                "Categories with no Text Channels:",
                                arguments.noText.toString()
                        )
                )
        );
    }

    private void filterAll(final boolean filter) {
        MenuType type = (filter) ? MenuType.FILTERALL : MenuType.UNFILTERALL;

        // Add a new Prompt menu,
        Messages.sendMessage(
                data.event.getChannel(),
                Embeds.getEmbed(EmbedType.PROMPT, data),
                MiscellaneousFunctions.addNewMenu(type, this)
        );
    }

    @Override
    public CommandType getType() {
        return CommandType.FILTER;
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
