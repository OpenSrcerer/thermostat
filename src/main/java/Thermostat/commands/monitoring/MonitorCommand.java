package thermostat.commands.monitoring;

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
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Adds channels to the database provided in
 * db.properties, upon user running the
 * command.
 */
public class MonitorCommand implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(MonitorCommand.class);
    private final CommandData data;

    public MonitorCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) {
        this.data = new CommandData(data, arguments, prefix);

        if (this.data.parameters == null) {
            ResponseDispatcher.commandFailed(
                    this,
                    Embeds.getEmbed(EmbedType.ERR_INPUT, this.data, "Invalid switch usage."),
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
            return;
        }

        monitorAction(
                ArgumentParser.parseChannelArgument(data.event.getChannel(), channels),
                MiscellaneousFunctions.getMonitorValue(onSwitch, offSwitch)
        );
    }

    private void monitorAction(final CommandArguments commandArguments, final int monitor) {
        final StringBuilder complete;

        // Monitor target channels
        try {
            complete = DataSource.execute(conn ->
                    PreparedActions.modifyChannel(
                            conn, DBActionType.MONITOR,
                            monitor, data.event.getGuild().getId(),
                            commandArguments.newArguments
                    )
            );
        } catch (Exception ex) {
            // Issues with the database transaction
            ResponseDispatcher.commandFailed(this, Embeds.getEmbed(EmbedType.ERR, data, ex.getMessage()), ex);
            return;
        }

        // Switch message depending on user action
        final String message;
        if (monitor == 1) {
            message = "Successfully monitored:";
        } else {
            message = "Successfully unmonitored:";
        }

        // Send the results embed to manager
        ResponseDispatcher.commandSucceeded(this,
                Embeds.getEmbed(EmbedType.DYNAMIC, data,
                        Arrays.asList(
                                message,
                                complete.toString(),
                                "Channels that were not valid or found:",
                                commandArguments.nonValid.toString(),
                                "Categories with no Text Channels:",
                                commandArguments.noText.toString()
                        )
                )
        );
    }

    private void unMonitorAll() {
        // add reaction & start message listener
        Consumer<Message> consumer = message -> {
            try {
                Messages.addReaction(message, "☑");
                MenuDispatcher.addMenu(MenuType.UNMONITORALL, message.getId(), this);
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
