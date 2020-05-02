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
     * @param set     Whether this value will be set, or be added/deducted
     *                from the existing slowmode value.
     */
    public static void putSlowmode(TextChannel channel, int time, boolean set) {
        try {
            // gets the current slowmode
            int slow = channel.getSlowmode();

            // if the slowmode is being set or not
            if (!set) {
                // if slowmode and the added time exceed the max slowmode
                if (slow + time > TextChannel.MAX_SLOWMODE) {
                    // sets to max slowmode value and exits
                    channel.getManager().setSlowmode(TextChannel.MAX_SLOWMODE).queue();
                    return;
                } else if (slow + time < 0) {
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
            if (slow != 0) {
                channel.getManager().setSlowmode(time).queue();
            }
        } catch (InsufficientPermissionException ex) {
            Messages.sendMessage(channel, Embeds.insufficientPerm());
            Delete.Channel(channel.getGuild().getId(), channel.getId());
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
        // accounting for each delay of the messages
        // this function picks an appropriate slowmode
        // adjustment number for each case.
        if ((averageDelay <= 1000) && (firstMessageTime > 0 && firstMessageTime <= 10000)) {
            putSlowmode(channel, 5, false);
            System.out.println("5");
        } else if ((averageDelay <= 1500) && (firstMessageTime > 0 && firstMessageTime <= 10000)) {
            putSlowmode(channel, 4, false);
            System.out.println("4");
        } else if ((averageDelay <= 2000) && (firstMessageTime > 0 && firstMessageTime <= 10000)) {
            putSlowmode(channel, 3, false);
            System.out.println("3");
        } else if ((averageDelay <= 3000) && (firstMessageTime > 0 && firstMessageTime <= 10000)) {
            putSlowmode(channel, 2, false);
            System.out.println("2");
        } else if ((averageDelay <= 4000) && (firstMessageTime > 0 && firstMessageTime <= 15000)) {
            putSlowmode(channel, 1, false);
            System.out.println("1");
        } else if ((averageDelay <= 5000) || (firstMessageTime > 15000 && firstMessageTime <= 30000)) {
            putSlowmode(channel, -1, false);
            System.out.println("-1");
        } else if ((averageDelay <= 6000) || (firstMessageTime > 30000 && firstMessageTime <= 60000)) {
            putSlowmode(channel, -2, false);
            System.out.println("-2");
        } else {
            putSlowmode(channel, 0, true);
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

        ResultSet rs = conn.query("SELECT CHANNEL_ID FROM CHANNELS WHERE GUILD_ID = " + guild.getId());
        // list that will hold all guilds from the database
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
                if (!(unit.between(firstMessageTime, nowTime) > 60000))
                    CHANNELS.add(rs.getString(1));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("x");
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

            // if last message is at least 30 seconds old and nothing else has happened
            // gets delay between each message and adds it to an array
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
