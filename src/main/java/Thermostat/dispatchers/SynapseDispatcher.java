package thermostat.dispatchers;

import thermostat.Thermostat;
import thermostat.entities.Synapse;
import thermostat.commands.monitoring.SynapseMonitor;
import thermostat.enumeration.SynapseState;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

/**
 * A manager type of class that organizes all synapses.
 * Main purpose queuing them for periodic server slowmode
 * updates.
 */
public final class SynapseDispatcher {
    /**
     * A cache that contains Synapses.
     */
    private static final Map<String, Synapse> synapses = new WeakHashMap<>();

    /**
     * Initializes the Runnable that will be scheduled for
     * SynapseMonitor requests.
     */
    public static void initializeSynapses() {
        Runnable run = () -> {
            for (Map.Entry<String, Synapse> synapseEntry : synapses.entrySet()) {
                if (synapseEntry.getValue().getState() == SynapseState.ACTIVE) {
                    new SynapseMonitor(synapseEntry.getValue());
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
        if (synapses.get(guildId) == null) {
            return new Synapse("0");
        } else {
            return synapses.get(guildId);
        }
    }

    /**
     * Creates a new Synapse object to manage a Guild's
     * slowmode & returns it.
     * @param guildId ID of Guild to manage.
     */
    public static synchronized Synapse addSynapse(String guildId) {
        Synapse newSynapse = new Synapse(guildId);
        synapses.put(guildId, newSynapse);
        return newSynapse;
    }

    /**
     * Removes a Synapse object from the synapses
     * cache (only used when a guild is removed).
     */
    public static synchronized void removeSynapse(String guildId) {
        synapses.remove(guildId);
    }
}