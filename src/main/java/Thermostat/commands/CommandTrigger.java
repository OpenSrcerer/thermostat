package thermostat.commands;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.Thermostat;
import thermostat.mySQL.DataSource;
import thermostat.mySQL.PreparedActions;
import thermostat.util.Constants;
import thermostat.commands.informational.ChartCommand;
import thermostat.commands.informational.GetMonitorCommand;
import thermostat.commands.informational.SettingsCommand;
import thermostat.commands.monitoring.MonitorCommand;
import thermostat.commands.monitoring.SensitivityCommand;
import thermostat.commands.monitoring.SetBoundsCommand;
import thermostat.commands.informational.InfoCommand;
import thermostat.commands.other.InviteCommand;
import thermostat.commands.other.PrefixCommand;
import thermostat.commands.other.VoteCommand;
import thermostat.commands.utility.FilterCommand;
import thermostat.commands.utility.WordFilter;
import thermostat.util.enumeration.CommandType;

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
        } catch (SQLException ignored) {
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

    /**
     * Convenience method that retrieves a Prefix for a Guild
     * from the cache or database.
     * @param guildId ID of Guild to lookup prefix for.
     * @return Prefix of said guild.
     */
    @Nonnull
    public static String getGuildPrefix(String guildId) {
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
            prefix = Constants.DEFAULT_PREFIX;
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
