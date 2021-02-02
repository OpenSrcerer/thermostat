package thermostat.mySQL;

import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import okhttp3.internal.annotations.EverythingIsNonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.util.GuildCache;
import thermostat.util.MiscellaneousFunctions;
import thermostat.util.enumeration.DBActionType;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    @EverythingIsNonNull
    public static void createGuild(final Connection conn, final String guildId) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("INSERT INTO GUILDS (GUILD_ID) VALUES (?);");
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
    @EverythingIsNonNull
    public static void createChannel(final Connection conn, final String guildId, final String channelId, final int monitor) throws SQLException {
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
     * Changes a channel's filtered/monitored value on the database.
     * @param conn      Connection to use for this action.
     * @param value     The value to give to the field in the database.
     * @param guildId   The ID of the Guild that the channel resides in.
     * @param channels  The ID of every channel.
     */
    @EverythingIsNonNull
    public static StringBuilder modifyChannel(final Connection conn, final DBActionType action, final int value,
                                              final String guildId, final List<String> channels) throws SQLException
    {
        StringBuilder builder = new StringBuilder();

        // Insert the channels into the SQL query manually.
        PreparedStatement statement = conn.prepareStatement(
                ("UPDATE CHANNEL_SETTINGS JOIN CHANNELS ON " +
                "(CHANNEL_SETTINGS.CHANNEL_ID = CHANNELS.CHANNEL_ID) JOIN GUILDS ON " +
                "(CHANNELS.GUILD_ID = GUILDS.GUILD_ID) " +
                "SET " + action.sqlAction1 + " WHERE CAST(CHANNEL_SETTINGS.CHANNEL_ID AS VARCHAR(60)) IN (@) " +
                "AND GUILDS.GUILD_ID = ?")
                .replaceFirst("@", MiscellaneousFunctions.toQueryString(channels))
        );

        statement.setInt(1, value);
        statement.setString(2, guildId);
        statement.executeUpdate();

        for (String channel : channels) {
            builder.append("<#").append(channel).append("> ");

            // Add channel to synapse cache.
            if (action.equals(DBActionType.MONITOR) && value == 1) {
                GuildCache.getSynapse(guildId).addChannel(channel);
            } else {
                GuildCache.getSynapse(guildId).removeChannel(channel);
            }
        }

        return builder;
    }

    /**
     * Changes a channel's slowmode bounds on the database.
     * @param conn      Connection to use for this action.
     * @param guildId   The ID of the Guild that the channel resides in.
     * @param channels  The ID of every channel.
     */
    @EverythingIsNonNull
    public static StringBuilder modifyBounds(final Connection conn, final String guildId,
                                             final int minBound, final int maxBound,
                                             final List<String> channels) throws SQLException
    {
        StringBuilder builder = new StringBuilder();

        int minimumCurr, maximumCurr, minimumNew = minBound, maximumNew = maxBound;
        PreparedStatement statement = conn.prepareStatement(
                ("SELECT CHANNEL_ID, MIN_SLOW, MAX_SLOW " +
                        "FROM CHANNEL_SETTINGS WHERE " +
                        "CAST(CHANNEL_ID AS VARCHAR(60)) IN (@)")
                        .replaceFirst("@", MiscellaneousFunctions.toQueryString(channels))
        );
        ResultSet rs = statement.executeQuery();

        while (rs.next()) {
            final String channelId = rs.getString(1);
            minimumCurr = rs.getInt(2);
            maximumCurr = rs.getInt(3);

            // Minimum not set
            if (minimumNew == -1) {
                minimumNew = minimumCurr;

                if (minimumNew > maximumNew) {
                    minimumNew = maximumNew;
                }
            }

            // Maximum not set
            if (maximumNew == -1) {
                maximumNew = maximumCurr;

                if (maximumNew < minimumNew) {
                    maximumNew = minimumNew;
                }
            }

            // Insert the channels into the SQL query manually.
            statement = conn.prepareStatement(
                    ("UPDATE CHANNEL_SETTINGS JOIN CHANNELS ON " +
                            "(CHANNEL_SETTINGS.CHANNEL_ID = CHANNELS.CHANNEL_ID) JOIN GUILDS ON " +
                            "(CHANNELS.GUILD_ID = GUILDS.GUILD_ID) " +
                            "SET CHANNEL_SETTINGS.MIN_SLOW = ?, CHANNEL_SETTINGS.MAX_SLOW = ? " +
                            "WHERE CHANNEL_SETTINGS.CHANNEL_ID = ? " +
                            "AND GUILDS.GUILD_ID = ?")
            );

            statement.setInt(1, minimumNew);
            statement.setInt(2, maximumNew);
            statement.setString(3, channelId);
            statement.setString(4, guildId);
            statement.executeUpdate();

            builder.append("<#").append(channelId).append("> ");
        }

        return builder;
    }

    /**
     * Retrieve whether a channel is filtered or not from the database.
     * @param conn Connection to apply this action to.
     * @param guildId ID of guild where the channel is in.
     * @param channelId ID of channel to check.
     * @return A boolean that shows whether the channel is filtered.
     * @throws SQLException Something went wrong while communicating with the database.
     */
    @EverythingIsNonNull
    public static boolean isFiltered(final Connection conn, final String guildId, final String channelId) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("SELECT FILTERED FROM CHANNEL_SETTINGS JOIN CHANNELS ON " +
                "(CHANNELS.CHANNEL_ID = CHANNEL_SETTINGS.CHANNEL_ID) WHERE CHANNELS.GUILD_ID = ? " +
                "AND CHANNELS.CHANNEL_ID = ?");
        statement.setString(1, guildId);
        statement.setString(2, channelId);
        ResultSet rs = statement.executeQuery();
        rs.next();
        return rs.getBoolean(1);
    }

    /**
     * Connects to the database, Unfiltering/Unmonitoring all channels for a given Guild.
     * @param event Event this action was called from.
     * @param type Type of database action to perform.
     * @return A DatabaseAction to Unmonitor/Unfilter channels from the database.
     */
    @EverythingIsNonNull
    public static DataSource.DatabaseAction<Void> discardChannels(final GuildMessageReactionAddEvent event,
                                                                  final DBActionType type)
    {
        return conn -> {
            List<String> channels = new ArrayList<>();
            PreparedStatement pst = conn.prepareStatement("SELECT * FROM CHANNELS JOIN CHANNEL_SETTINGS ON " +
                    "(CHANNELS.CHANNEL_ID = CHANNEL_SETTINGS.CHANNEL_ID) " +
                    "WHERE CHANNELS.GUILD_ID = ? AND CHANNEL_SETTINGS." + type.sqlAction2 + " = 1");
            pst.setString(1, event.getGuild().getId());
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                channels.add(rs.getString(1));
            }

            // Do nothing if the list is empty, but still send a response as if something was done.
            // THE ILLUSION OF USER INTERACTION
            if (!channels.isEmpty()) {
                PreparedActions.modifyChannel(conn, type, 0, event.getGuild().getId(), channels);
            }

            return null;
        };
    }

    /**
     * Connects to the database, Filtering/Monitoring all channels for a given Guild.
     * @param event Event this action was called from.
     * @param type Type of database action to perform.
     * @return A DatabaseAction to Monitor/Filter channels from the database.
     */
    @EverythingIsNonNull
    public static DataSource.DatabaseAction<Void> acquireChannels(final GuildMessageReactionAddEvent event,
                                                                  final DBActionType type)
    {
        return conn -> {
            // Retrieve all channels in the Guild.
            List<String> channels = event.getGuild().getChannels()
                    .stream().map(ISnowflake::getId).collect(Collectors.toList());

            // Do nothing if the list is empty, but still send a response as if something was done.
            // THE ILLUSION OF USER INTERACTION
            if (!channels.isEmpty()) {
                PreparedActions.modifyChannel(conn, type, 1, event.getGuild().getId(), channels);
            }

            return null;
        };
    }

    /**
     * Deletes a whole guild from the database,
     * including its' respective channels and
     * channel settings.
     * @param guildId Guild to be deleted from DB.
     */
    public static void deleteGuild(@Nonnull final String guildId) {
        try {
            DataSource.execute(conn -> {
                String[] statements = {
                        "DELETE CHANNEL_SETTINGS FROM GUILDS JOIN CHANNELS" +
                                " ON (CHANNELS.GUILD_ID = GUILDS.GUILD_ID) JOIN CHANNEL_SETTINGS" +
                                " ON (CHANNEL_SETTINGS.CHANNEL_ID = CHANNELS.CHANNEL_ID)" +
                                " WHERE GUILDS.GUILD_ID = ?",
                        "DELETE CHANNELS FROM GUILDS JOIN CHANNELS " +
                                "ON (CHANNELS.GUILD_ID = GUILDS.GUILD_ID) " +
                                "WHERE GUILDS.GUILD_ID = ?",
                        "DELETE FROM GUILDS WHERE GUILD_ID = ?"
                };

                PreparedStatement statement;
                // Execute every statement.
                for (String s : statements) {
                    statement = conn.prepareStatement(s);
                    statement.setString(1, guildId);
                    statement.executeUpdate();
                }
                return null;
            });
            lgr.info("Successfully deleted " + guildId + "from the database. (Guild Kicked Thermo)");
        } catch (SQLException ex) {
            lgr.warn("Failed to delete Guild " + guildId + " from the database. Details:", ex);
        }
    }

    /**
     * Deletes a channel from the database,
     * including its' respective settings.
     * @param guildId   Guild where the channel resides in.
     * @param channelId Channel that will be removed from DB.
     */
    @EverythingIsNonNull
    public static void deleteChannel(final Connection conn, final String guildId, final String channelId) throws SQLException {
        String[] statements = {
                "DELETE CHANNEL_SETTINGS FROM GUILDS JOIN CHANNELS" +
                        " ON (CHANNELS.GUILD_ID = GUILDS.GUILD_ID) JOIN CHANNEL_SETTINGS" +
                        " ON (CHANNEL_SETTINGS.CHANNEL_ID = CHANNELS.CHANNEL_ID)" +
                        " WHERE GUILDS.GUILD_ID = ? AND CHANNELS.CHANNEL_ID = ?",
                "DELETE CHANNELS FROM GUILDS JOIN CHANNELS " +
                        "ON (CHANNELS.GUILD_ID = GUILDS.GUILD_ID) " +
                        "WHERE GUILDS.GUILD_ID = ? AND CHANNELS.CHANNEL_ID = ?"
        };

        PreparedStatement statement;
        // Execute every statement.
        for (String s : statements) {
            statement = conn.prepareStatement(s);
            statement.setString(1, guildId);
            statement.setString(2, channelId);
            statement.executeUpdate();
        }
    }
}
