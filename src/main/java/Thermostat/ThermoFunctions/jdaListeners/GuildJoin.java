package thermostat.thermoFunctions.jdaListeners;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import thermostat.mySQL.Create;

/**
 * Guild Join Listener
 * Adds a guild to the database provided in
 * db.properties, upon onGuildJoin event.
 */

public class GuildJoin extends ListenerAdapter {
    public void onGuildJoin(GuildJoinEvent ev) {
        Create.Guild(ev.getGuild().getId());
    }
}
