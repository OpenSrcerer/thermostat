package Thermostat.MySQL;

import java.sql.SQLException;

/**
 * <h1>Create</h1>
 * <p>Class that contains static functions, used to
 * initiate a database connection and perform
 * deletion operations while accounting for
 * relational dependencies.
 */

public class Delete
{
    /**
     * Deletes a whole guild from the database,
     * including its' respective channels and
     * channel settings.
     * <p>Affects Tables: <b>GUILDS, CHANNELS, CHANNEL_SETTINGS</b>
     * @param GUILD_ID Guild to be deleted from DB.
     */
    public static void Guild (String GUILD_ID)
    {
        // initiates connection with database
        Connection conn;
        try {
            conn = new Connection();
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
            return;
        }

        // update queries to delete SQL rows related to guild
        // that just kicked bot
        // delete child then parent tables

        try
        {
            conn.update("DELETE CHANNEL_SETTINGS FROM GUILDS JOIN CHANNELS" +
                    " ON (CHANNELS.GUILD_ID = GUILDS.GUILD_ID) JOIN CHANNEL_SETTINGS" +
                    " ON (CHANNEL_SETTINGS.CHANNEL_ID = CHANNELS.CHANNEL_ID)" +
                    " WHERE GUILDS.GUILD_ID = " + GUILD_ID
            );

            conn.update(
                    "DELETE CHANNELS FROM GUILDS JOIN CHANNELS " +
                            "ON (CHANNELS.GUILD_ID = GUILDS.GUILD_ID) " +
                            "WHERE GUILDS.GUILD_ID = " + GUILD_ID
            );

            conn.update("DELETE FROM GUILDS WHERE GUILD_ID = " + GUILD_ID);
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
            System.out.println("Guild could not be deleted.");
        }

        conn.closeConnection();
    }

    /**
     * Deletes a channel from the database,
     * including its' respective settings.
     * <p>Affects Tables: <b>CHANNELS, CHANNEL_SETTINGS</b>
     * @param GUILD_ID Guild where the channel resides in.
     * @param CHANNEL_ID Channel that will be removed from DB.
     */
    public static void Channel (String GUILD_ID, String CHANNEL_ID)
    {
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
            conn.update("DELETE CHANNEL_SETTINGS FROM GUILDS JOIN CHANNELS" +
                    " ON (CHANNELS.GUILD_ID = GUILDS.GUILD_ID) JOIN CHANNEL_SETTINGS" +
                    " ON (CHANNEL_SETTINGS.CHANNEL_ID = CHANNELS.CHANNEL_ID)" +
                    " WHERE GUILDS.GUILD_ID = " + GUILD_ID +
                    " AND CHANNELS.CHANNEL_ID = " + CHANNEL_ID
            );

            conn.update(
                    "DELETE CHANNELS FROM GUILDS JOIN CHANNELS " +
                            "ON (CHANNELS.GUILD_ID = GUILDS.GUILD_ID) " +
                            "WHERE GUILDS.GUILD_ID = " + GUILD_ID +
                            " AND CHANNELS.CHANNEL_ID = " + CHANNEL_ID
            );
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
            System.out.println("Channel could not be deleted.");
        }

        conn.closeConnection();
    }
}

