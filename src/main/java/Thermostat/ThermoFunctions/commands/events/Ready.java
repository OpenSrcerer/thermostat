package thermostat.thermoFunctions.commands.events;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.Thermostat;
import thermostat.dispatchers.SynapseDispatcher;
import thermostat.thermoFunctions.commands.CommandTrigger;
import thermostat.thermoFunctions.commands.utility.WordFilterCommand;

import javax.annotation.Nonnull;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
        try {
            initializeWordFiles("niceWords.txt", "badWords.txt");
        } catch (Exception ex) {
            shutdownThermostat();
            lgr.error("Word files could not be set up!\nBot instance shutting down...");
            return;
        }

        thermo.addEventListener(
                new CommandTrigger(),
                new MessageDeleteEvent(),
                new ReactionAddEvent(),
                new SynapseEvents()
        );

        SynapseDispatcher.initializeSynapses();
        getConnectedGuilds();
        thermo.getPresence().setPresence(OnlineStatus.ONLINE, Activity.streaming("@Thermostat prefix", "https://github.com/opensrcerer/thermostat"));
    }

    public void initializeWordFiles(@Nonnull String file1, @Nonnull String file2) throws Exception {
        ArrayList<String> niceWords, badWords;

        niceWords = retrieveWordFile(file1);
        badWords = retrieveWordFile(file2);

        WordFilterCommand.setWordArrays(niceWords, badWords);
        lgr.info("Loaded word files " + file1 + " and " + file2);
    }

    private static ArrayList<String> retrieveWordFile(String fileName) throws Exception {
        ArrayList<String> wordFileArray = new ArrayList<>();

        InputStream fileStream = Thermostat.class.getClassLoader().getResourceAsStream(fileName);

        if (fileStream == null) {
            throw new FileNotFoundException("Could not find a file with given file name.");
        }

        Scanner scanner = new Scanner(fileStream);
        scanner.useDelimiter(",+");

        while (scanner.hasNext()) {
            wordFileArray.add(scanner.next());
        }

        return wordFileArray;
    }

    /** Prints out list of currently connected guilds
     * with names, owner ids, and guild ids. */
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
