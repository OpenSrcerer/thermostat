package thermostat.thermoFunctions.commands;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.mySQL.DataSource;
import thermostat.thermoFunctions.Functions;
import thermostat.thermoFunctions.commands.informational.Chart;
import thermostat.thermoFunctions.commands.informational.GetMonitorList;
import thermostat.thermoFunctions.commands.informational.Settings;
import thermostat.thermoFunctions.commands.monitoring.*;
import thermostat.thermoFunctions.commands.other.Info;
import thermostat.thermoFunctions.commands.other.Invite;
import thermostat.thermoFunctions.commands.other.Prefix;
import thermostat.thermoFunctions.commands.other.Vote;
import thermostat.thermoFunctions.commands.utility.Filter;
import thermostat.thermoFunctions.entities.CommandType;
import thermostat.thermoFunctions.listeners.WordFilterEvent;
import thermostat.thermostat;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Main adapter class for any sort of command
 * based on the GuildMessageReactionEvent event.
 */
public class CommandManager extends ListenerAdapter {
    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent ev) {

        if (ev.getMember() == null)
            return;

        if (ev.getMember().getUser().isBot())
            return;

        String prefix = DataSource.queryString("SELECT GUILD_PREFIX FROM GUILDS WHERE GUILD_ID = ?", ev.getGuild().getId());
        if (prefix == null) {
            prefix = thermostat.prefix;
        }

        // gets given arguments and passes them to a list
        ArrayList<String> args = new ArrayList<>(Arrays.asList(ev.getMessage().getContentRaw().split("\\s+")));

        Functions.checkGuildAndChannelThenSet(ev.getGuild().getId(), ev.getChannel().getId());

        // Checks for whether the channel has the offensive-word filter activated.
        if (DataSource.queryBool("SELECT FILTERED FROM CHANNEL_SETTINGS JOIN CHANNELS ON " +
                "(CHANNELS.CHANNEL_ID = CHANNEL_SETTINGS.CHANNEL_ID) WHERE CHANNELS.GUILD_ID = ? " +
                "AND CHANNELS.CHANNEL_ID = ?",
                Arrays.asList(ev.getGuild().getId(), ev.getChannel().getId()))) {
            Logger lgr = LoggerFactory.getLogger(CommandManager.class);
            lgr.debug("Starting WFE");
            new WordFilterEvent(ev.getChannel(), ev.getMessage(), args);
        }

        if (
                args.get(0).equalsIgnoreCase(prefix + CommandType.CHART.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + CommandType.CHART.getAlias2())
        ) Chart.execute(args, ev.getGuild(), ev.getChannel(), ev.getMember(), prefix);

        else if (
                args.get(0).equalsIgnoreCase(prefix + CommandType.FILTER.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + CommandType.FILTER.getAlias2())
        ) Filter.execute(args, ev.getGuild(), ev.getChannel(), ev.getMember());

        else if (
                args.get(0).equalsIgnoreCase(prefix + CommandType.GETMONITORLIST.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + CommandType.GETMONITORLIST.getAlias2())
        ) GetMonitorList.execute(ev.getGuild(), ev.getChannel(), ev.getMember());

        else if (
                args.get(0).equalsIgnoreCase(prefix + CommandType.SETTINGS.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + CommandType.SETTINGS.getAlias2())
        ) Settings.execute(args, ev.getGuild(), ev.getChannel(), ev.getMember());

        else if (
                args.get(0).equalsIgnoreCase(prefix + CommandType.MONITOR.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + CommandType.MONITOR.getAlias2())
        ) Monitor.execute(args, ev.getGuild(), ev.getChannel(), ev.getMember());

        else if (
                args.get(0).equalsIgnoreCase(prefix + CommandType.SENSITIVITY.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + CommandType.SENSITIVITY.getAlias2())
        ) Sensitivity.execute(args, ev.getGuild(), ev.getChannel(), ev.getMember(), prefix);

        else if (
                args.get(0).equalsIgnoreCase(prefix + CommandType.SETBOUNDS.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + CommandType.SETBOUNDS.getAlias2())
        ) SetMaximum.execute(args, ev.getGuild(), ev.getChannel(), ev.getMember());

        else if (
                args.get(0).equalsIgnoreCase(prefix + CommandType.UNMONITOR.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + CommandType.UNMONITOR.getAlias2())
        ) UnMonitor.execute(args, ev.getGuild(), ev.getChannel(), ev.getMember());

        else if (
                args.get(0).equalsIgnoreCase(prefix + CommandType.UNMONITORALL.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + CommandType.UNMONITORALL.getAlias2())
        ) UnMonitorAll.execute(ev.getGuild(), ev.getChannel(), ev.getMember());

        else if (
                args.get(0).equalsIgnoreCase(prefix + CommandType.INFO.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + CommandType.INFO.getAlias2()) ||
                        args.get(0).equalsIgnoreCase(prefix + CommandType.HELP.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + CommandType.HELP.getAlias2())
        ) Info.execute(ev.getChannel(), ev.getMember());

        else if (
                args.get(0).equalsIgnoreCase(prefix + CommandType.INVITE.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + CommandType.INVITE.getAlias2())
        ) Invite.execute(ev.getChannel(), ev.getMember());

        else if (
                args.get(0).equalsIgnoreCase(prefix + CommandType.PREFIX.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + CommandType.PREFIX.getAlias2())
        ) Prefix.execute(args, ev.getGuild(), ev.getChannel(), ev.getMember(), prefix, false);

        else if (
                (args.get(0).equalsIgnoreCase("<@!" + thermostat.thermo.getSelfUser().getId() + ">"))
        ) {
            if (args.size() > 1)
                if (args.get(1).equalsIgnoreCase(CommandType.PREFIX.getAlias1()) || args.get(1).equalsIgnoreCase(CommandType.PREFIX.getAlias2()))
                    Prefix.execute(args, ev.getGuild(), ev.getChannel(), ev.getMember(), prefix, true);
        } else if (
                args.get(0).equalsIgnoreCase(prefix + CommandType.VOTE.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + CommandType.VOTE.getAlias2())
        ) Vote.execute(ev.getChannel(), ev.getMember());
    }
}
