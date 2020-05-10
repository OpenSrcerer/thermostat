package Thermostat.MySQL;

import java.sql.SQLException;

// Driver class for importing data
// into the database

/**
 * <h1>Create</h1>
 * <p>Class that contains static functions, used to
 * initiate a database connection and perform
 * creation operations on it, such as for a Guild,
 * or Channel.
 */

public class Create {
    /**
     * Creates a new instance of a guild in the database.
     * <p>Affects tables: <b>GUILDS</b>
     * @param GUILD_ID Guild that is about to be added to DB.
     */
    public static void Guild(String GUILD_ID) {
        if (GUILD_ID == null) {
            throw new NullPointerException();
        }

        Connection conn;
        try {
            conn = new Connection();
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
            return;
        }

        try {
            conn.update("INSERT INTO GUILDS (GUILD_ID, GUILD_ENABLE) VALUES (" + GUILD_ID + ", 0);");
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Guild could not be created.");
        }

        conn.closeConnection();
    }

    /**
     * Initializes a new entry for a channel with a matching
     * guild in the database.
     * <p>Affects tables: <b>GUILDS, CHANNELS, CHANNEL_SETTINGS</b>
     * @param GUILD_ID Guild that the channel resides in.
     * @param CHANNEL_ID The channel about to be added to the DB.
     * @param monitor Whether the channel should be initialized as
     *                monitored (inits with 1 or 0).
     */
    public static void Channel(String GUILD_ID, String CHANNEL_ID, int monitor) {
        if (GUILD_ID == null || CHANNEL_ID == null) {
            throw new NullPointerException();
        }

        Connection conn;
        try {
            conn = new Connection();
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
            return;
        }

        try {
            conn.update("INSERT INTO CHANNELS (CHANNEL_ID, GUILD_ID) VALUES (" + CHANNEL_ID + ", " + GUILD_ID + ");");
            // add channelID, min, and max slowmode
            conn.update("INSERT INTO CHANNEL_SETTINGS (CHANNEL_ID, MIN_SLOW, MAX_SLOW, MONITORED) VALUES (" + CHANNEL_ID + ", 0, 0, " + monitor + ");");
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Channels could not be added.");
        }

        conn.closeConnection();
    }

    /**
     * Changes a channel's monitor value on the database.
     * @param GUILD_ID The ID of the Guild that the channel resides in.
     * @param CHANNEL_ID The Channel's id.
     * @param monitor monitor value. Can be 1 or 0 to indicate truth or falsity
     */
    public static void ChannelMonitor (String GUILD_ID, String CHANNEL_ID, int monitor) {
        Connection conn;
        try {
            conn = new Connection();
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
            return;
        }

        try
        {
            conn.update("UPDATE CHANNEL_SETTINGS JOIN CHANNELS ON " +
                    "(CHANNEL_SETTINGS.CHANNEL_ID = CHANNELS.CHANNEL_ID) JOIN GUILDS ON " +
                    "(CHANNELS.GUILD_ID = GUILDS.GUILD_ID) " +
                    "SET MONITORED = " + monitor + " WHERE CHANNEL_SETTINGS.CHANNEL_ID = " +
                    CHANNEL_ID + " AND GUILDS.GUILD_ID = " + GUILD_ID);
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
            System.out.println("Monitoring state could not be changed.");
        }

        conn.closeConnection();
    }
}
