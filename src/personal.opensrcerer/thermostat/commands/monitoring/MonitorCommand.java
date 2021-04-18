package thermostat.commands.monitoring;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.commands.Command;
import thermostat.dispatchers.CommandDispatcher;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.embeds.Embeds;
import thermostat.mySQL.DataSource;
import thermostat.mySQL.PreparedActions;
import thermostat.util.ArgumentParser;
import thermostat.util.MiscellaneousFunctions;
import thermostat.util.RestActions;
import thermostat.util.entities.CommandArguments;
import thermostat.util.entities.CommandContext;
import thermostat.util.enumeration.CommandType;
import thermostat.util.enumeration.DBActionType;
import thermostat.util.enumeration.EmbedType;
import thermostat.util.enumeration.MenuType;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

/**
 * Adds channels to the database provided in
 * db.properties, upon user running the
 * command.
 */
public class MonitorCommand implements Command {
    /**
     * Logger for this class.
     */
    private static final Logger lgr = LoggerFactory.getLogger(MonitorCommand.class);

    /**
     * Logger for this class.
     */
    private final CommandContext data;

    public MonitorCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) {
        this.data = new CommandContext(data, arguments, prefix);

        if (this.data.parameters == null) {
            ResponseDispatcher.commandFailed(
                    this,
                    Embeds.getEmbed(EmbedType.ERR_INPUT, this.data, "Invalid switch usage."),
                    "Bad arguments.");
            return;
        }

        CommandDispatcher.checkPermissionsAndQueue(this);
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
        }

        if (allSwitch != null) {
            ResponseDispatcher.commandSucceeded(this, monitorAll(onSwitch != null));
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
            complete = DataSource.demand(conn ->
                    PreparedActions.modifyChannel(
                            conn, DBActionType.MONITOR,
                            monitor, data.event.getGuild().getId(),
                            commandArguments.channels
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

    @CheckReturnValue
    private RestAction<Void> monitorAll(final boolean monitor) {
        MenuType type = (monitor) ? MenuType.MONITORALL : MenuType.UNMONITORALL;

        return RestActions.sendMessage(data.event.getChannel(), Embeds.getEmbed(EmbedType.PROMPT, data))
                .flatMap(message -> MiscellaneousFunctions.addNewMenu(message, type, this));
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
    public CommandContext getData() {
        return data;
    }
}
