package thermostat.commands;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.commands.informational.ChartCommand;
import thermostat.commands.informational.GetMonitorCommand;
import thermostat.commands.informational.InfoCommand;
import thermostat.commands.informational.SettingsCommand;
import thermostat.commands.monitoring.MonitorCommand;
import thermostat.commands.monitoring.SensitivityCommand;
import thermostat.commands.monitoring.SetBoundsCommand;
import thermostat.commands.other.InviteCommand;
import thermostat.commands.other.PrefixCommand;
import thermostat.commands.other.VoteCommand;
import thermostat.commands.utility.FilterCommand;
import thermostat.mySQL.PreparedActions;
import thermostat.util.ArgumentParser;
import thermostat.util.GuildCache;
import thermostat.util.enumeration.CommandType;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Adapter for any sort of action based on the
 * GuildMessageReactionEvent event class.
 */
public final class CommandTrigger extends ListenerAdapter {
    /**
     * Logger for this class.
     */
    private static final Logger lgr = LoggerFactory.getLogger(CommandTrigger.class);

    /**
     * Trigger a new command when a message calling thermostat gets sent.
     * @param event Event that contains sent message.
     */
    @Override
    public void onGuildMessageReceived(@Nonnull final GuildMessageReceivedEvent event) {
        if (!ArgumentParser.validateEvent(event)) { // If the event is invalid or the member is a bot, don't reply
            return;
        }

        // Get the arguments from the event and split them appropriately.
        ArrayList<String> arguments = new ArrayList<>(Arrays.asList(event.getMessage().getContentRaw().split("\\s+")));
        String prefix = GuildCache.getPrefix(event.getGuild().getId()); // Retrieves the prefix for this Guild
        CommandType type = ArgumentParser.getCommandByInit(arguments, prefix); // Get the type of the Command & prep arguments.
        PreparedActions.performGMREActions(event); // Run Database sync & Word Filter

        if (type == null) {
            return;
        }

        forwardCommand(type, event, arguments, prefix); // Create the command and queue it
    }

    /**
     * Matches a given CommandType with a Command and creates one.
     * @param type Type of Command.
     * @param event Event to be used for the creation of the Command.
     * @param arguments Arguments to use in the Command.
     * @param prefix Prefix of the Guild that called this command.
     */
    private static void forwardCommand(final CommandType type, final GuildMessageReceivedEvent event,
                                       final List<String> arguments, final String prefix) {
        switch (type) {
            // Informational
            case CHART -> new ChartCommand(event, arguments, prefix);
            case GETMONITOR -> new GetMonitorCommand(event);
            case INFO, HELP -> new InfoCommand(event, arguments, prefix);
            case SETTINGS -> new SettingsCommand(event, arguments, prefix);
            // Moderation (TBA)
            /*case BAN -> ;
            case KICK -> ;
            case MUTE -> ;
            case PURGE -> ;*/
            // Monitoring
            case MONITOR -> new MonitorCommand(event, arguments, prefix);
            case SENSITIVITY -> new SensitivityCommand(event, arguments, prefix);
            case SETBOUNDS -> new SetBoundsCommand(event, arguments, prefix);
            // Other
            case INVITE -> new InviteCommand(event, prefix);
            case PREFIX -> new PrefixCommand(event, arguments, prefix);
            case VOTE -> new VoteCommand(event, prefix);
            // Utility
            case FILTER -> new FilterCommand(event, arguments, prefix);
            default -> lgr.warn("No commands matched.");
        }
    }
}
