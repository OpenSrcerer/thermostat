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
    // DEFAULT CHANNEL SETTINGS VALUES
    private static final int SAMPLE_RATE = 30;
    private static final boolean CHANNEL_MONITOR = false;
    // ------------------------------

    /**
     * Creates a new instance of a guild in the database.
     * <p>Affects tables: <b>GUILDS</b>
     * @param GUILD_ID
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
     * @param GUILD_ID
     * @param CHANNEL_ID
     */
    public static void Channel(String GUILD_ID, String CHANNEL_ID) {
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
            conn.update("INSERT INTO CHANNEL_SETTINGS (CHANNEL_ID, CHANNEL_MONITOR, SAMPLE_RATE) VALUES (" + CHANNEL_ID + ", " + CHANNEL_MONITOR + ", " + SAMPLE_RATE + ");");
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Channels could not be added.");
        }

        conn.closeConnection();
    }
}
