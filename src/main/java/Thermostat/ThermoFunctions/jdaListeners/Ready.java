package thermostat.thermoFunctions.jdaListeners;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.Thermostat;
import thermostat.dispatchers.SynapseDispatcher;
import thermostat.mySQL.DataSource;
import thermostat.thermoFunctions.commands.CommandTrigger;
import thermostat.thermoFunctions.commands.events.SynapseEvents;
import thermostat.thermoFunctions.monitorThreads.MessageReceived;
import thermostat.thermoFunctions.threaded.InitWordFiles;

import javax.annotation.Nonnull;
import java.util.List;

import static thermostat.Thermostat.shutdownThermostat;
import static thermostat.Thermostat.thermo;

/**
 * Listener class that runs the rest of the
 * bot once the JDA instance has completed
 * initialization.
 */
public class Ready extends ListenerAdapter {
    private static final Logger lgr = LoggerFactory.getLogger(Thermostat.class);
    public void onReady(@Nonnull ReadyEvent event) {

        DataSource.getDataSource();

        if (!new InitWordFiles("niceWords.txt", "badWords.txt").call()) {
            shutdownThermostat();
            lgr.error("Word files could not be set up!\nBot instance shutting down...");
            return;
        }

        thermo.addEventListener(
                new CommandTrigger(),
                // Other Event Listeners
                new GuildJoin(),
                new GuildLeave(),
                new MessageDeleteEvent(),
                new ReactionAddEvent(),
                new MessageReceived(),
                new ChannelDeleteEvent(),
                new SynapseEvents()
        );

        SynapseDispatcher.initializeSynapses();
        getConnectedGuilds();
        thermo.getPresence().setPresence(OnlineStatus.ONLINE, Activity.streaming("@Thermostat prefix", "https://github.com/opensrcerer/thermostat"));
    }

    // Prints out list of currently connected guilds
    // with names, owner ids, and guild ids.
    private void getConnectedGuilds() {
        List<Guild> guildList = thermo.getGuilds();

        guildList.forEach(
                element -> {
                    System.out.print(element.getName() + " - ");
                    System.out.println(element.getId());
                }
        );
    }
}
