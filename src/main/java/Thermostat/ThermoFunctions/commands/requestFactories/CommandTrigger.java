package thermostat.thermoFunctions.commands.requestFactories;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import thermostat.mySQL.DataSource;
import thermostat.thermoFunctions.Functions;
import thermostat.thermoFunctions.commands.CommandData;
import thermostat.thermoFunctions.commands.requestFactories.informational.ChartCommand;
import thermostat.thermoFunctions.commands.requestFactories.informational.GetMonitorCommand;
import thermostat.thermoFunctions.commands.requestFactories.informational.SettingsCommand;
import thermostat.thermoFunctions.commands.requestFactories.monitoring.*;
import thermostat.thermoFunctions.commands.requestFactories.other.InfoCommand;
import thermostat.thermoFunctions.commands.requestFactories.other.InviteCommand;
import thermostat.thermoFunctions.commands.requestFactories.other.PrefixCommand;
import thermostat.thermoFunctions.commands.requestFactories.other.VoteCommand;
import thermostat.thermoFunctions.commands.requestFactories.utility.FilterCommand;
import thermostat.thermoFunctions.entities.RequestType;
import thermostat.thermoFunctions.commands.requestFactories.utility.WordFilterCommand;
import thermostat.thermostat;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Main adapter class for any sort of command
 * based on the GuildMessageReactionEvent event.
 */
public class CommandTrigger extends ListenerAdapter {

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
        CommandData data = new CommandData(ev.getGuild(), ev.getChannel(), ev.getMember(), prefix, args, ev.getMessage());
        Functions.checkGuildAndChannelThenSet(ev.getGuild().getId(), ev.getChannel().getId());

        // Checks for whether the channel has the offensive-word filter activated.
        if (DataSource.queryBool("SELECT FILTERED FROM CHANNEL_SETTINGS JOIN CHANNELS ON " +
                "(CHANNELS.CHANNEL_ID = CHANNEL_SETTINGS.CHANNEL_ID) WHERE CHANNELS.GUILD_ID = ? " +
                "AND CHANNELS.CHANNEL_ID = ?",
                Arrays.asList(ev.getGuild().getId(), ev.getChannel().getId()))) {
            new WordFilterCommand(data);
        }

        if (
                args.get(0).equalsIgnoreCase(prefix + RequestType.CHART.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + RequestType.CHART.getAlias2())
        ) {
            args.remove(0);
            new ChartCommand(data);
        }

        else if (
                args.get(0).equalsIgnoreCase(prefix + RequestType.FILTER.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + RequestType.FILTER.getAlias2())
        ) {
            args.remove(0);
            new FilterCommand(data);
        }

        else if (
                args.get(0).equalsIgnoreCase(prefix + RequestType.GETMONITOR.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + RequestType.GETMONITOR.getAlias2())
        ) {
            args.remove(0);
            new GetMonitorCommand(data);
        }

        else if (
                args.get(0).equalsIgnoreCase(prefix + RequestType.SETTINGS.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + RequestType.SETTINGS.getAlias2())
        ) {
            args.remove(0);
            new SettingsCommand(data);
        }

        else if (
                args.get(0).equalsIgnoreCase(prefix + RequestType.MONITOR.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + RequestType.MONITOR.getAlias2())
        ) {
            args.remove(0);
            new MonitorCommand(data);
        }

        else if (
                args.get(0).equalsIgnoreCase(prefix + RequestType.SENSITIVITY.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + RequestType.SENSITIVITY.getAlias2())
        ) {
            args.remove(0);
            new SensitivityCommand(data);
        }

        else if (
                args.get(0).equalsIgnoreCase(prefix + RequestType.SETBOUNDS.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + RequestType.SETBOUNDS.getAlias2())
        ) {
            args.remove(0);
            new SetBoundsCommand(data);
        }

        else if (
                args.get(0).equalsIgnoreCase(prefix + RequestType.INFO.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + RequestType.INFO.getAlias2()) ||
                        args.get(0).equalsIgnoreCase(prefix + RequestType.HELP.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + RequestType.HELP.getAlias2())
        ) {
            args.remove(0);
            new InfoCommand(data);
        }

        else if (
                args.get(0).equalsIgnoreCase(prefix + RequestType.INVITE.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + RequestType.INVITE.getAlias2())
        ) {
            args.remove(0);
            new InviteCommand(data);
        }

        else if (
                args.get(0).equalsIgnoreCase(prefix + RequestType.PREFIX.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + RequestType.PREFIX.getAlias2())
        ) {
            args.remove(0);
            new PrefixCommand(data);
        }

        else if (
                (args.get(0).equalsIgnoreCase("<@!" + thermostat.thermo.getSelfUser().getId() + ">"))
        ) {
            if (args.size() > 1)
                if (args.get(1).equalsIgnoreCase(RequestType.PREFIX.getAlias1()) ||
                        args.get(1).equalsIgnoreCase(RequestType.PREFIX.getAlias2())) {
                    args.subList(0, 1).clear();
                    new PrefixCommand(data);
                }
        } else if (
                args.get(0).equalsIgnoreCase(prefix + RequestType.VOTE.getAlias1()) ||
                        args.get(0).equalsIgnoreCase(prefix + RequestType.VOTE.getAlias2())
        ) {
            args.remove(0);
            new VoteCommand(data);
        }
    }
}
