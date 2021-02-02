package thermostat.commands;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.Thermostat;
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
import thermostat.commands.utility.WordFilter;
import thermostat.mySQL.DataSource;
import thermostat.mySQL.PreparedActions;
import thermostat.util.ArgumentParser;
import thermostat.util.GuildCache;
import thermostat.util.enumeration.CommandType;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

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
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        if (!ArgumentParser.validateEvent(event)) {
            return;
        }

        String prefix = GuildCache.getPrefix(event.getGuild().getId());
        // gets given arguments and passes them to a list
        ArrayList<String> arguments = new ArrayList<>(Arrays.asList(event.getMessage().getContentRaw().split("\\s+")));

        try {
            DataSource.execute(conn -> {
                DataSource.syncDatabase(conn, event.getGuild().getId(), event.getChannel().getId());
                // Make sure to implement cache checks before communicating with the database.
                // TBD - Bookmark
                // Checks for whether the channel has the offensive-word filter activated.
                if (PreparedActions.isFiltered(conn, event.getGuild().getId(), event.getChannel().getId())) {
                    new WordFilter(event);
                }
                return null;
            });
        } catch (SQLException ex) {
            lgr.warn("Database synchronization failed:", ex);
        }

        if (
                arguments.get(0).equalsIgnoreCase(prefix + CommandType.CHART.getAlias1()) ||
                        arguments.get(0).equalsIgnoreCase(prefix + CommandType.CHART.getAlias2())
        ) {
            arguments.remove(0);
            new ChartCommand(event, arguments, prefix);
        }

        else if (
                arguments.get(0).equalsIgnoreCase(prefix + CommandType.FILTER.getAlias1()) ||
                        arguments.get(0).equalsIgnoreCase(prefix + CommandType.FILTER.getAlias2())
        ) {
            arguments.remove(0);
            new FilterCommand(event, arguments, prefix);
        }

        else if (
                arguments.get(0).equalsIgnoreCase(prefix + CommandType.GETMONITOR.getAlias1()) ||
                        arguments.get(0).equalsIgnoreCase(prefix + CommandType.GETMONITOR.getAlias2())
        ) {
            arguments.remove(0);
            new GetMonitorCommand(event);
        }

        else if (
                arguments.get(0).equalsIgnoreCase(prefix + CommandType.SETTINGS.getAlias1()) ||
                        arguments.get(0).equalsIgnoreCase(prefix + CommandType.SETTINGS.getAlias2())
        ) {
            arguments.remove(0);
            new SettingsCommand(event, arguments, prefix);
        }

        else if (
                arguments.get(0).equalsIgnoreCase(prefix + CommandType.MONITOR.getAlias1()) ||
                        arguments.get(0).equalsIgnoreCase(prefix + CommandType.MONITOR.getAlias2())
        ) {
            arguments.remove(0);
            new MonitorCommand(event, arguments, prefix);
        }

        else if (
                arguments.get(0).equalsIgnoreCase(prefix + CommandType.SENSITIVITY.getAlias1()) ||
                        arguments.get(0).equalsIgnoreCase(prefix + CommandType.SENSITIVITY.getAlias2())
        ) {
            arguments.remove(0);
            new SensitivityCommand(event, arguments, prefix);
        }

        else if (
                arguments.get(0).equalsIgnoreCase(prefix + CommandType.SETBOUNDS.getAlias1()) ||
                        arguments.get(0).equalsIgnoreCase(prefix + CommandType.SETBOUNDS.getAlias2())
        ) {
            arguments.remove(0);
            new SetBoundsCommand(event, arguments, prefix);
        }

        else if (
                arguments.get(0).equalsIgnoreCase(prefix + CommandType.INFO.getAlias1()) ||
                        arguments.get(0).equalsIgnoreCase(prefix + CommandType.INFO.getAlias2()) ||
                        arguments.get(0).equalsIgnoreCase(prefix + CommandType.HELP.getAlias1()) ||
                        arguments.get(0).equalsIgnoreCase(prefix + CommandType.HELP.getAlias2())
        ) {
            arguments.remove(0);
            new InfoCommand(event, arguments, prefix);
        }

        else if (
                arguments.get(0).equalsIgnoreCase(prefix + CommandType.INVITE.getAlias1()) ||
                        arguments.get(0).equalsIgnoreCase(prefix + CommandType.INVITE.getAlias2())
        ) {
            arguments.remove(0);
            new InviteCommand(event, prefix);
        }

        // PrefixCommand has two checks, one for th!prefix
        else if (
                arguments.get(0).equalsIgnoreCase(prefix + CommandType.PREFIX.getAlias1()) ||
                        arguments.get(0).equalsIgnoreCase(prefix + CommandType.PREFIX.getAlias2())
        ) {
            arguments.remove(0);
            new PrefixCommand(event, arguments, prefix);
        }

        // one for @Thermostat
        else if (
                (arguments.get(0).equalsIgnoreCase("<@!" + Thermostat.thermo.getSelfUser().getId() + ">"))
        ) {
            arguments.remove(0);
            new PrefixCommand(event, arguments, prefix);
        }

        else if (
                arguments.get(0).equalsIgnoreCase(prefix + CommandType.VOTE.getAlias1()) ||
                        arguments.get(0).equalsIgnoreCase(prefix + CommandType.VOTE.getAlias2())
        ) {
            arguments.remove(0);
            new VoteCommand(event, prefix);
        }
    }
}
