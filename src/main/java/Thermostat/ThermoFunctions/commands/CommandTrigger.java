package thermostat.thermoFunctions.commands;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.Thermostat;
import thermostat.mySQL.DataSource;
import thermostat.thermoFunctions.Functions;
import thermostat.thermoFunctions.commands.informational.ChartCommand;
import thermostat.thermoFunctions.commands.informational.GetMonitorCommand;
import thermostat.thermoFunctions.commands.informational.SettingsCommand;
import thermostat.thermoFunctions.commands.monitoring.MonitorCommand;
import thermostat.thermoFunctions.commands.monitoring.SensitivityCommand;
import thermostat.thermoFunctions.commands.monitoring.SetBoundsCommand;
import thermostat.thermoFunctions.commands.other.InfoCommand;
import thermostat.thermoFunctions.commands.other.InviteCommand;
import thermostat.thermoFunctions.commands.other.PrefixCommand;
import thermostat.thermoFunctions.commands.other.VoteCommand;
import thermostat.thermoFunctions.commands.utility.FilterCommand;
import thermostat.thermoFunctions.commands.utility.WordFilterCommand;
import thermostat.thermoFunctions.entities.CommandType;

import javax.annotation.Nonnull;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;

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
     * Cache object that contains Guild prefixes.
     */
    private static final Map<String, String> prefixCache = new WeakHashMap<>();

    /**
     * Trigger a new command when a message calling thermostat gets sent.
     * @param event Event that contains sent message.
     */
    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        String prefix = getGuildPrefix(event.getGuild().getId());
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
            new PrefixCommand(event, arguments, prefix);
        }

        else if (
                arguments.get(0).equalsIgnoreCase(prefix + CommandType.VOTE.getAlias1()) ||
                        arguments.get(0).equalsIgnoreCase(prefix + CommandType.VOTE.getAlias2())
        ) {
            arguments.remove(0);
            new VoteCommand(event, arguments, prefix);
        }
    }

    /**
     * Convenience method that retrieves a Prefix for a Guild
     * from the cache or database.
     * @param guildId ID of Guild to lookup prefix for.
     * @return Prefix of said guild.
     */
    @Nonnull
    private static String getGuildPrefix(String guildId) {
        boolean isCached = true;
        String prefix = prefixCache.get(guildId);

        // Null check 1: If prefix is not cached for guild
        // retrieve prefix from database and create a cache entry
        if (prefix == null) {
            isCached = false;
            try {
                prefix = retrievePrefix(guildId);
            } catch (SQLException ex) {
                lgr.warn("Failure to retrieve prefix for Guild" + guildId + ":", ex);
            }
        }

        // Null check 2: If prefix is NULL in database
        // Fallback to default prefix
        if (prefix == null) {
            prefix = Thermostat.prefix;
        }

        if (!isCached) {
            prefixCache.put(guildId, prefix);
        }

        return prefix;
    }

    /**
     *
     * Retrieves a Prefix for a Guild from the database.
     * @param guildId ID of Guild to lookup prefix for.
     * @return Prefix of said guild.
     */
    private static String retrievePrefix(String guildId) throws SQLException {
        return DataSource.execute(conn -> {
            PreparedStatement query = conn.prepareStatement("SELECT GUILD_PREFIX FROM GUILDS WHERE GUILD_ID = ?");
            query.setString(1, guildId);
            ResultSet rs = query.executeQuery();

            if (rs.next()) {
                return rs.getString(1);
            } else {
                return null;
            }
        });
    }

    /**
     * Updates Guild prefix cache entry if it exists.
     * @param guildId ID of Guild to update.
     * @param newPrefix Newly assigned Guild prefix.
     */
    public static void updateEntry(String guildId, String newPrefix) {
        prefixCache.replace(guildId, newPrefix);
    }
}
