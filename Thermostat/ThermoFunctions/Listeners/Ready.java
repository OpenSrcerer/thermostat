package Thermostat.ThermoFunctions.Listeners;

import Thermostat.MySQL.Connection;
import Thermostat.ThermoFunctions.Commands.*;
import Thermostat.ThermoFunctions.MonitorThreads.ChannelListener;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.SQLException;

import static Thermostat.thermostat.thermo;

/**
 * Listener class that runs the rest of the
 * bot once the JDA instance has completed
 * initialization.
 */
public class Ready extends ListenerAdapter
{
    public void onReady(ReadyEvent event)
    {
        if (!testDatabase())
        {
            // kill the JDA instance if database is not working properly
            thermo.shutdownNow();
            System.out.println("A database connection could not be made!\nCheck your login details in Connection.java.\nBot instance shutting down...");
            return;
        }

        ChannelListener CL = new ChannelListener();

        // Connection Listeners
        // thermo.addEventListener(new Reconnect());

        // Commands Listeners
        thermo.addEventListener(new Monitor());
        thermo.addEventListener(new UnMonitor());
        thermo.addEventListener(new GetMonitorList());
        thermo.addEventListener(new UnMonitorAll());
        thermo.addEventListener(new Info());
        //thermo.addEventListener(new ChannelSettings());

        // Other Event Listeners
        thermo.addEventListener(new GuildJoin());
        thermo.addEventListener(new GuildLeave());
    }

    /**
     * Checks if database connection can be made.
     * @return boolean value; true if database can be reached; false if not
     */
    public boolean testDatabase()
    {
        try
        {
            Connection connection = new Connection();
            connection.closeConnection();
        } catch (SQLException ex)
        {
            return false;
        }
        return true;
    }
}
