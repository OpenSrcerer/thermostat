package thermostat.mySQL;

import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.preparedStatements.ErrorEmbeds;
import thermostat.thermoFunctions.Messages;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * Contains functions used to
 * initiate a database connection and insert
 * new Guilds or Channels to the DB.
 */

public abstract class Create {
    public static final Logger lgr = LoggerFactory.getLogger(Create.class);

    /**
     * Creates a new instance of a guild in the database.
     *
     * @param GUILD_ID Guild that is about to be added to DB.
     */
    public static void Guild(String GUILD_ID) {
        try {
            DataSource.update("INSERT INTO GUILDS (GUILD_ID, GUILD_ENABLE) VALUES (?, 0);", GUILD_ID);
        } catch (SQLException ex) {
            lgr.error(ex.getMessage(), ex);
        }
    }

    /**
     * Initializes a new entry for a channel with a matching
     * guild in the database.
     *
     * @param GUILD_ID   Guild that the channel resides in.
     * @param CHANNEL_ID The channel about to be added to the DB.
     * @param monitor    Whether the channel should be initialized as
     *                   monitored (inits with 1 or 0).
     */
    public static void Channel(String GUILD_ID, String CHANNEL_ID, int monitor) {
        try {
            DataSource.update("INSERT INTO CHANNELS (CHANNEL_ID, GUILD_ID) VALUES (?, ?);", Arrays.asList(CHANNEL_ID, GUILD_ID));
            // add channelID, min, and max slowmode
            DataSource.update("INSERT INTO CHANNEL_SETTINGS (CHANNEL_ID, MIN_SLOW, MAX_SLOW, MONITORED) VALUES (?, 0, 0, ?);",
                    Arrays.asList(CHANNEL_ID, Integer.toString(monitor)));
        } catch (SQLException ex) {
            lgr.error(ex.getMessage(), ex);
        }
    }

    /**
     * Changes a channel's monitor value on the database.
     *
     * @param GUILD_ID   The ID of the Guild that the channel resides in.
     * @param CHANNEL_ID The Channel's id.
     * @param monitor    Whether the channel should be initialized as
     *                   monitored (inits with 1 or 0).
     */
    public static void Monitor(String GUILD_ID, String CHANNEL_ID, int monitor) {
        try {
            if (!DataSource.checkDatabaseForData("SELECT * FROM CHANNELS JOIN GUILDS ON " +
                    "(CHANNELS.GUILD_ID = GUILDS.GUILD_ID) WHERE CHANNEL_ID = ?", CHANNEL_ID))
            {
                Create.Channel(GUILD_ID, CHANNEL_ID, monitor);
                return;
            }

            DataSource.update("UPDATE CHANNEL_SETTINGS JOIN CHANNELS ON " +
                    "(CHANNEL_SETTINGS.CHANNEL_ID = CHANNELS.CHANNEL_ID) JOIN GUILDS ON " +
                    "(CHANNELS.GUILD_ID = GUILDS.GUILD_ID) " +
                    "SET MONITORED = ? WHERE CHANNEL_SETTINGS.CHANNEL_ID = ?" +
                    " AND GUILDS.GUILD_ID = ?",
                    Arrays.asList(Integer.toString(monitor), CHANNEL_ID, GUILD_ID));
        } catch (SQLException ex) {
            lgr.error(ex.getMessage(), ex);
        }
    }

    public static StringBuilder setFilter(String filtered, List<String> args, TextChannel eventChannel) {

        StringBuilder builder = new StringBuilder();
        try {
            for (String arg : args) {
                if (!DataSource.checkDatabaseForData("SELECT * FROM CHANNELS JOIN GUILDS ON " +
                        "(CHANNELS.GUILD_ID = GUILDS.GUILD_ID) WHERE CHANNEL_ID = ?", arg))
                {
                    Create.Channel(eventChannel.getGuild().getId(), arg, 0);
                }

                if (filtered.equals("0")) {
                    DataSource.update("UPDATE CHANNEL_SETTINGS SET FILTERED = ?, WEBHOOK_ID = 0, WEBHOOK_TOKEN = 0 WHERE CHANNEL_ID = ?",
                            Arrays.asList(filtered, arg));
                } else {
                    DataSource.update("UPDATE CHANNEL_SETTINGS SET FILTERED = ? WHERE CHANNEL_ID = ?",
                            Arrays.asList(filtered, arg));
                }
                builder.append("<#").append(arg).append("> ");
            }
        } catch (SQLException ex) {
            Messages.sendMessage(eventChannel, ErrorEmbeds.errFatal(ex.getLocalizedMessage()));
        }
        return builder;
    }
}
