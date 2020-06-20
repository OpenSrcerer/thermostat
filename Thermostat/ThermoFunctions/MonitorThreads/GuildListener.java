package Thermostat.ThermoFunctions.MonitorThreads;

import Thermostat.MySQL.Connection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.*;

import static Thermostat.thermostat.thermo;

/**
 * <h1>Channel Listener</h1>
 * <p>
 * Class that manages every Guild's monitoring threads.
 * Adds / Removes instances as per database, which is checked
 * every 5 seconds for changes.
 * Each instance is managed by a ChannelWorker class.
 * @see ChannelWorker
 */
public class GuildListener
{
    protected static ScheduledExecutorService SES;
    // ActiveWorkers array used for maintaining threads working on monitoring
    private static ArrayList<ChannelWorker> activeWorkers = new ArrayList<>();

    /**
     * Constructor, called once in the main function
     * of the {@link Thermostat.thermostat class}.
     */
    public GuildListener()
    {
        Runnable setup = GuildListener::setupMonitoring;
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        executor.setRemoveOnCancelPolicy(true);
        SES = executor;

        // returning ScheduledFuture is ignored because
        // there's no need to modify the main scheduler
        // it only turns off once the whole program shuts down
        SES.scheduleAtFixedRate(setup, 2, 5, TimeUnit.SECONDS);
    }

    /**
     * Tells the scheduledexecutor to shut down.
     * Kills the thread pool.
     */
    public void shutdown()
    {
        SES.shutdown();
    }

    /**
     * This is the function that gets called periodically
     * by the Scheduling Thread of this listener. Grabs Guilds
     * from the database, and adjusts the thread number
     * accordingly depending on whether Guilds were recently
     * added or removed from the database.
     */
    public static void setupMonitoring () {
        try {
            // grabs guilds from the database
            Connection conn = new Connection();

            ResultSet rs = conn.query("SELECT GUILD_ID FROM GUILDS");
            // list that will hold all guilds from the database
            ArrayList<String> GUILDS = new ArrayList<>();
            try {
                while (rs.next()) {
                    GUILDS.add(rs.getString(1));
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            conn.closeConnection();

            // case 1: when the guilds array is larger than the current active workers,
            // it means that there were guilds recently added
            if (GUILDS.size() > activeWorkers.size()) {
                // only one list modification is allowed
                // in order to not throw a java.util.concurrentModificationException
                ArrayList<ChannelWorker> channelWorkerArrayList = new ArrayList<>();

                // iterates through guilds and activeworkers
                // checking for lone guilds with no active worker
                for (String it1 : GUILDS) {
                    boolean found = false;

                    for (ChannelWorker it2 : activeWorkers) {
                        if (it2.getAssignedGuild().equals(it1)) {
                            // if a match is found, leave it alone
                            // but check if it's running
                            it2.scheduleWorker(thermo);
                            found = true;
                            break;
                        }
                    }

                    // if a match is not found, create new worker thread for guild
                    if (!found) {
                        // adds worker to active worker array with assigned guild
                        ChannelWorker GW = new ChannelWorker();
                        GW.setAssignedGuild(it1);
                        channelWorkerArrayList.add(GW);
                    }
                }
                // passes values through the collection GWArrayList for concurrency
                activeWorkers.addAll(channelWorkerArrayList);
            }
            // case 2: when the guilds array is smaller than the current active workers,
            // it means that there were guilds recently expunged from the database
            else if (GUILDS.size() < activeWorkers.size()) {
                // only one list modification is allowed
                // in order to not throw a java.util.concurrentModificationException
                ArrayList<ChannelWorker> channelWorkerArrayList = new ArrayList<>();

                // iterates through channel workers and guilds
                // checking for lone channel workers with no matching guilds
                for (ChannelWorker it1 : activeWorkers) {
                    boolean found = false;

                    for (String it2 : GUILDS) {
                        if (it1.getAssignedGuild().equals(it2)) {
                            // if a match is found, leave the thread alone
                            found = true;
                            break;
                        }
                    }

                    // if a match isn't found, invalidate the ScheduledFuture
                    // of the runnable and kill the thread
                    if (!found) {
                        it1.invalidate();
                        channelWorkerArrayList.add(it1);
                    }
                }
                activeWorkers.removeAll(channelWorkerArrayList);
            }

        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
