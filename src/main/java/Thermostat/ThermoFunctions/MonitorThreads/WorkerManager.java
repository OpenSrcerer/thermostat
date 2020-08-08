package Thermostat.ThermoFunctions.MonitorThreads;

import Thermostat.MySQL.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.*;

/**
 * <h1>WorkerManager</h1>
 * <p>
 * Class that manages every Guild's monitoring.
 * Adds / Removes instances as per database, which is checked
 * every 5 seconds for changes.
 * Each instance is managed by the Worker class.
 * @see Worker
 */
public class WorkerManager
{
    protected static ScheduledExecutorService scheduledExecutorService;
    // ActiveWorkers array used for maintaining threads working on monitoring
    private static ArrayList<Worker> activeWorkers = new ArrayList<>();

    /**
     * Gives back an array of currently active
     * guild workers.
     * @return The array of guild workers.
     */
    public static ArrayList<Worker> getActiveWorkers() {
        return activeWorkers;
    }

    public static void setActiveWorkers(ArrayList<Worker> workers) {
        activeWorkers = workers;
    }

    /**
     * Constructor, called once in the main function
     * of the {@link Thermostat.thermostat class}.
     */
    public WorkerManager()
    {
        Runnable setup = WorkerManager::setupMonitoring;
        scheduledExecutorService = new ScheduledThreadPoolExecutor(4);

        // returning ScheduledFuture is ignored because
        // there's no need to modify the main scheduler
        // it only turns off once the whole program shuts down
        scheduledExecutorService.scheduleAtFixedRate(setup, 2, 5, TimeUnit.SECONDS);
    }

    /**
     * This is the function that gets called periodically
     * by the Scheduling Thread of this listener. Grabs Guilds
     * from the database, and adjusts the thread number
     * accordingly depending on whether Guilds were recently
     * added or removed from the database.
     */
    public static void setupMonitoring() {
        try {

            // list that will hold all guilds taken from the DB
            ArrayList<String> GUILDS = DataSource.query("SELECT GUILD_ID FROM GUILDS");

            // case 1: when the guilds array is larger than the current active workers,
            // it means that there were guilds recently added
            if (GUILDS.size() > activeWorkers.size()) {
                // only one list modification is allowed
                // in order to not throw a java.util.concurrentModificationException
                ArrayList<Worker> workerArrayList = new ArrayList<>();

                // iterates through guilds and activeworkers
                // checking for lone guilds with no active worker
                for (String it1 : GUILDS) {
                    boolean found = false;

                    for (Worker it2 : activeWorkers) {
                        if (it2.getAssignedGuild().equals(it1)) {
                            // if a match is found, leave it alone
                            // but check if it's running
                            it2.scheduleWorker();
                            found = true;
                            break;
                        }
                    }

                    // if a match is not found, create new worker thread for guild
                    if (!found) {
                        // adds worker to active worker array with assigned guild
                        Worker worker = new Worker();
                        worker.setAssignedGuild(it1);
                        workerArrayList.add(worker);
                    }
                }
                // passes values through the collection GWArrayList for concurrency
                activeWorkers.addAll(workerArrayList);
            }
            // case 2: when the guilds array is smaller than the current active workers,
            // it means that there were guilds recently expunged from the database
            else if (GUILDS.size() < activeWorkers.size()) {
                // only one list modification is allowed
                // in order to not throw a java.util.concurrentModificationException

                // list used to remove
                ArrayList<Worker> workersToRemove = new ArrayList<>();

                // iterates through channel workers and guilds
                // checking for lone channel workers with no matching guilds
                for (Worker it1 : activeWorkers) {
                    boolean found = false;

                    for (String it2 : GUILDS) {
                        if (it1.getAssignedGuild().equals(it2)) {
                            // if a match is found, leave the thread alone
                            found = true;
                            break;
                        }
                    }

                    // if a match isn't found, invalidate the ScheduledFuture
                    if (!found) {
                        it1.invalidate();
                        workersToRemove.add(it1);
                    }
                }
                activeWorkers.removeAll(workersToRemove);
            }

        } catch (Exception ex)
        {
            Logger lgr = LoggerFactory.getLogger(DataSource.class);
            lgr.error(ex.getMessage(), ex);
        }
    }
}
