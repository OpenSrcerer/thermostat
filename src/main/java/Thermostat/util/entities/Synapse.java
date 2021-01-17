package thermostat.util.entities;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.Thermostat;
import thermostat.commands.monitoring.SynapseMonitor;
import thermostat.mySQL.DataSource;
import thermostat.mySQL.Delete;
import thermostat.util.enumeration.SynapseState;

import javax.annotation.Nonnull;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
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
     * The ChronoUnit that the Synapse uses to measure message time.
     */
    private static final ChronoUnit millis = ChronoUnit.MILLIS;

    /**
     * The ID of the Guild that the Synapse owes to.
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
     * @param guildId ID of Guild to check.
     */
    public Synapse(@Nonnull String guildId) {
        this.guildId = guildId;
        this.monitoredChannels = initializeMonitoredChannels(guildId);

        try {
            setMessageCachingSize();
        } catch (SQLException ex) {
            lgr.warn("Failed to retrieve caching size for " + this.guildId + ". Defaulting to 10.");
            messageCachingSize = 10;
        }
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
    public synchronized void addMessage(String channelId, OffsetDateTime messageTime) {
        if (monitoredChannels.get(channelId) == null) {
            return;
        }

        monitoredChannels.get(channelId).addFirst(messageTime);

        if (monitoredChannels.get(channelId).size() == messageCachingSize) {
            new SynapseMonitor(this);
        }
    }

    @Nonnull
    private static Map<String, LinkedList<OffsetDateTime>> initializeMonitoredChannels(String guildId) {
        Map<String, LinkedList<OffsetDateTime>> monChannels = new HashMap<>();

        ArrayList<String> databaseMonitoredChannels = DataSource.queryStringArray("SELECT CHANNELS.CHANNEL_ID FROM CHANNELS " +
                "JOIN CHANNEL_SETTINGS ON (CHANNELS.CHANNEL_ID = CHANNEL_SETTINGS.CHANNEL_ID) " +
                "WHERE CHANNELS.GUILD_ID = ? AND CHANNEL_SETTINGS.MONITORED = 1", guildId);

        Guild guild = Thermostat.thermo.getGuildById(guildId);
        if (guild == null || databaseMonitoredChannels == null) {
            return new HashMap<>();
        }

        // Get all channel ids from list of text channels
        List<String> channelsInGuild = guild.getTextChannels().stream().map(ISnowflake::getId).collect(Collectors.toList());

        for (String channel : databaseMonitoredChannels) {
            if (channelsInGuild.contains(channel)) {
                monChannels.put(channel, new LinkedList<>());
            } else {
                Delete.Channel(guildId, channel);
            }
        }

        return monChannels;
    }

    /**
     * Set the caching size for the message date Linked List.
     * Retrieves the size from the database. Called upon for first initialization.
     * Default: 10
     */
    public void setMessageCachingSize() throws SQLException {
        this.messageCachingSize = DataSource.execute(conn ->
        {
            PreparedStatement statement = conn.prepareStatement("SELECT CACHING_SIZE FROM GUILDS WHERE GUILD_ID = ?");
            statement.setString(1, guildId);
            ResultSet rs = statement.executeQuery();
            return rs.getInt(1);
        });
    }

    /**
     * Set the caching size for the message date Linked List.
     * Trims list of message dates if cache is smaller than current size.
     * @param newSize New cache size.
     */
    public synchronized void setMessageCachingSize(int newSize) {
        if (newSize < messageCachingSize) {
            int cacheDifference = messageCachingSize - newSize;

            // Trim unneeded messages to match new size
            for (Map.Entry<String, LinkedList<OffsetDateTime>> entry : monitoredChannels.entrySet()) {
                // if list has more elements than the new size
                if (entry.getValue().size() > newSize) {
                    // remove all unnecessary ones
                    for (int index = 0; index < cacheDifference; ++index) {
                        entry.getValue().remove();
                    }
                }
            }
        }

        this.messageCachingSize = newSize;
    }

    /**
     * A function that adjusts the slowmode for the given channel.
     *
     * @param channel TextChannel that will have the slowmode adjusted.
     * @param time    Int representing the adjustment time.
     */
    private static void putSlowmode(TextChannel channel, int time) {
        // gets the maximum and minimum slowmode values
        // from the database.
        int min = DataSource.queryInt("SELECT MIN_SLOW FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = ?", channel.getId());
        int max = DataSource.queryInt("SELECT MAX_SLOW FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = ?", channel.getId());

        int slowmodeToSet;

        try {
            // gets the current slowmode in the channel
            int slow = channel.getSlowmode();

            // if slowmode and the added time exceed the max slowmode
            if (slow + time > max && max > 0) {
                // sets to max DATABASE slowmode value and exits
                slowmodeToSet = max;
            } else // if it's less than minimum DB value
                // sets it to that minimum value
                // otherwise just sets it
                if (slow + time > TextChannel.MAX_SLOWMODE) {
                    // sets to max DISCORD slowmode value
                    slowmodeToSet = TextChannel.MAX_SLOWMODE;
                } else slowmodeToSet = Math.max(slow + time, min);

            channel.getManager().setSlowmode(slowmodeToSet)
                    .queue(null, throwable ->
                            lgr.info("Failed to set slowmode on channel "
                                    + channel.getName() + " of Guild " +
                                    channel.getGuild().getName() +
                                    ". Cause:" + throwable.getMessage()
                            )
                    );

            // Adds +1 when slowmode turns on for the first time. (Charting)
            if (slow == min && slowmodeToSet > min) {
                DataSource.update("UPDATE CHANNELS SET MANIPULATED = MANIPULATED + 1 WHERE CHANNEL_ID = ? AND GUILD_ID = ?",
                        channel.getId(), channel.getGuild().getId());
            }

        } catch (Exception ex) {
            lgr.info("Failed to set slowmode on channel "
                    + channel.getName() + " of Guild " +
                    channel.getGuild().getName() +
                    ". Cause:", ex
            );
        }
    }

    /**
     * Calculates the slowmode for a certain channel. See function above.
     *
     * @param channel          The channel that will have the slowmode adjusted.
     * @param averageDelay     The average delay between the past 25 messages.
     * @param firstMessageTime How long it has passed since the last message was sent.
     */
    private static void slowmodeSwitch(TextChannel channel, Long averageDelay, Long firstMessageTime) {
        if (channel == null) {
            return;
        }

        float offset = DataSource.querySens("SELECT SENSOFFSET FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = ?", channel.getId());

        // accounting for each delay of the messages
        // this function picks an appropriate slowmode
        // adjustment number for each case.
        if ((averageDelay <= 100 * offset) && (firstMessageTime > 0 && firstMessageTime <= 1000)) {
            putSlowmode(channel, 20);
        } else if ((averageDelay <= 250 * offset) && (firstMessageTime > 0 && firstMessageTime <= 2500)) {
            putSlowmode(channel, 10);
        } else if ((averageDelay <= 500 * offset) && (firstMessageTime > 0 && firstMessageTime <= 5000)) {
            putSlowmode(channel, 6);
        } else if ((averageDelay <= 750 * offset) && (firstMessageTime > 0 && firstMessageTime <= 8000)) {
            putSlowmode(channel, 4);
        } else if ((averageDelay <= 1000 * offset) && (firstMessageTime > 0 && firstMessageTime <= 10000)) {
            putSlowmode(channel, 2);
        } else if ((averageDelay <= 1250 * offset) && (firstMessageTime > 0 && firstMessageTime <= 10000)) {
            putSlowmode(channel, 1);
        } else if ((averageDelay <= 1500 * offset) && (firstMessageTime > 0 && firstMessageTime <= 10000)) {
            putSlowmode(channel, 0);
        } else if ((firstMessageTime > 0 && firstMessageTime <= 10000) || (averageDelay < 2000 && averageDelay >= 1500)) {
            putSlowmode(channel, -1);
        } else if ((firstMessageTime > 10000 && firstMessageTime <= 30000) || (averageDelay < 2500 && averageDelay >= 2000)) {
            putSlowmode(channel, -2);
        } else if ((firstMessageTime > 30000 && firstMessageTime <= 60000) || averageDelay >= 2500) {
            putSlowmode(channel, -4);
        }
    }

    /**
     * Calculates an average of the delay time between
     * each message.
     *
     * @param messageTimes A list containing Discord sent
     *                 message times.
     * @return A long value, with the average time.
     */
    private static long calculateAverageTime(List<OffsetDateTime> messageTimes) {
        long sum = 0;

        if (messageTimes.isEmpty())
            return 0;

        for (int index = 0; index < messageTimes.size() - 1; ++index) {
            sum += ChronoUnit.MILLIS.between(messageTimes.get(index + 1), messageTimes.get(index));
        }
        return sum / messageTimes.size();
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
        final StringBuilder adjusted = new StringBuilder(), notAdjusted = new StringBuilder();

        if (synapseGuild == null) {
            lgr.info("[Slowmode Dispatch] Guild was null - ID: " + this.getGuildId());
            return;
        }

        // Go through every channel in the set and calculate the
        // necessary slowmode for every entry.
        for (Map.Entry<String, LinkedList<OffsetDateTime>> channelData : monitoredChannels.entrySet()) {
            TextChannel channel = synapseGuild.getTextChannelById(channelData.getKey());
            if (channel == null) {
                lgr.info("[Slowmode Dispatch] Channel was null." +
                        " - Guild: " + synapseGuild.getName() + " - Channel ID: " + channelData.getKey());
                notAdjusted.append(channelData.getKey()).append(" ");
                continue;
            } else if (channelData.getValue().size() < 10) {
                notAdjusted.append(channelData.getKey()).append(" ");
                continue;
            }

            if (millis.between(channelData.getValue().get(0), timeNow) > 60000) {
                putSlowmode(channel, Integer.MIN_VALUE);
            } else {
                slowmodeSwitch(
                        channel,
                        calculateAverageTime(channelData.getValue()),
                        millis.between(channelData.getValue().get(0), timeNow)
                );
                adjusted.append(channel.getName()).append(" ");
            }

            channelData.getValue().clear();
        }

        lgr.info("[Synapse Stats - " + synapseGuild.getName() + "] - Adjusted: [" + adjusted.toString() +
                "] - Not Adjusted: [" + notAdjusted + "]");
        // If none of the channels were slowmoded
        // deactivate the Synapse for the whole Guild
        if (adjusted.isEmpty()) {
            state = SynapseState.INACTIVE;
            lgr.info("Synapse deactivated for " + synapseGuild.getName());
        }
    }
}
