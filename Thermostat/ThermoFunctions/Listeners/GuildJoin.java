package Thermostat.ThermoFunctions.Listeners;

import Thermostat.MySQL.Connection;
import Thermostat.MySQL.Create;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * <h1>Guild Join Listener</h1>
 * <p>
 * Adds a guild to the database provided in
 * {@link Connection}, upon onGuildJoin event
 * occurrence. Extends ListenerAdapter thus must
 * be added as a listener in {@link Thermostat.thermostat}.
 */

public class GuildJoin extends ListenerAdapter
{
    public void onGuildJoin(GuildJoinEvent ev)
    {
        Create.Guild(ev.getGuild().getId());
    }
}
