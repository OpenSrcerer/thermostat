package Thermostat.ThermoFunctions.MonitorThreads;

import Thermostat.Embeds;
import Thermostat.MySQL.Connection;
import Thermostat.MySQL.Delete;
import Thermostat.ThermoFunctions.Messages;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.w3c.dom.Text;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static Thermostat.thermostat.thermo;

/**
 * <h1>Guild Worker</h1>
 * <p>
 * Manager class for each thread of a Guild.
 * Runs its' own scheduling system, independently
 * of the thread management one.
 */
public class GuildWorker {
    private ScheduledFuture scheduledFuture;
    private String assignedGuild;

    /**
     * Creates an instance of the GuildWorker, representing
     * a thread that manages a Guild's channels.
     */
    public GuildWorker() {
        Runnable mon = () -> monitorChannels(thermo.getGuildById(assignedGuild));
        scheduledFuture = ChannelListener.SES.scheduleAtFixedRate(mon, 0, 10, TimeUnit.SECONDS);
    }

    /**
     * Function for scheduling the worker. Checks if it
     * may have somehow failed, and restarts it if so.
     */
    public void scheduleWorker(JDA thermo) {
        if (scheduledFuture.isDone() || scheduledFuture.isCancelled()) {
            Runnable mon = () -> monitorChannels(thermo.getGuildById(assignedGuild));
            scheduledFuture = ChannelListener.SES.scheduleAtFixedRate(mon, 0, 10, TimeUnit.SECONDS);
        }
    }

    /**
     * Setter for the Guild that this class will
     * manage.
     *
     * @param AG A string containing the Guild ID.
     */
    public void setAssignedGuild(String AG) {
        assignedGuild = AG;
    }

    /**
     * Getter for the ID that this class is managing.
     *
     * @return A string containing the Guild ID.
     */
    public String getAssignedGuild() {
        return assignedGuild;
    }

    /**
     * Invalidation method for the scheduledFuture
     * instance of the runnable. Usually called upon
     * when the Guild is no longer in the database.
     */
    public void invalidate() {
        scheduledFuture.cancel(true);
    }

    /**
     * Calculates an average of the delay time between
     * each message.
     *
     * @param delays A list containing the delays between
     *               messages.
     * @return A long value, with the average time.
     */
    public static long calculateAverageTime(List<Long> delays) {
        Long sum = 0L;
        if (!delays.isEmpty()) {
            for (Long mark : delays) {
                sum += mark;
            }
            return sum.longValue() / delays.size();
        }
        return sum;
    }

    /**
     * A function that adjusts the slowmode for the given channel.
     *
     * @param channel TextChannel that will have the slowmode adjusted.
     * @param time    Int representing the adjustment time.
     */
    public static void putSlowmode(TextChannel channel, int time, int max, int min) {
        try {
            // gets the current slowmode
            int slow = channel.getSlowmode();

            // if slowmode and the added time exceed the max slowmode
            if (slow + time > max && max > 0) {
                // sets to max DATABASE slowmode value and exits
                channel.getManager().setSlowmode(max).queue();
                return;
            }
            if (slow + time > TextChannel.MAX_SLOWMODE) {
                // sets to max DISCORD slowmode value and exits
                channel.getManager().setSlowmode(TextChannel.MAX_SLOWMODE).queue();
                return;
            }
            if (slow + time < min)
            {
                // if it's less than minimum DB value
                // sets it to that minimum value
                channel.getManager().setSlowmode(min).queue();
                return;
            }
            // otherwise just sets it
            channel.getManager().setSlowmode(slow + time).queue();

        } catch (InsufficientPermissionException ex) {
            Messages.sendMessage(channel, Embeds.insufficientPerm());
            Delete.Channel(channel.getGuild().getId(), channel.getId());
        } catch (Exception ex)
        {
            ex.printStackTrace();
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
        // variables that store the maximum
        // and minimum  slow values for each channel
        int max = 0, min = 0;

        // gets the maximum and minimum slowmode values
        // from the database.
        Connection conn = null;
        try {
            conn = new Connection();

            ResultSet minrs = conn.query("SELECT MIN_SLOW FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = " + channel.getId()),
                    maxrs = conn.query("SELECT MAX_SLOW FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = " + channel.getId());

            maxrs.next();
            minrs.next();

            min = minrs.getInt(1);
            max = maxrs.getInt(1);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // accounting for each delay of the messages
        // this function picks an appropriate slowmode
        // adjustment number for each case.
        if ((averageDelay <= 1000) && (firstMessageTime > 0 && firstMessageTime <= 10000)) {
            putSlowmode(channel, 5, max, min);
            System.out.println("5");
        } else if ((averageDelay <= 1500) && (firstMessageTime > 0 && firstMessageTime <= 10000)) {
            putSlowmode(channel, 4, max, min);
            System.out.println("4");
        } else if ((averageDelay <= 2000) && (firstMessageTime > 0 && firstMessageTime <= 10000)) {
            putSlowmode(channel, 3, max, min);
            System.out.println("3");
        } else if ((averageDelay <= 3000) && (firstMessageTime > 0 && firstMessageTime <= 10000)) {
            putSlowmode(channel, 2, max, min);
            System.out.println("2");
        } else if ((averageDelay <= 4000) && (firstMessageTime > 0 && firstMessageTime <= 15000)) {
            putSlowmode(channel, 1, max, min);
            System.out.println("1");
        } else if ((averageDelay <= 5000) || (firstMessageTime > 15000 && firstMessageTime <= 40000)) {
            putSlowmode(channel, -1, max, min);
            System.out.println("-1");
        } else if ((averageDelay <= 6000) || (firstMessageTime > 40000 && firstMessageTime <= 60000)) {
            putSlowmode(channel, -2, max, min);
            System.out.println("-2");
        } else {
            putSlowmode(channel, -999999999, max, min);
            System.out.println("none");
        }

        if (channel.getGuild().getId().equals("645188230756696085")) {
            System.out.println(channel.getGuild().getName() + " - AVG: " + averageDelay + "/firstMessageTime: " + firstMessageTime);
        }

        if (channel.getGuild().getId().equals("289746418816516098")) {
            System.out.println(channel.getGuild().getName() + " - AVG: " + averageDelay + "/firstMessageTime: " + firstMessageTime);
        }
    }

    /**
     * Function that gets called periodically to
     * adjust the slowmode of each channel in the given guild.
     *
     * @param guild The guild that will have the channels monitored.
     */
    public static void monitorChannels(Guild guild) {
        // get ID of guild channels to monitor
        Connection conn;
        try {
            conn = new Connection();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return;
        }

        ResultSet rs = conn.query("SELECT CHANNELS.CHANNEL_ID FROM CHANNELS " +
                "JOIN CHANNEL_SETTINGS ON (CHANNELS.CHANNEL_ID = CHANNEL_SETTINGS.CHANNEL_ID) " +
                "WHERE CHANNELS.GUILD_ID = " + guild.getId() + " AND CHANNEL_SETTINGS.MONITORED = 1");
        // list that will hold all currently monitored channels
        ArrayList<String> CHANNELS = new ArrayList<>();

        try {
            while (rs.next()) {
                ChronoUnit unit = ChronoUnit.MILLIS;
                OffsetDateTime nowTime = OffsetDateTime.now().toInstant().atOffset(ZoneOffset.UTC).truncatedTo(unit);
                OffsetDateTime firstMessageTime;

                try {
                    firstMessageTime = guild.getTextChannelById(rs.getString(1)).getHistory().retrievePast(1).complete().get(0).getTimeCreated();
                } catch (InsufficientPermissionException | NullPointerException ex) {
                    TextChannel tc = guild.getTextChannelById(rs.getString(1));

                    if (ex.toString().contains("MESSAGE_HISTORY")) {
                        Messages.sendMessage(tc, Embeds.insufficientPerm("Read Message History"));
                        Delete.Channel(guild.getId(), tc.getId());
                    } else if (ex.toString().contains("MESSAGE_READ")) {
                        Messages.sendMessage(tc, Embeds.insufficientPerm("Read Messages"));
                        Delete.Channel(guild.getId(), tc.getId());
                    }
                    return;
                }

                // if the time between the last message and now is more than
                // 60 seconds, don't monitor the channel to save resources
                // but update its' slowmode to the minimum
                ResultSet rsx = conn.query("SELECT MIN_SLOW FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = " + rs.getString(1));
                rsx.next();
                int min = rsx.getInt(1);
                if (!(unit.between(firstMessageTime, nowTime) > 60000)) {
                    CHANNELS.add(rs.getString(1));
                } else {
                    putSlowmode(guild.getTextChannelById(rs.getString(1)), -99999999, TextChannel.MAX_SLOWMODE, min);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        conn.closeConnection();

        for (String it : CHANNELS) {
            // delete channel from db if channel was removed
            if (guild.getTextChannelById(it) == null) {
                Delete.Channel(guild.getId(), it);
                break;
            }

            // monitor function, gets latest message as reference point
            TextChannel Channel = guild.getTextChannelById(it);

            ChronoUnit unit = ChronoUnit.MILLIS;
            OffsetDateTime nowTime = OffsetDateTime.now().toInstant().atOffset(ZoneOffset.UTC).truncatedTo(unit);

            List<Message> retrieved = Channel.getHistory().retrievePast(10).complete();
            List<Long> delays = new ArrayList<>();

            OffsetDateTime firstMessageTime = retrieved.get(0).getTimeCreated();

            // gets delay between each message
            for (int index = 0; index < retrieved.size() - 1; ++index) {
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
