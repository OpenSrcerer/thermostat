package thermostat.thermoFunctions.commands.monitoring.synapses;

import thermostat.thermoFunctions.entities.SynapseState;

import javax.annotation.Nonnull;
import java.util.ArrayList;

/**
 * Synapses are micromanager instances that are given a
 * sole job: Monitor each Guild for necessary slowmode
 * changes. One Synapse is attached to one Guild.
 */
public class Synapse {

    /**
     * Represents whether the Synapse is cached or not.
     */
    private SynapseState state = SynapseState.ACTIVE;

    /**
     * The ID of the Guild that the Synapse owes to.
     */
    private final String guildId;

    /**
     * The monitored channels of the Guild.
     * Monitored with MonitorCommand.
     *
     * @see thermostat.thermoFunctions.commands.monitoring.MonitorCommand
     */
    private final ArrayList<String> monitoredChannels;

    /**
     * Create a new Synapse that checks a Guild periodically.
     * @param guildId ID of Guild to check.
     */
    public Synapse(@Nonnull String guildId) {
        this.guildId = guildId;
        this.monitoredChannels = SynapseFunctions.initializeMonitoredChannels(guildId);
    }

    /**
     * Set the state of the Synapse.
     * @param state New state of Synapse.
     */
    public synchronized void setState(SynapseState state) {
        this.state = state;
    }

    /**
     * Adds a new channel in synapse's monitoring cache.
     * @param channelId ID of channel to monitor.
     */
    public void addChannel(String channelId) {
        monitoredChannels.add(channelId);
    }

    /**
     * Removes a TextChannel from the monitoredChannels
     * ArrayList (only used when a TextChannel is unmonitored/removed).
     */
    public void removeChannel(String channelId) {
        monitoredChannels.removeIf(channel -> channel.equals(channelId));
    }

    /**
     * Gives back all the monitored channels for the Synapse.
     * @return Monitored TextChannel ID-s for the Synapse
     */
    public ArrayList<String> getChannels() {
        return monitoredChannels;
    }

    /**
     * Getter for the Guild the Synapse monitors.
     * @return ID of Guild that Synapse monitors.
     */
    public String getGuildId() {
        return guildId;
    }

    /**
     * @return The caching state of the Synapse.
     */
    public SynapseState getState() {
        return state;
    }
}
