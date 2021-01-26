package thermostat.commands.monitoring;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.embeds.DynamicEmbeds;
import thermostat.embeds.ErrorEmbeds;
import thermostat.embeds.GenericEmbeds;
import thermostat.embeds.HelpEmbeds;
import thermostat.Messages;
import thermostat.commands.Command;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.mySQL.PreparedActions;
import thermostat.mySQL.DataSource;
import thermostat.util.ArgumentParser;
import thermostat.util.MiscellaneousFunctions;
import thermostat.util.entities.ReactionMenu;
import thermostat.util.enumeration.CommandType;
import thermostat.util.enumeration.DBActionType;
import thermostat.util.enumeration.MenuType;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static thermostat.util.ArgumentParser.parseArguments;

/**
 * Adds channels to the database provided in
 * db.properties, upon user running the
 * command.
 */
@SuppressWarnings("ConstantConditions")
public class MonitorCommand implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(MonitorCommand.class);

    private GuildMessageReceivedEvent data;
    private Map<String, List<String>> parameters;
    private final String prefix;
    private final long commandId;

    public MonitorCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) {
        this.commandId = MiscellaneousFunctions.getCommandId();
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
     * Command form: th!monitor
     * Switches:
     * --on
     * --off
     * --all
     * -c [channels/categories]
     */
    @Override
    public void run() {
        final List<String> channels = parameters.get("c");
        final List<String> onSwitch = parameters.get("-on");
        final List<String> offSwitch = parameters.get("-off");
        final List<String> allSwitch = parameters.get("-all");

        if (offSwitch == null && onSwitch == null) {
            ResponseDispatcher.commandFailed(this,
                    HelpEmbeds.expandedHelpMonitor(prefix));
        } else if (allSwitch != null) {
            unMonitorAll();
        }

        monitorAction(channels, MiscellaneousFunctions.getMonitorValue(onSwitch, offSwitch));
    }

    private void monitorAction(final List<String> channels, final int monitor) {
        final StringBuilder nonValid, noText, complete;

        // #1 - Retrieve target channels
        {
            ArgumentParser.Arguments results = ArgumentParser.parseChannelArgument(data.getChannel(), channels);
            channels.clear();

            nonValid = results.nonValid;
            noText = results.noText;
            channels.addAll(results.newArguments);
        }

        // #2 - Monitor target channels
        try {
            complete = DataSource.execute(conn -> PreparedActions.modifyChannel(conn, DBActionType.MONITOR, monitor, data.getGuild().getId(), channels));
        } catch (Exception ex) {
            // Issues with the database transaction
            ResponseDispatcher.commandFailed(this, ErrorEmbeds.error(ex.getLocalizedMessage(), commandId), ex);
            return;
        }

        // #3 - Switch message depending on user action
        final String message;
        if (monitor == 1) {
            message = "Successfully monitored:";
        } else {
            message = "Successfully unmonitored:";
        }

        // #4 - Send the results embed to manager
        ResponseDispatcher.commandSucceeded(this,
                DynamicEmbeds.dynamicEmbed(
                        Arrays.asList(
                                message,
                                complete.toString(),
                                "Channels that were not valid or found:",
                                nonValid.toString(),
                                "Categories with no Text Channels:",
                                noText.toString()
                        ), data.getMember().getUser(), commandId
                )
        );
    }

    private void unMonitorAll() {
        // add reaction & start message listener
        Consumer<Message> consumer = message -> {
            try {
                Messages.addReaction(message, "☑");
                new ReactionMenu(
                        MenuType.UNMONITORALL, data.getMember().getId(),
                        message.getId(), data.getChannel()
                );
                ResponseDispatcher.commandSucceeded(this, GenericEmbeds.allRemoved(
                        getEvent().getAuthor().getId(), getEvent().getAuthor().getAvatarUrl(),
                        "monitored"
                ));
            } catch (Exception ex) {
                ResponseDispatcher.commandFailed(this, ErrorEmbeds.error(ex.getLocalizedMessage(), this.getId()), ex);
            }
        };

        Messages.sendMessage(
                data.getChannel(),
                GenericEmbeds.promptEmbed(data.getMember().getUser().getAsTag(), data.getMember().getUser().getAvatarUrl()),
                consumer
        );
    }

    @Override
    public GuildMessageReceivedEvent getEvent() {
        return data;
    }

    @Override
    public CommandType getType() {
        return CommandType.MONITOR;
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
