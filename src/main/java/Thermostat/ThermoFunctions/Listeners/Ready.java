package Thermostat.ThermoFunctions.Listeners;

import Thermostat.MySQL.DataSource;
import Thermostat.ThermoFunctions.Commands.*;
import Thermostat.ThermoFunctions.MonitorThreads.MessageReceived;
import Thermostat.ThermoFunctions.MonitorThreads.WorkerManager;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

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
            System.out.println("A database connection could not be made!\nCheck your login details in db.properties.\nBot instance shutting down...");
            return;
        }

        // creates instance of WorkerManager for guild
        // updating
        WorkerManager workerManager = new WorkerManager();

        // thread to manage bot status monitoring (disabled)
        // StatusMonitor statusMonitor = new StatusMonitor();

        // Commands Listeners
        thermo.addEventListener(new Monitor());
        thermo.addEventListener(new UnMonitor());
        thermo.addEventListener(new GetMonitorList());
        thermo.addEventListener(new UnMonitorAll());
        thermo.addEventListener(new Info());
        thermo.addEventListener(new SetMaximum());
        thermo.addEventListener(new SetMinimum());
        thermo.addEventListener(new Settings());
        thermo.addEventListener(new Invite());
        thermo.addEventListener(new Vote());
        //thermo.addEventListener(new SetPrefix());
        //thermo.addEventListener(new GetPrefix());

        // Other Event Listeners
        thermo.addEventListener(new GuildJoin());
        thermo.addEventListener(new GuildLeave());
        thermo.addEventListener(new MessageDeleteEvent());
        thermo.addEventListener(new ReactionAddEvent());
        thermo.addEventListener(new MessageReceived());

        getConnectedGuilds();

        thermo.getPresence().setPresence(OnlineStatus.ONLINE, Activity.watching("🔥 burning channels! th!getprefix"));
    }

    /**
     * Checks if database connection can be made.
     * @return boolean value; true if database can be reached; false if not
     */
    public boolean testDatabase()
    {

        try (
                Connection conn = DataSource.getConnection();
        ) {}
        catch (SQLException ex) {
            Logger lgr = LoggerFactory.getLogger(DataSource.class);
            lgr.error("There's an error with your database login credentials! Check db.properties!", ex);
            return false;
        } catch (Exception ex) {
            Logger lgr = LoggerFactory.getLogger(DataSource.class);
            lgr.error("Could not find db.properties file!", ex);
            return false;
        }
        return true;
    }

    // Prints out list of currently connected guilds
    // with names, owner ids, and guild ids.
    public void getConnectedGuilds()
    {
        List<Guild> guildList = thermo.getGuilds();

        guildList.forEach(
            element -> {
                System.out.print(element.getName() + " - ");
                System.out.println(element.getId());
            }
        );
    }
}
