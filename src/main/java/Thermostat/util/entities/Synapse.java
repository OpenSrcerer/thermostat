package thermostat.util.entities;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.Thermostat;
import thermostat.commands.monitoring.SynapseMonitor;
import thermostat.mySQL.DataSource;
import thermostat.mySQL.PreparedActions;
import thermostat.util.Constants;
import thermostat.util.enumeration.SynapseState;

import javax.annotation.Nonnull;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Synapses are micromanager instances that are given a
 * sole job: Monitor each Guild for necessary slowmode
 * changes. One Synapse is attached to one Guild.
 */
public class Synapse {
    // ***************************************************************
    // **                       VARIABLES                           **
    // ***************************************************************
    /**
     * Logger for Synapse operations.
     */
    private static final Logger lgr = LoggerFactory.getLogger(Synapse.class);

    /**
     * The ID of the Guild that the Synapse will constantly monitor.
     */
    private final String guildId;

    /**
     * The monitored channels of the Guild.
     * @see thermostat.commands.monitoring.MonitorCommand
     */
    private final Map<String, LinkedList<OffsetDateTime>> monitoredChannels;

    /**
     * Maximum size for the LinkedList that contains dates to the monitored
     * channels for each channel.
     */
    private int messageCachingSize;

    /**
     * Represents whether the Synapse is working or not.
     */
    private SynapseState state = SynapseState.ACTIVE;

    // ***************************************************************
    // **                CONSTRUCTOR/GETTERS/SETTERS                **
    // ***************************************************************

    /**
     * Create a new Synapse that checks a Guild periodically.
     * @param guildId ID of Guild to monitor.
     */
    public Synapse(@Nonnull String guildId) {
        this.guildId = guildId;
        this.monitoredChannels = initializeMonitoredChannels(guildId);
        initMessageCachingSize();
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
     * @return The caching state of the Synapse.
     */
    public SynapseState getState() {
        return state;
    }

    /**
     * Set the state of the Synapse.
     * @param state New state of Synapse.
     */
    public synchronized void setState(SynapseState state) {
        this.state = state;
    }

    // ***************************************************************
    // **                      MONITORING METHODS                   **
    // ***************************************************************

    /**
     * Adds a new message's creation time in the last
     * 10 messages cache.
     * @param channelId ID of channel that the message belongs to.
     * @param messageTime Creation time of message.
     */
    public synchronized void addMessage(final String channelId, final OffsetDateTime messageTime) {
        LinkedList<OffsetDateTime> channelMessages = monitoredChannels.get(channelId);
        if (channelMessages == null) {
            return;
        }
        channelMessages.addFirst(messageTime);
        if (channelMessages.size() >= messageCachingSize) {
            new SynapseMonitor(this, channelMessages, channelId);
        }
    }

    /**
     * Initializes monitored channels Map for a Synapse.
     * @param guildId ID of Synapse's guild.
     * @return A monitor Map for a Synapse.
     */
    @Nonnull
    private static Map<String, LinkedList<OffsetDateTime>> initializeMonitoredChannels(final String guildId) {
        Map<String, LinkedList<OffsetDateTime>> monChannels = new HashMap<>();

        try {
            DataSource.demand(conn -> {
                ArrayList<String> databaseMonitoredChannels = new ArrayList<>();
                PreparedStatement statement = conn.prepareStatement("SELECT CHANNELS.CHANNEL_ID FROM CHANNELS " +
                        "JOIN CHANNEL_SETTINGS ON (CHANNELS.CHANNEL_ID = CHANNEL_SETTINGS.CHANNEL_ID) " +
                        "WHERE CHANNELS.GUILD_ID = ? AND CHANNEL_SETTINGS.MONITORED = 1");
                statement.setString(1, guildId);
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    databaseMonitoredChannels.add(rs.getString(1));
                }

                Guild guild = Thermostat.thermo.getGuildById(guildId);
                if (guild == null || databaseMonitoredChannels.isEmpty()) {
                    return null;
                }

                // Get all channel ids from list of text channels
                List<String> channelsInGuild = guild.getTextChannels().stream().map(ISnowflake::getId).collect(Collectors.toList());

                for (final String channel : databaseMonitoredChannels) {
                    if (channelsInGuild.contains(channel)) {
                        monChannels.put(channel, new LinkedList<>());
                    } else {
                        PreparedActions.deleteChannel(conn, guildId, channel);
                    }
                }

                return null;
            });
        } catch (SQLException ex) {
            lgr.warn("Initializing Monitored Channels Failure for " + guildId + ". Falling back to default. Details:", ex);
        }

        return monChannels;
    }

    /**
     * Set the caching size for the message date Linked List.
     * Retrieves the size from the database. Called upon for first initialization.
     */
    public void initMessageCachingSize() {
        try {
            this.messageCachingSize = DataSource.demand(conn -> {
                PreparedStatement statement = conn.prepareStatement("SELECT CACHING_SIZE FROM GUILDS WHERE GUILD_ID = ?");
                statement.setString(1, guildId);
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    return Constants.DEFAULT_CACHING_SIZE;
                }
            });
        } catch (SQLException ex) {
            this.messageCachingSize = Constants.DEFAULT_CACHING_SIZE;
        }
        lgr.debug("Set message caching size " + this.messageCachingSize + " for " + this.guildId + ".");
    }

    /**
     * Set the caching size for the message date Linked List.
     * Trims list of message dates if cache is smaller than current size.
     * @param newSize New cache size.
     */
    public synchronized void setMessageCachingSize(final int newSize) {
        if (newSize < messageCachingSize) {
            int cacheDifference = messageCachingSize - newSize;

            // Trim unneeded messages to match new size
            for (Map.Entry<String, LinkedList<OffsetDateTime>> entry : monitoredChannels.entrySet()) {
                if (entry.getValue().size() > newSize) { // if list has more elements than the new size
                    for (int index = 0; index < cacheDifference; ++index) { // remove all unnecessary ones
                        entry.getValue().remove();
                    }
                }
            }
        }
        this.messageCachingSize = newSize;
    }

}
