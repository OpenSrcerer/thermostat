package thermostat.thermoFunctions.commands.monitoring.synapses;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import thermostat.Thermostat;
import thermostat.thermoFunctions.entities.SynapseState;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Synapses are micromanager instances that are given a
 * sole job: Monitor each Guild for necessary slowmode
 * changes. One Synapse is attached to one Guild.
 */
public class Synapse {

    /**
     * Represents whether the Synapse is working or not.
     */
    private SynapseState state = SynapseState.ACTIVE;

    /**
     * The ID of the Guild that the Synapse owes to.
     */
    private final String guildId;

    /**
     * The monitored channels of the Guild.
     *
     * @see thermostat.thermoFunctions.commands.monitoring.MonitorCommand
     */
    private final Map<String, LinkedList<OffsetDateTime>> monitoredChannels;

    /**
     * The ChronoUnit that the Synapse uses to measure message time.
     */
    private static final ChronoUnit millis = ChronoUnit.MILLIS;

    /**
     * Create a new Synapse that checks a Guild periodically.
     * @param guildId ID of Guild to check.
     */
    public Synapse(@Nonnull String guildId) {
        this.guildId = guildId;
        this.monitoredChannels = SynapseFunctions.initializeMonitoredChannels(guildId);
    }

    /**
     * Iterate through each of the channels found in this Synapse's
     * monitoredChannels cache, and adjust their slowmode value
     * depending on a temporal average of the dispatch rate between
     * every message.
     * @param lgr Logger to use for logging.
     */
    public void monitorChannels(Logger lgr) {
        OffsetDateTime timeNow = OffsetDateTime.now().toInstant().atOffset(ZoneOffset.UTC).truncatedTo(millis);
        Guild synapseGuild = Thermostat.thermo.getGuildById(this.getGuildId());
        int slowmodedChannels = 0;

        if (synapseGuild == null) {
            lgr.info("Guild was null during periodic slowmode dispatch.\n ID: " + this.getGuildId());
            return;
        }

        // Go through every channel in the set and calculate the
        // necessary slowmode for every entry.
        for (Map.Entry<String, LinkedList<OffsetDateTime>> channelData : monitoredChannels.entrySet()) {
            TextChannel channel = synapseGuild.getTextChannelById(channelData.getKey());
            if (channel == null) {
                lgr.info("Channel was null during periodic slowmode dispatch.\n" +
                        "Guild Name: " + synapseGuild.getName() + "\nChannel ID: " + channelData.getKey());
                continue;
            } else if (channelData.getValue().size() < 10) {
                lgr.info("Channel shown below had below 10 messages sent after Thermostat's bootup.\n" +
                        "Guild Name: " + synapseGuild.getName() + "\nChannel ID: " + channel.getName());
                continue;
            }

            if (millis.between(channelData.getValue().get(0), timeNow) > 60000) {
                SynapseFunctions.putSlowmode(channel, Integer.MIN_VALUE);
            } else {
                SynapseFunctions.slowmodeSwitch(
                        channel,
                        SynapseFunctions.calculateAverageTime(channelData.getValue()),
                        millis.between(channelData.getValue().get(0), timeNow)
                );
                ++slowmodedChannels;
            }
        }

        // If none of the channels were slowmoded
        // deactivate the Synapse for the whole Guild
        if (slowmodedChannels == 0) {
            state = SynapseState.INACTIVE;
            lgr.info("Synapse deactivated for " + synapseGuild.getName());
        }
    }

    /**
     * Adds a new message's creation time in the last
     * 10 messages cache.
     * @param channelId ID of channel that the message belongs to.
     * @param messageTime Creation time of message.
     */
    public void addMessage(String channelId, OffsetDateTime messageTime) {
        if (monitoredChannels.get(channelId) == null) {
            return;
        }

        if (monitoredChannels.get(channelId).size() == 10) {
            monitoredChannels.get(channelId).removeLast();
        }
        monitoredChannels.get(channelId).addFirst(messageTime);
    }

    /**
     * Adds a new channel in synapse's monitoring cache.
     * @param channelId ID of channel to monitor.
     */
    public void addChannel(String channelId) {
        monitoredChannels.put(channelId, new LinkedList<>());
    }

    /**
     * Removes a TextChannel from the monitoredChannels
     * ArrayList (only used when a TextChannel is unmonitored/removed).
     */
    public void removeChannel(String channelId) {
        monitoredChannels.keySet().removeIf(channel -> channel.equals(channelId));
    }

    /**
     * Gives back all the monitored channels for this Synapse.
     * @return Monitored TextChannel Map for this Synapse
     */
    public Set<String> getChannels() {
        return monitoredChannels.keySet();
    }

    /**
     * Getter for the Guild the Synapse monitors.
     * @return ID of Guild that Synapse monitors.
     */
    public String getGuildId() {
        return guildId;
    }

    /**
     * Set the state of the Synapse.
     * @param state New state of Synapse.
     */
    public synchronized void setState(SynapseState state) {
        this.state = state;
    }

    /**
     * @return The caching state of the Synapse.
     */
    public SynapseState getState() {
        return state;
    }
}
