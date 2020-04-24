package Thermostat.ThermoFunctions.MonitorThreads;

import Thermostat.MySQL.Connection;
import Thermostat.MySQL.Delete;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * <h1>Guild Worker</h1>
 * <p>
 * Manager class for each thread of a Guild.
 * Runs its' own scheduling system, independently
 * of the thread management one.
 */
public class GuildWorker
{
    private ScheduledExecutorService exec;
    private ScheduledFuture scheduledFuture;
    private String assignedGuild;

    /**
     * Creates an instance of the GuildWorker, representing
     * a thread that manages a Guild's channels.
     * @param thermo The JDA instance of the thermostat bot.
     *               Used to get a Guild variable to perform
     *               operations with its' channels upon.
     */
    public GuildWorker(JDA thermo)
    {
        Runnable mon = () -> monitorChannels(thermo.getGuildById(assignedGuild));
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        executor.setRemoveOnCancelPolicy(true);
        exec = executor;
        // scheduledFuture = ChannelListener.SES.scheduleAtFixedRate(mon, 0, 10, TimeUnit.SECONDS);
        // to be tested for implementation
        scheduledFuture = exec.scheduleAtFixedRate(mon, 0, 10, TimeUnit.SECONDS);
    }

    /**
     * Setter for the Guild that this class will
     * manage.
     * @param AG A string containing the Guild ID.
     */
    public void setAssignedGuild(String AG)
    {
        assignedGuild = AG;
    }

    /**
     * Getter for the ID that this class is managing.
     * @return A string containing the Guild ID.
     */
    public String getAssignedGuild()
    {
        return assignedGuild;
    }

    /**
     * Invalidation method for the scheduledFuture
     * instance of the runnable. Usually called upon
     * when the Guild is no longer in the database.
     */
    public void invalidate()
    {
        scheduledFuture.cancel(true);
    }

    /**
     * Shuts down the local executor of this class.
     * Will need to deprecate after passing that job
     * to the executor in {@link ChannelListener}
     */
    public void shutdown() {
        exec.shutdown();
    }

    /**
     * Calculates an average of the delay time between
     * each message.
     * @param delays A list containing the delays between
     *              messages.
     * @return A long value, with the average time.
     */
    public static long calculateAverageTime(List<Long> delays)
    {
        Long sum = 0L;
        if(!delays.isEmpty()) {
            for (Long mark : delays) {
                sum += mark;
            }
            return sum.longValue() / delays.size();
        }
        return sum;
    }

    /**
     * A function that adjusts the slowmode for the given channel.
     * @param channel TextChannel that will have the slowmode adjusted.
     * @param time Int representing the adjustment time.
     * @param set Whether this value will be set, or be added/deducted
     *            from the existing slowmode value.
     */
    public static void putSlowmode(TextChannel channel, int time, boolean set)
    {
        // gets the current slowmode
        int slow = channel.getSlowmode();

        // if the slowmode is being set or not
        if (!set)
        {
            // if slowmode and the added time exceed the max slowmode
            if (slow + time > TextChannel.MAX_SLOWMODE)
            {
                // sets to max slowmode value and exits
                channel.getManager().setSlowmode(TextChannel.MAX_SLOWMODE).queue();
                return;
            }
            else if (slow + time < 0)
            {
                // if it's less than zero, due to time being negative
                // sets it to zero
                channel.getManager().setSlowmode(0).queue();
                return;
            }
            // otherwise just sets it
            channel.getManager().setSlowmode(slow + time).queue();
            return;
        }

        // if slowmode isn't zero, update it
        if (slow != 0)
        {
            channel.getManager().setSlowmode(time).queue();
        }
    }

    /**
     * Calculates the slowmode for a certain channel. See function above.
     * @param channel The channel that will have the slowmode adjusted.
     * @param averageDelay The average delay between the past 25 messages.
     * @param firstMessageTime How long it has passed since the last message was sent.
     */
    public static void slowmodeSwitch(TextChannel channel, Long averageDelay, Long firstMessageTime)
    {
        // accounting for each delay of the messages
        // this function picks an appropriate slowmode
        // adjustment number for each case.
        if (averageDelay <= 500 && firstMessageTime < 5000) {
            putSlowmode(channel, 10, false);
        }
        else if (averageDelay <= 1000 && firstMessageTime < 5000) {
            putSlowmode(channel, 4, false);
        }
        else if (averageDelay <= 1500 && firstMessageTime < 5000) {
            putSlowmode(channel, 2, false);
        }
        else if (averageDelay <= 2000 && firstMessageTime < 5000) {
            putSlowmode(channel, -1, false);
        }
        else if ((averageDelay <= 3000 && averageDelay > 2000) && firstMessageTime < 5000) {
            putSlowmode(channel, -2, false);
        }
        else if ((averageDelay <= 4000 && averageDelay > 3000) || (firstMessageTime > 5000 && firstMessageTime <= 60000)) {
            putSlowmode(channel, -8, false);
        }
        else if (averageDelay > 6000 || firstMessageTime > 60000) {
            putSlowmode(channel, 0, true);
        }
    }

    /**
     * Function that gets called periodically to
     * adjust the slowmode of each channel in the given guild.
     * @param guild The guild that will have the channels monitored.
     */
    public static void monitorChannels(Guild guild)
    {
        // get ID of guild channels to monitor
        Connection conn = new Connection();

        ResultSet rs = conn.query("SELECT CHANNEL_ID FROM CHANNELS WHERE GUILD_ID = " + guild.getId());
        // list that will hold all guilds from the database
        ArrayList<String> CHANNELS = new ArrayList<>();
        try {
            while (rs.next())
            {
                CHANNELS.add(rs.getString(1));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        conn.closeConnection();

        for (String it : CHANNELS)
        {
            // delete channel from db if channel was removed
            if (guild.getTextChannelById(it) == null)
            {
                Delete.Channel(guild.getId(), it);
                break;
            }

            // monitor function, gets lastest message as reference point
            TextChannel Channel = guild.getTextChannelById(it);

            List<Message> retrieved = Channel.getHistory().retrievePast(25).complete();
            List<Long> delays = new ArrayList<>();

            ChronoUnit unit = ChronoUnit.MILLIS;

            OffsetDateTime nowTime = OffsetDateTime.now().toInstant().atOffset(ZoneOffset.UTC).truncatedTo(unit);
            OffsetDateTime firstMessageTime = retrieved.get(0).getTimeCreated();

            // if last message is at least 30 seconds old and nothing else has happened

            // gets delay between each message and adds it to an array
            for (int index = 0; index < retrieved.size() - 1; ++index)
            {
                delays.add(
                        unit.between(
                                retrieved.get(index + 1).getTimeCreated(),
                                retrieved.get(index).getTimeCreated()
                        )
                );
            }
            slowmodeSwitch(Channel, calculateAverageTime(delays), unit.between(firstMessageTime, nowTime));
        }
    }
}
