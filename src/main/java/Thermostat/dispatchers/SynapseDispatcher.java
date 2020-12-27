package thermostat.dispatchers;

import thermostat.Thermostat;
import thermostat.thermoFunctions.commands.monitoring.synapses.Synapse;
import thermostat.thermoFunctions.commands.monitoring.synapses.SynapseMonitor;
import thermostat.thermoFunctions.entities.SynapseState;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * A manager type of class that organizes all synapses.
 * Main purpose queuing them for periodic server slowmode
 * updates.
 */
public final class SynapseDispatcher {
    private static final ArrayList<Synapse> synapses = new ArrayList<>();

    /**
     * Initializes the Runnable that will be scheduled for
     * SynapseMonitor requests.
     */
    public static void initializeSynapses() {
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
     * Creates a new Synapse object to manage a Guild's
     * slowmode.
     * @param guildId ID of Guild to manage.
     */
    public static synchronized Synapse addSynapse(String guildId) {
        Synapse newSynapse = new Synapse(guildId);
        synapses.add(newSynapse);
        return newSynapse;
    }

    /**
     * Removes a Synapse object from the synapses
     * ArrayList (only used when a guild is removed).
     */
    public static synchronized void removeSynapse(String guildId) {
        synapses.removeIf(synapse -> synapse.getGuildId().equals(guildId));
    }
}