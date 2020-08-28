package thermostat.thermoFunctions.listeners;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.mySQL.DataSource;
import thermostat.thermoFunctions.commands.Command;
import thermostat.thermoFunctions.monitorThreads.DBLServerMonitor;
import thermostat.thermoFunctions.monitorThreads.MessageReceived;
import thermostat.thermoFunctions.monitorThreads.WorkerManager;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static thermostat.thermostat.thermo;

/**
 * Listener class that runs the rest of the
 * bot once the JDA instance has completed
 * initialization.
 */
public class Ready extends ListenerAdapter {
    public void onReady(@Nonnull ReadyEvent event) {
        if (!testDatabase()) {
            // kill the JDA instance if database is not working properly
            thermo.shutdownNow();
            System.out.println("A database connection could not be made!\nCheck your login details in db.properties.\nBot instance shutting down...");
            return;
        }

        thermo.addEventListener(
                new Command(),
                // Other Event Listeners
                new GuildJoin(),
                new GuildLeave(),
                new MessageDeleteEvent(),
                new ReactionAddEvent(),
                new MessageReceived(),
                new ChannelDeleteEvent()
        );
        DBLServerMonitor.getInstance();
        WorkerManager.getInstance();
        getConnectedGuilds();
        thermo.getPresence().setPresence(OnlineStatus.ONLINE, Activity.streaming("@Thermostat prefix", "https://www.youtube.com/watch?v=fC7oUOUEEi4"));
    }

    /**
     * Checks if database connection can be made.
     *
     * @return boolean value; true if database can be reached; false if not
     */

    @SuppressWarnings({"EmptyTryBlock"})
    public boolean testDatabase() {
        // sample connection to check if database
        // can be reached
        try (
                Connection ignored = DataSource.getConnection()
        ) {
        } catch (SQLException ex) {
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
    public void getConnectedGuilds() {
        List<Guild> guildList = thermo.getGuilds();

        guildList.forEach(
                element -> {
                    System.out.print(element.getName() + " - ");
                    System.out.println(element.getId());
                }
        );
    }
}
