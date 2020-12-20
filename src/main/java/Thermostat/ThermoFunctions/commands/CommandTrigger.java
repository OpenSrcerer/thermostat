package thermostat.thermoFunctions.commands;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import thermostat.mySQL.DataSource;
import thermostat.thermoFunctions.Functions;
import thermostat.thermoFunctions.commands.informational.ChartCommand;
import thermostat.thermoFunctions.commands.informational.GetMonitorCommand;
import thermostat.thermoFunctions.commands.informational.SettingsCommand;
import thermostat.thermoFunctions.commands.monitoring.*;
import thermostat.thermoFunctions.commands.other.InfoCommand;
import thermostat.thermoFunctions.commands.other.InviteCommand;
import thermostat.thermoFunctions.commands.other.PrefixCommand;
import thermostat.thermoFunctions.commands.other.VoteCommand;
import thermostat.thermoFunctions.commands.utility.FilterCommand;
import thermostat.thermoFunctions.entities.CommandType;
import thermostat.thermoFunctions.commands.utility.WordFilterCommand;
import thermostat.Thermostat;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Adapter for any sort of action based on the
 * GuildMessageReactionEvent event class.
 */
public final class CommandTrigger extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {

        String prefix = DataSource.queryString("SELECT GUILD_PREFIX FROM GUILDS WHERE GUILD_ID = ?", event.getGuild().getId());
        if (prefix == null) {
            prefix = Thermostat.prefix;
        }

        // gets given arguments and passes them to a list
        ArrayList<String> arguments = new ArrayList<>(Arrays.asList(event.getMessage().getContentRaw().split("\\s+")));
        Functions.checkGuildAndChannelThenSet(event.getGuild().getId(), event.getChannel().getId());

        // Checks for whether the channel has the offensive-word filter activated.
        if (DataSource.queryBool("SELECT FILTERED FROM CHANNEL_SETTINGS JOIN CHANNELS ON " +
                "(CHANNELS.CHANNEL_ID = CHANNEL_SETTINGS.CHANNEL_ID) WHERE CHANNELS.GUILD_ID = ? " +
                "AND CHANNELS.CHANNEL_ID = ?",
                Arrays.asList(event.getGuild().getId(), event.getChannel().getId()))) {
            new WordFilterCommand(event);
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
            new InviteCommand(event, arguments, prefix);
        }

        else if (
                arguments.get(0).equalsIgnoreCase(prefix + CommandType.PREFIX.getAlias1()) ||
                        arguments.get(0).equalsIgnoreCase(prefix + CommandType.PREFIX.getAlias2())
        ) {
            arguments.remove(0);
            new PrefixCommand(event, arguments, prefix);
        }

        else if (
                (arguments.get(0).equalsIgnoreCase("<@!" + Thermostat.thermo.getSelfUser().getId() + ">"))
        ) {
            if (arguments.size() > 1)
                if (arguments.get(1).equalsIgnoreCase(CommandType.PREFIX.getAlias1()) ||
                        arguments.get(1).equalsIgnoreCase(CommandType.PREFIX.getAlias2())) {
                    arguments.subList(0, 1).clear();
                    new PrefixCommand(event, arguments, prefix);
                }
        } else if (
                arguments.get(0).equalsIgnoreCase(prefix + CommandType.VOTE.getAlias1()) ||
                        arguments.get(0).equalsIgnoreCase(prefix + CommandType.VOTE.getAlias2())
        ) {
            arguments.remove(0);
            new VoteCommand(event, arguments, prefix);
        }
    }
}
