package thermostat.commands.utility;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.Messages;
import thermostat.commands.Command;
import thermostat.dispatchers.MenuDispatcher;
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
import java.util.function.Consumer;

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

        checkPermissionsAndQueue(this);
    }

    /**
     * Command form: th!filter <true/false> [channel(s)/category(ies)]
     */
    @Override
    public void run() {
        final List<String> channels = data.parameters.get("c");
        final List<String> onSwitch = data.parameters.get("-on");
        final List<String> offSwitch = data.parameters.get("-off");
        final List<String> allSwitch = data.parameters.get("-all");

        if (offSwitch == null && onSwitch == null) {
            ResponseDispatcher.commandFailed(this,
                    Embeds.getEmbed(EmbedType.HELP_FILTER, data),
                    "User did not provide arguments.");
        } else if (allSwitch != null) {
            unFilterAll();
        }

        filterAction(channels, MiscellaneousFunctions.getMonitorValue(onSwitch, offSwitch));
    }

    private void filterAction(final List<String> channels, final int filter) {
        DBActionType type = filter == 1 ? DBActionType.FILTER : DBActionType.UNFILTER;
        final StringBuilder nonValid,
                noText,
                complete;

        // #1 - Parse Target Channels
        {
            CommandArguments results = ArgumentParser.parseChannelArgument(data.event.getChannel(), channels);
            channels.clear();

            nonValid = results.nonValid;
            noText = results.noText;
            channels.addAll(results.newArguments);
        }

        // #2 - Filter Target Channels
        try {
            complete = DataSource.execute(conn -> PreparedActions.modifyChannel(conn, type, filter, data.event.getGuild().getId(), channels));
        } catch (SQLException ex) {
            ResponseDispatcher.commandFailed(this,
                    Embeds.getEmbed(EmbedType.ERR, data, ex.getMessage()),
                    ex);
            return;
        }

        // #3 - Switch message depending on user action
        final String message;
        if (filter == 1) {
            message = "Enabled filtering on:";
        } else {
            message = "Disabled filtering on:";
        }

        // #4 - Send the results embed to manager
        ResponseDispatcher.commandSucceeded(this,
                Embeds.getEmbed(EmbedType.DYNAMIC, data,
                        Arrays.asList(
                                message,
                                complete.toString(),
                                "Channels that were not valid or found:",
                                nonValid.toString(),
                                "Categories with no Text Channels:",
                                noText.toString()
                        )
                )
        );
    }

    private void unFilterAll() {
        // add reaction & start message listener
        Consumer<Message> consumer = message -> {
            try {
                Messages.addReaction(message, "☑");
                MenuDispatcher.addMenu(MenuType.UNFILTERALL, message.getId(), this);
            } catch (Exception ex) {
                ResponseDispatcher.commandFailed(this,
                        Embeds.getEmbed(EmbedType.ERR, data, ex.getMessage()),
                        ex);
            }
        };

        Messages.sendMessage(
                data.event.getChannel(),
                Embeds.getEmbed(EmbedType.PROMPT, data),
                consumer
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
