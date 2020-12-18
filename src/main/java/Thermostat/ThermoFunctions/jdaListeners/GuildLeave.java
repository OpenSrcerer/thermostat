package thermostat.thermoFunctions.jdaListeners;

import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import thermostat.mySQL.Delete;
import thermostat.thermoFunctions.monitorThreads.Worker;
import thermostat.thermoFunctions.monitorThreads.WorkerManager;

/**
 * Removes a guild from the database provided in
 * db.properties, upon onGuildLeave event.
 */
public class GuildLeave extends ListenerAdapter {
    public void onGuildLeave(GuildLeaveEvent ev) {
        Delete.Guild(ev.getGuild().getId());

        for (Worker worker : WorkerManager.activeWorkers) {

        }
    }
}
