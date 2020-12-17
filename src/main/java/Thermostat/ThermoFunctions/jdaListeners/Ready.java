package thermostat.thermoFunctions.jdaListeners;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.discordbots.api.client.entity.Vote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.managers.RequestManager;
import thermostat.mySQL.DataSource;
import thermostat.thermoFunctions.commands.requestFactories.CommandTrigger;
import thermostat.thermoFunctions.commands.requestListeners.informational.ChartListener;
import thermostat.thermoFunctions.commands.requestListeners.informational.GetMonitorListener;
import thermostat.thermoFunctions.commands.requestListeners.informational.SettingsListener;
import thermostat.thermoFunctions.commands.requestListeners.monitoring.MonitorListener;
import thermostat.thermoFunctions.commands.requestListeners.monitoring.SensitivityListener;
import thermostat.thermoFunctions.commands.requestListeners.monitoring.SetBoundsListener;
import thermostat.thermoFunctions.commands.requestListeners.other.InfoListener;
import thermostat.thermoFunctions.commands.requestListeners.other.InviteListener;
import thermostat.thermoFunctions.commands.requestListeners.other.PrefixListener;
import thermostat.thermoFunctions.commands.requestListeners.other.VoteListener;
import thermostat.thermoFunctions.commands.requestListeners.utility.FilterListener;
import thermostat.thermoFunctions.commands.requestListeners.utility.WordFilterListener;
import thermostat.thermoFunctions.monitorThreads.DBLServerMonitor;
import thermostat.thermoFunctions.monitorThreads.MessageReceived;
import thermostat.thermoFunctions.monitorThreads.WorkerManager;
import thermostat.thermoFunctions.threaded.InitWordFiles;
import thermostat.thermostat;

import javax.annotation.Nonnull;
import java.util.List;

import static thermostat.thermostat.thermo;

/**
 * Listener class that runs the rest of the
 * bot once the JDA instance has completed
 * initialization.
 */
public class Ready extends ListenerAdapter {
    private static final Logger lgr = LoggerFactory.getLogger(thermostat.class);

    public void onReady(@Nonnull ReadyEvent event) {

        DataSource.getDataSource();

        if (!new InitWordFiles("niceWords.txt", "badWords.txt").call()) {
            DataSource.killDataSource();
            thermo.shutdownNow();
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
                new ChannelDeleteEvent()
        );

        RequestManager.addListener(
                new ChartListener(),
                new GetMonitorListener(),
                new SettingsListener(),
                new MonitorListener(),
                new SensitivityListener(),
                new SetBoundsListener(),
                new InfoListener(),
                new InviteListener(),
                new PrefixListener(),
                new VoteListener(),
                new FilterListener(),
                new WordFilterListener()
        );

        DBLServerMonitor.getInstance();
        WorkerManager.getInstance();
        getConnectedGuilds();
        thermo.getPresence().setPresence(OnlineStatus.ONLINE, Activity.streaming("@Thermostat prefix", "https://www.youtube.com/watch?v=fC7oUOUEEi4"));
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
