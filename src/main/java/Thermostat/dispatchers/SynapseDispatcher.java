package thermostat.dispatchers;

import net.dv8tion.jda.api.entities.ISnowflake;
import thermostat.mySQL.DataSource;
import thermostat.mySQL.Delete;
import thermostat.thermoFunctions.commands.monitoring.synapses.Synapse;
import thermostat.Thermostat;
import thermostat.thermoFunctions.commands.monitoring.synapses.SynapseMonitor;
import thermostat.thermoFunctions.entities.SynapseState;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * A manager type of class that organizes all synapses.
 * Main purpose queuing them for periodic server slowmode
 * updates.
 */
public final class SynapseDispatcher {
    private static final ArrayList<Synapse> synapses = new ArrayList<>();

    /**
     * Initializes the synapses cache with a synapse for every Guild Thermostat is in.
     */
    public static void initializeSynapses() {
        List<String> databaseGuilds = DataSource.queryStringArray("SELECT GUILD_ID FROM GUILDS", "");
        List<String> thermostatGuilds = Thermostat.thermo.getGuilds().stream().map(ISnowflake::getId).collect(Collectors.toList());

        if (databaseGuilds == null) {
            return;
        }

        for (String guild : databaseGuilds) {
            if (thermostatGuilds.contains(guild)) {
                synapses.add(new Synapse(guild));
            } else {
                Delete.Guild(guild);
            }
        }

        Runnable run = () -> {
            for (Synapse synapse : synapses) {
                if (synapse.getState() == SynapseState.ACTIVE) {
                    new SynapseMonitor(synapse);
                }
            }
        };

        Thermostat.executor.scheduleAtFixedRate(run, 0, 8, TimeUnit.SECONDS);
    }

    /**
     * Searches for a Synapse that manages a specific
     * Guild and then returns it.
     * @param guildId Guild's ID.
     * @return The Synapse that manages the Guild.
     */
    @Nonnull
    public static Synapse getSynapse(String guildId) {
        for (Synapse synapse : synapses) {
            if (synapse.getGuildId().equals(guildId)) {
                return synapse;
            }
        }

        return new Synapse("0");
    }

    /**
     * Removes a Synapse object from the synapses
     * ArrayList (only used when a guild is removed).
     */
    public static synchronized void removeSynapse(String guildId) {
        synapses.removeIf(synapse -> synapse.getGuildId().equals(guildId));
    }

    /**
     * Creates a new Synapse object to manage a Guild's
     * slowmode.
     * @param guildId ID of Guild to manage.
     */
    public static void addSynapse(String guildId) {
        Synapse newSynapse = new Synapse(guildId);
        synapses.add(newSynapse);
    }
}