package thermostat.commands.monitoring;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.Thermostat;
import thermostat.commands.Command;
import thermostat.dispatchers.CommandDispatcher;
import thermostat.mySQL.DataSource;
import thermostat.mySQL.PreparedActions;
import thermostat.util.MiscellaneousFunctions;
import thermostat.util.entities.CommandData;
import thermostat.util.entities.Synapse;
import thermostat.util.enumeration.CommandType;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.LinkedList;

public class SynapseMonitor implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(SynapseMonitor.class);

    /**
     * Synapse that started this monitoring event.
     */
    private final Synapse synapse;

    /**
     * Data package for this command.
     */
    private final CommandData data;

    /**
     * ID of Channel to monitor.
     */
    private final String channelId;

    /**
     * List of channel's messages, according to the Synapse's caching size.
     */
    private final LinkedList<OffsetDateTime> channelMessages;

    /**
     * Create a new Monitor event for each Synapse.
     * @param synapse Synapse to take as an argument.
     */
    public SynapseMonitor(final Synapse synapse, final LinkedList<OffsetDateTime> channelMessages, final String channelId) {
        this.data = new CommandData(null);
        this.synapse = synapse;
        this.channelId = channelId;
        this.channelMessages = channelMessages;

        CommandDispatcher.queueCommand(this);
    }

    @Override
    public void run() {
        try {
            Guild synapseGuild = Thermostat.thermo.getGuildById(synapse.getGuildId()); // Retrieve Guild to monitor.
            if (synapseGuild == null) {
                lgr.info("[Slowmode Dispatch] Could not monitor null Guild - ID: " + synapse.getGuildId());
                return;
            }
            TextChannel channel = synapseGuild.getTextChannelById(channelId);
            if (channel == null) {
                lgr.info("[Slowmode Dispatch] Could not monitor null Channel" +
                        " - Guild ID: " + synapseGuild.getName() + " - Channel ID: " + channelId);
                return;
            }

            slowmodeSwitch(channel, MiscellaneousFunctions.calculateAverageTime(channelMessages));
            lgr.info("[Synapse Stats - " + synapseGuild.getName() + "] - Adjusted: [" + channel.getName() + "]");
            channelMessages.clear();
        } catch (SQLException ex) {
            lgr.info("Failure in monitoring Guild " + synapse.getGuildId() + ".", ex);
        }
    }

    /**
     * Calculates the slowmode for a certain channel.
     * @param channel          The channel that will have the slowmode adjusted.
     * @param averageDelay     The average delay between the past 25 messages.
     */
    private static void slowmodeSwitch(@Nullable final TextChannel channel, final long averageDelay) throws SQLException
    {
        DataSource.demand(conn -> {
            if (channel == null) {
                return null;
            }

            // gets the maximum and minimum slowmode values
            // from the database.
            PreparedStatement statement = conn.prepareStatement("SELECT MIN_SLOW, MAX_SLOW, SENSOFFSET " +
                    "FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = ?");
            statement.setString(1, channel.getId());
            ResultSet rs = statement.executeQuery();
            rs.next();

            int min = rs.getInt(1);
            int max = rs.getInt(2);
            float offset = rs.getFloat(3);

            // accounting for each delay of the messages
            // this function picks an appropriate slowmode
            // adjustment number for each case.
            if (averageDelay <= 100 * offset) {
                putSlowmode(conn, channel, 20, min, max);
            } else if (averageDelay <= 250 * offset) {
                putSlowmode(conn, channel, 10, min, max);
            } else if (averageDelay <= 500 * offset) {
                putSlowmode(conn, channel, 6, min, max);
            } else if (averageDelay <= 750 * offset) {
                putSlowmode(conn, channel, 4, min, max);
            } else if (averageDelay <= 1000 * offset) {
                putSlowmode(conn, channel, 2, min, max);
            } else if (averageDelay <= 1250 * offset) {
                putSlowmode(conn, channel, 1, min, max);
            } else if (averageDelay <= 1500 * offset) {
                putSlowmode(conn, channel, 0, min, max);
            } else if (averageDelay <= 2000 * offset) {
                putSlowmode(conn, channel, -1, min, max);
            } else if (averageDelay <= 2500 * offset) {
                putSlowmode(conn, channel, -2, min, max);
            } else {
                putSlowmode(conn, channel, -4, min, max);
            }
            return null;
        });
    }

    /**
     * Adjusts the slowmode for the given channel.
     * @param channel TextChannel that will have the slowmode adjusted.
     * @param time    Int representing the adjustment time.
     */
    private static void putSlowmode(@Nullable final Connection conn, final TextChannel channel, final int time,
                                    final int min, final int max) throws SQLException
    {
        int slowmodeToSet;

        int slow = channel.getSlowmode(); // gets the current slowmode in the channel

        if (slow + time > max && max > 0) { // if slowmode and the added time exceed the max slowmode
            slowmodeToSet = max; // Set slowmode to the maximum value taken from the database.
        } else if (slow + time > TextChannel.MAX_SLOWMODE) {
            slowmodeToSet = TextChannel.MAX_SLOWMODE; // sets to max DISCORD slowmode value
        } else {
            slowmodeToSet = Math.max(slow + time, min);
        }

        channel.getManager().setSlowmode(slowmodeToSet).queue();

        // Adds +1 when a channel gets monitored.
        if (slow == min && slowmodeToSet > min && conn != null) {
            PreparedActions.incrementMonitorChart(conn, channel.getGuild().getId(), channel.getId());
        }
    }

    @Override
    public CommandType getType() {
        return CommandType.SYNAPSE_MONITOR;
    }

    @Override
    public Logger getLogger() {
        return lgr;
    }

    @Override
    public CommandData getData() {
        return data;
    }
}
