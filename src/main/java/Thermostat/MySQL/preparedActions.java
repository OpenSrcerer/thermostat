package thermostat.mySQL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.util.GuildCache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Contains functions used to insert
 * new Guilds or Channels to the DB.
 */
public final class PreparedActions {
    /**
     * Logger for this class.
     */
    public static final Logger lgr = LoggerFactory.getLogger(PreparedActions.class);

    /**
     * Creates a new entry of a Guild in the database.
     * @param guildId Guild that is about to be added to DB.
     */
    public static void createGuild(Connection conn, String guildId) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("INSERT INTO GUILDS (GUILD_ID, GUILD_ENABLE) VALUES (?, 0);");
        statement.setString(1, guildId);
        statement.executeUpdate();
    }

    /**
     * Initializes a new entry for a channel with a matching
     * guild in the database.
     *
     * @param guildId   Guild that the channel resides in.
     * @param channelId The channel about to be added to the DB.
     * @param monitor   Whether the channel should be initialized as
     *                  monitored (inits with 1 or 0).
     */
    public static void createChannel(Connection conn, String guildId, String channelId, int monitor) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("INSERT INTO CHANNELS (CHANNEL_ID, GUILD_ID) VALUES (?, ?);");
        statement.setString(1, channelId);
        statement.setString(2, guildId);
        statement.executeUpdate();

        statement = conn.prepareStatement("INSERT INTO CHANNEL_SETTINGS (CHANNEL_ID, MIN_SLOW, MAX_SLOW, MONITORED) VALUES (?, 0, 0, ?);");
        statement.setString(1, channelId);
        statement.setString(2, Integer.toString(monitor));
        statement.executeUpdate();
    }

    /**
     * Changes a channel's monitor value on the database.
     *
     * @param conn      Connection to use for monitoring.
     * @param guildId   The ID of the Guild that the channel resides in.
     * @param channelId The Channel's id.
     * @param monitor   Whether the channel should be initialized as
     *                  monitored (inits with 1 or 0).
     */
    public static void createMonitor(Connection conn, String guildId, String channelId, int monitor) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("SELECT * FROM CHANNELS JOIN GUILDS ON " +
                "(CHANNELS.GUILD_ID = GUILDS.GUILD_ID) WHERE CHANNEL_ID = ?");
        statement.setString(1, channelId);

        if (!statement.executeQuery().next()) {
            PreparedActions.createChannel(conn, guildId, channelId, monitor);
            return;
        }

        statement = conn.prepareStatement("UPDATE CHANNEL_SETTINGS JOIN CHANNELS ON " +
                        "(CHANNEL_SETTINGS.CHANNEL_ID = CHANNELS.CHANNEL_ID) JOIN GUILDS ON " +
                        "(CHANNELS.GUILD_ID = GUILDS.GUILD_ID) " +
                        "SET MONITORED = ? WHERE CHANNEL_SETTINGS.CHANNEL_ID = ?" +
                        " AND GUILDS.GUILD_ID = ?");
        statement.setString(1, Integer.toString(monitor));
        statement.setString(2, channelId);
        statement.setString(3, guildId);

        if (monitor == 1) {
            GuildCache.getSynapse(guildId).addChannel(channelId);
        } else {
            GuildCache.getSynapse(guildId).removeChannel(channelId);
        }
    }

    public static StringBuilder setFilter(Connection conn, String filtered, List<String> channels) throws SQLException {
        StringBuilder builder = new StringBuilder();

        PreparedStatement statement;

            for (String channel : channels) {
                if (filtered.equals("0")) {
                    statement = conn.prepareStatement("UPDATE CHANNEL_SETTINGS SET FILTERED = ?, WEBHOOK_ID = 0, WEBHOOK_TOKEN = 0 WHERE CHANNEL_ID = ?");
                    statement.setString(1, filtered);
                    statement.setString(2, channel);
                } else {
                    statement = conn.prepareStatement("UPDATE CHANNEL_SETTINGS SET FILTERED = ? WHERE CHANNEL_ID = ?");
                    statement.setString(1, filtered);
                    statement.setString(2, channel);
                }
                statement.executeUpdate();
                builder.append("<#").append(channel).append("> ");
            }

        return builder;
    }

    /**
     * Retrieve whether a channel is monitored or not from the database.
     * @param conn Connection to use to perform this action.
     * @param channel Channel to perform this action on.
     * @return Retrieves whether the channel is monitored or not. -1 otherwise.
     * @throws SQLException If something went wrong while performing changes in database.
     */
    public static int retrieveMonitoredValue(Connection conn, String channel) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("SELECT MONITORED FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = ?");
        statement.setString(1, channel);
        ResultSet rs = statement.executeQuery();

        if (rs.next()) {
            return rs.getInt(1);
        } else {
            return -1;
        }
    }
}
