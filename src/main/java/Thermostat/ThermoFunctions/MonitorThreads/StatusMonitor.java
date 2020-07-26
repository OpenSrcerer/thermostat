package Thermostat.ThermoFunctions.MonitorThreads;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static Thermostat.thermostat.thermo;

/**
 * <h1>StatusMonitor</h1>
 * Keeps the status of the bot updated
 * with the proper number of servers.
 */
public class StatusMonitor
{
    private ScheduledExecutorService statusScheduler = Executors.newSingleThreadScheduledExecutor();

    public StatusMonitor() {
        Runnable status = this::setStatusWithGuildNumber;
        statusScheduler.scheduleAtFixedRate(status, 1, 60, TimeUnit.SECONDS);
    }

    public void setStatusWithGuildNumber()
    {
        thermo.getPresence().setPresence(OnlineStatus.ONLINE, Activity.watching(thermo.getGuilds().size() + " servers ~ Prefix: th!"));
    }
}
