package thermostat.commands.utility;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.embeds.*;
import thermostat.Messages;
import thermostat.commands.Command;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.mySQL.DataSource;
import thermostat.mySQL.PreparedActions;
import thermostat.util.ArgumentParser;
import thermostat.util.MiscellaneousFunctions;
import thermostat.util.entities.Arguments;
import thermostat.util.entities.ReactionMenu;
import thermostat.util.enumeration.CommandType;
import thermostat.util.enumeration.DBActionType;
import thermostat.util.enumeration.MenuType;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static thermostat.util.ArgumentParser.parseArguments;

@SuppressWarnings("ConstantConditions")
public class FilterCommand implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(FilterCommand.class);

    private GuildMessageReceivedEvent data;
    private Map<String, List<String>> parameters;
    private final String prefix;
    private final long commandId;

    public FilterCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) {
        this.commandId = MiscellaneousFunctions.getCommandId();
        this.prefix = prefix;

        try {
            this.parameters = parseArguments(arguments);
        } catch (Exception ex) {
            ResponseDispatcher.commandFailed(this, Embeds.inputError(ex.getLocalizedMessage(), this.commandId), ex);
            return;
        }

        if (ArgumentParser.validateEvent(data)) {
            this.data = data;
        } else {
            ResponseDispatcher.commandFailed(this, Embeds.error("Event was not valid. Please try again."), "Event had a null member.");
            return;
        }

        checkPermissionsAndQueue(this);
    }

    /**
     * Command form: th!filter <true/false> [channel(s)/category(ies)]
     */
    @Override
    public void run() {
        final List<String> channels = parameters.get("c");
        final List<String> onSwitch = parameters.get("-on");
        final List<String> offSwitch = parameters.get("-off");
        final List<String> allSwitch = parameters.get("-all");

        if (offSwitch == null && onSwitch == null) {
            ResponseDispatcher.commandFailed(this,
                    Embeds.expandedHelpFilter(prefix),
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
            Arguments results = ArgumentParser.parseChannelArgument(data.getChannel(), channels);
            channels.clear();

            nonValid = results.nonValid;
            noText = results.noText;
            channels.addAll(results.newArguments);
        }

        // #2 - Filter Target Channels
        try {
            complete = DataSource.execute(conn -> PreparedActions.modifyChannel(conn, type, filter, data.getGuild().getId(), channels));
        } catch (SQLException ex) {
            ResponseDispatcher.commandFailed(this,
                    Embeds.error(ex.getLocalizedMessage(), MiscellaneousFunctions.getCommandId()),
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
                Embeds.dynamicEmbed(
                        Arrays.asList(
                                message,
                                complete.toString(),
                                "Channels that were not valid or found:",
                                nonValid.toString(),
                                "Categories with no Text Channels:",
                                noText.toString()
                        ),
                        data.getMember().getUser(), commandId
                )
        );
    }

    private void unFilterAll() {
        // add reaction & start message listener
        Consumer<Message> consumer = message -> {
            try {
                Messages.addReaction(message, "☑");
                new ReactionMenu(
                        MenuType.UNFILTERALL, data.getMember().getId(),
                        message.getId(), data.getChannel()
                );
                ResponseDispatcher.commandSucceeded(this, Embeds.allRemoved(
                        getEvent().getAuthor().getId(), getEvent().getAuthor().getAvatarUrl(),
                        "filtered"
                ));
            } catch (Exception ex) {
                ResponseDispatcher.commandFailed(this, Embeds.error(ex.getLocalizedMessage(), this.getId()), ex);
            }
        };

        Messages.sendMessage(
                data.getChannel(),
                Embeds.promptEmbed(data.getMember().getUser().getAsTag(), data.getMember().getUser().getAvatarUrl()),
                consumer
        );
    }

    @Override
    public GuildMessageReceivedEvent getEvent() {
        return data;
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
    public long getId() {
        return commandId;
    }
}
