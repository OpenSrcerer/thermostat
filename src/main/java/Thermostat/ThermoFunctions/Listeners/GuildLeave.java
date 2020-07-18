package Thermostat.ThermoFunctions.Listeners;

import Thermostat.MySQL.Delete;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * <h1>Guild Leave Listener</h1>
 * <p>
 * Removes a guild from the database provided in
 * db.properties, upon onGuildLeave event
 * occurrence. Extends ListenerAdapter thus must
 * be added as a listener in {@link Thermostat.thermostat}.
 */
public class GuildLeave extends ListenerAdapter
{
    public void onGuildLeave(GuildLeaveEvent ev)
    {
        Delete.Guild(ev.getGuild().getId());
    }
}
