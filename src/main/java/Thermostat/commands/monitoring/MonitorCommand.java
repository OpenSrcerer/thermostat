package thermostat.commands.monitoring;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.embeds.*;
import thermostat.Messages;
import thermostat.commands.Command;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.mySQL.PreparedActions;
import thermostat.mySQL.DataSource;
import thermostat.util.ArgumentParser;
import thermostat.util.MiscellaneousFunctions;
import thermostat.util.entities.Arguments;
import thermostat.util.entities.CommandData;
import thermostat.util.entities.ReactionMenu;
import thermostat.util.enumeration.CommandType;
import thermostat.util.enumeration.DBActionType;
import thermostat.util.enumeration.EmbedType;
import thermostat.util.enumeration.MenuType;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Adds channels to the database provided in
 * db.properties, upon user running the
 * command.
 */
@SuppressWarnings("ConstantConditions")
public class MonitorCommand implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(MonitorCommand.class);
    private final CommandData data;

    public MonitorCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) {
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
     * Command form: th!monitor
     * Switches:
     * --on
     * --off
     * --all
     * -c [channels/categories]
     */
    @Override
    public void run() {
        final List<String> channels = data.parameters.get("c");
        final List<String> onSwitch = data.parameters.get("-on");
        final List<String> offSwitch = data.parameters.get("-off");
        final List<String> allSwitch = data.parameters.get("-all");

        if (offSwitch == null && onSwitch == null) {
            ResponseDispatcher.commandFailed(this, Embeds.getEmbed(EmbedType.HELP_MONITOR, data));
            return;
        } else if (allSwitch != null) {
            unMonitorAll();
        }

        monitorAction(channels, MiscellaneousFunctions.getMonitorValue(onSwitch, offSwitch));
    }

    private void monitorAction(final List<String> channels, final int monitor) {
        final StringBuilder nonValid, noText, complete;

        // #1 - Retrieve target channels
        {
            Arguments results = ArgumentParser.parseChannelArgument(data.event.getChannel(), channels);
            // Channels.clear throws exception
            // Move Arguments to a separate function??
            channels.clear();

            nonValid = results.nonValid;
            noText = results.noText;
            channels.addAll(results.newArguments);
        }

        // #2 - Monitor target channels
        try {
            complete = DataSource.execute(conn -> PreparedActions.modifyChannel(conn, DBActionType.MONITOR, monitor, data.event.getGuild().getId(), channels));
        } catch (Exception ex) {
            // Issues with the database transaction
            ResponseDispatcher.commandFailed(this, Embeds.getEmbed(EmbedType.ERR, data, ex.getMessage()), ex);
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

    private void unMonitorAll() {
        // add reaction & start message listener
        Consumer<Message> consumer = message -> {
            try {
                Messages.addReaction(message, "☑");
                new ReactionMenu(
                        MenuType.UNMONITORALL, data.event.getMember().getId(),
                        message.getId(), data.event.getChannel()
                );
                ResponseDispatcher.commandSucceeded(
                        this, Embeds.getEmbed(EmbedType.ALL_REMOVED, data,
                                "monitored"
                        )
                );
            } catch (Exception ex) {
                ResponseDispatcher.commandFailed(this,
                        Embeds.getEmbed(EmbedType.ERR, data, ex.getMessage()), ex
                );
            }
        };

        Messages.sendMessage(data.event.getChannel(),
                Embeds.getEmbed(EmbedType.PROMPT, data),
                consumer
        );
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
    public CommandData getData() {
        return data;
    }
}
