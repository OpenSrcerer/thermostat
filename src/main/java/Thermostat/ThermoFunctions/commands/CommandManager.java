package thermostat.thermoFunctions.commands;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import thermostat.mySQL.DataSource;
import thermostat.thermoFunctions.Functions;
import thermostat.thermoFunctions.commands.informational.Chart;
import thermostat.thermoFunctions.commands.informational.GetMonitor;
import thermostat.thermoFunctions.commands.informational.Settings;
import thermostat.thermoFunctions.commands.monitoring.*;
import thermostat.thermoFunctions.commands.other.Info;
import thermostat.thermoFunctions.commands.other.Invite;
import thermostat.thermoFunctions.commands.other.Prefix;
import thermostat.thermoFunctions.commands.other.Vote;
import thermostat.thermoFunctions.commands.utility.Filter;
import thermostat.thermoFunctions.entities.CommandType;
import thermostat.thermoFunctions.commands.utility.WordFilterEvent;
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
            new WordFilterEvent(ev.getChannel(), ev.getMessage(), args);
        }

        if (
                args.get(0).equalsIgnoreCase(prefix + CommandType.CHART.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + CommandType.CHART.getAlias2())
        ) {
            args.remove(0);
            new Chart(ev.getGuild(), ev.getChannel(), ev.getMember(), prefix, args);
        }

        else if (
                args.get(0).equalsIgnoreCase(prefix + CommandType.FILTER.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + CommandType.FILTER.getAlias2())
        ) {
            args.remove(0);
            new Filter(ev.getGuild(), ev.getChannel(), ev.getMember(), prefix, args);
        }

        else if (
                args.get(0).equalsIgnoreCase(prefix + CommandType.GETMONITOR.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + CommandType.GETMONITOR.getAlias2())
        ) {
            args.remove(0);
            new GetMonitor(ev.getGuild(), ev.getChannel(), ev.getMember());
        }

        else if (
                args.get(0).equalsIgnoreCase(prefix + CommandType.SETTINGS.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + CommandType.SETTINGS.getAlias2())
        ) {
            args.remove(0);
            new Settings(ev.getGuild(), ev.getChannel(), ev.getMember(), args);
        }

        else if (
                args.get(0).equalsIgnoreCase(prefix + CommandType.MONITOR.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + CommandType.MONITOR.getAlias2())
        ) {
            args.remove(0);
            new Monitor(ev.getGuild(), ev.getChannel(), ev.getMember(), prefix, args);
        }

        else if (
                args.get(0).equalsIgnoreCase(prefix + CommandType.SENSITIVITY.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + CommandType.SENSITIVITY.getAlias2())
        ) {
            args.remove(0);
            new Sensitivity(ev.getGuild(), ev.getChannel(), ev.getMember(), prefix, args);
        }

        else if (
                args.get(0).equalsIgnoreCase(prefix + CommandType.SETBOUNDS.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + CommandType.SETBOUNDS.getAlias2())
        ) {
            args.remove(0);
            new SetBounds(ev.getGuild(), ev.getChannel(), ev.getMember(), prefix, args);
        }

        else if (
                args.get(0).equalsIgnoreCase(prefix + CommandType.INFO.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + CommandType.INFO.getAlias2()) ||
                        args.get(0).equalsIgnoreCase(prefix + CommandType.HELP.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + CommandType.HELP.getAlias2())
        ) {
            args.remove(0);
            new Info(ev.getChannel(), ev.getMember(), prefix, args);
        }

        else if (
                args.get(0).equalsIgnoreCase(prefix + CommandType.INVITE.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + CommandType.INVITE.getAlias2())
        ) {
            args.remove(0);
            new Invite(ev.getChannel(), ev.getMember());
        }

        else if (
                args.get(0).equalsIgnoreCase(prefix + CommandType.PREFIX.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + CommandType.PREFIX.getAlias2())
        ) {
            args.remove(0);
            new Prefix(ev.getGuild(), ev.getChannel(), ev.getMember(), prefix, args);
        }

        else if (
                (args.get(0).equalsIgnoreCase("<@!" + thermostat.thermo.getSelfUser().getId() + ">"))
        ) {
            if (args.size() > 1)
                if (args.get(1).equalsIgnoreCase(CommandType.PREFIX.getAlias1()) ||
                        args.get(1).equalsIgnoreCase(CommandType.PREFIX.getAlias2())) {
                    args.subList(0, 1).clear();
                    new Prefix(ev.getGuild(), ev.getChannel(), ev.getMember(), prefix, args);
                }
        } else if (
                args.get(0).equalsIgnoreCase(prefix + CommandType.VOTE.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + CommandType.VOTE.getAlias2())
        ) {
            args.remove(0);
            new Vote(ev.getChannel(), ev.getMember());
        }
    }
}
