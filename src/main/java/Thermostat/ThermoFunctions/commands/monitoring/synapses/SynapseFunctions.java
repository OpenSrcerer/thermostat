package thermostat.thermoFunctions.commands.monitoring.synapses;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.Thermostat;
import thermostat.mySQL.DataSource;
import thermostat.mySQL.Delete;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public final class SynapseFunctions {
    private static final Logger lgr = LoggerFactory.getLogger(SynapseFunctions.class);

    @Nonnull
    protected static Map<String, LinkedList<OffsetDateTime>> initializeMonitoredChannels(String guildId) {
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
     * A function that adjusts the slowmode for the given channel.
     *
     * @param channel TextChannel that will have the slowmode adjusted.
     * @param time    Int representing the adjustment time.
     */
    public static void putSlowmode(TextChannel channel, int time) {
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
                        Arrays.asList(channel.getId(), channel.getGuild().getId()));
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
    public static void slowmodeSwitch(TextChannel channel, Long averageDelay, Long firstMessageTime) {
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
    public static long calculateAverageTime(List<OffsetDateTime> messageTimes) {
        long sum = 0;

        if (messageTimes.isEmpty())
            return 0;

        for (int index = 0; index < messageTimes.size() - 1; ++index) {
            sum += ChronoUnit.MILLIS.between(messageTimes.get(index + 1), messageTimes.get(index));
        }
        return sum / messageTimes.size();
    }
}
