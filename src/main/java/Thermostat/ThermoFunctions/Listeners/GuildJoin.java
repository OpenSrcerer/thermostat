package thermostat.thermoFunctions.listeners;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import thermostat.mySQL.Create;

/**
 * <h1>Guild Join Listener</h1>
 * <p>
 * Adds a guild to the database provided in
 * db.properties, upon onGuildJoin event
 * occurrence. Extends ListenerAdapter thus must
 * be added as a listener in {@link thermostat.thermostat}.
 */

public class GuildJoin extends ListenerAdapter {
    public void onGuildJoin(GuildJoinEvent ev) {
        Create.Guild(ev.getGuild().getId());
    }
}
