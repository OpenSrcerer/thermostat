package thermostat.thermoFunctions.monitorThreads;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.mySQL.Create;
import thermostat.mySQL.DataSource;
import thermostat.preparedStatements.ErrorEmbeds;
import thermostat.thermoFunctions.Messages;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static thermostat.thermostat.thermo;

/**
 * Manager class for each instance of a Guild.
 */
public class Worker {

    private static final Logger lgr = LoggerFactory.getLogger(Worker.class);

    private ScheduledFuture<?> scheduledFuture;
    private String assignedGuild;
    protected final List<WorkerChannel> channelsToMonitor = new ArrayList<>();


    /**
     * Creates an instance of the ChannelWorker, representing
     * a thread that manages a Guild's channels.
     */
    public Worker() {
        Runnable mon = () -> monitorChannels(thermo.getGuildById(assignedGuild));
        scheduledFuture = WorkerManager.scheduledExecutorService.scheduleAtFixedRate(mon, 0, 8, TimeUnit.SECONDS);
    }

    /**
     * Function for scheduling the worker. Checks if it
     * may have somehow failed, and restarts it if so.
     */
    public void scheduleWorker() {
        if (scheduledFuture.isDone() || scheduledFuture.isCancelled()) {
            Runnable mon = () -> monitorChannels(thermo.getGuildById(assignedGuild));
            scheduledFuture = WorkerManager.scheduledExecutorService.scheduleAtFixedRate(mon, 0, 8, TimeUnit.SECONDS);
        }
    }

    /**
     * Setter for the Guild that this class will
     * manage.
     *
     * @param assignedGuild A string containing the Guild ID.
     */
    public void setAssignedGuild(String assignedGuild) {
        this.assignedGuild = assignedGuild;
    }

    /**
     * Getter for the ID that this class is managing.
     *
     * @return A string containing the Guild ID.
     */
    public String getAssignedGuild() {
        return this.assignedGuild;
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
    public long calculateAverageTime(List<Long> delays) {
        Long sum = 0L;
        if (!delays.isEmpty()) {
            for (Long it : delays) {
                sum += it;
            }
            return sum / delays.size();
        }
        return sum;
    }

    /**
     * A function that adjusts the slowmode for the given channel.
     *
     * @param channel TextChannel that will have the slowmode adjusted.
     * @param time    Int representing the adjustment time.
     */
    public void putSlowmode(TextChannel channel, int time, int max, int min) {
        Consumer<Throwable> slowmodeFailureConsumer = throwable -> {
            if (!(throwable instanceof IOException)) {
                lgr.error("I/O Exception!", throwable);
                removeChannel(channel);
            }
        };

        int slowmodeToSet;

        try {
            // gets the current slowmode
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

            channel.getManager().setSlowmode(slowmodeToSet).queue(null, slowmodeFailureConsumer);

            // Adds +1 to slowmode turning on for charting purposes.
            if (slow == min && slowmodeToSet > min) {
                DataSource.update("UPDATE CHANNELS SET MANIPULATED = MANIPULATED + 1 WHERE CHANNEL_ID = ? AND GUILD_ID = ?",
                        Arrays.asList(channel.getId(), channel.getGuild().getId()));
            }

        } catch (InsufficientPermissionException ex) {
            removeChannel(channel);
        } catch (Exception ex) {
            lgr.error(ex.getMessage(), ex);
        }
    }

    /**
     * Removes channels from monitoring.
     *
     * @param channel The channel to be removed.
     */
    public void removeChannel(TextChannel channel) {

        // add failed to monitor msg here

        Messages.sendMessage(channel, ErrorEmbeds.errFatal("I am missing the necessary permissions to monitor this channel."));

        for (int index = 0; index < channelsToMonitor.size(); ++index) {
            if (channelsToMonitor.get(index).getChannelId().equals(channel.getId())) {
                channelsToMonitor.remove(channelsToMonitor.get(index));
            }
        }

        Create.ChannelMonitor(channel.getGuild().getId(), channel.getId(), 0);
    }

    /**
     * Calculates the slowmode for a certain channel. See function above.
     *
     * @param channel          The channel that will have the slowmode adjusted.
     * @param averageDelay     The average delay between the past 25 messages.
     * @param firstMessageTime How long it has passed since the last message was sent.
     */
    public void slowmodeSwitch(TextChannel channel, Long averageDelay, Long firstMessageTime) {
        if (channel == null) {
            return;
        }
        // variables that store the maximum
        // and minimum  slow values for each channel
        int max, min;
        float offset;

        // gets the maximum and minimum slowmode values
        // from the database.
        max = DataSource.queryInt("SELECT MAX_SLOW FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = ?", channel.getId());
        min = DataSource.queryInt("SELECT MIN_SLOW FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = ?", channel.getId());
        offset = DataSource.querySens("SELECT SENSOFFSET FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = ?", channel.getId());

        // accounting for each delay of the messages
        // this function picks an appropriate slowmode
        // adjustment number for each case.
        if ((averageDelay <= 100 * offset) && (firstMessageTime > 0 && firstMessageTime <= 1000)) {
            putSlowmode(channel, 20, max, min);
        } else if ((averageDelay <= 250 * offset) && (firstMessageTime > 0 && firstMessageTime <= 2500)) {
            putSlowmode(channel, 10, max, min);
        } else if ((averageDelay <= 500 * offset) && (firstMessageTime > 0 && firstMessageTime <= 5000)) {
            putSlowmode(channel, 6, max, min);
        } else if ((averageDelay <= 750 * offset) && (firstMessageTime > 0 && firstMessageTime <= 8000)) {
            putSlowmode(channel, 4, max, min);
        } else if ((averageDelay <= 1000 * offset) && (firstMessageTime > 0 && firstMessageTime <= 10000)) {
            putSlowmode(channel, 2, max, min);
        } else if ((averageDelay <= 1250 * offset) && (firstMessageTime > 0 && firstMessageTime <= 10000)) {
            putSlowmode(channel, 1, max, min);
        } else if ((averageDelay <= 1500 * offset) && (firstMessageTime > 0 && firstMessageTime <= 10000)) {
            putSlowmode(channel, 0, max, min);
        } else if ((firstMessageTime > 0 && firstMessageTime <= 10000) || (averageDelay < 2000 && averageDelay >= 1500)) {
            putSlowmode(channel, -1, max, min);
        } else if ((firstMessageTime > 10000 && firstMessageTime <= 30000) || (averageDelay < 2500 && averageDelay >= 2000)) {
            putSlowmode(channel, -2, max, min);
        } else if ((firstMessageTime > 30000 && firstMessageTime <= 60000) || averageDelay >= 2500) {
            putSlowmode(channel, -4, max, min);
        }
    }

    /**
     * Gets the delays between each message and puts
     * them into a list.
     *
     * @param offsetDateTimes List of time when messages
     *                        were sent.
     * @param chronoUnit      Unit, (in ms) of the applied calculations.
     * @return List of delays between messages.
     */
    public List<Long> getListOfDelays(List<OffsetDateTime> offsetDateTimes, ChronoUnit chronoUnit) {
        List<Long> longList = new ArrayList<>();

        if (offsetDateTimes.size() < 5) {
            longList.add(5000L);
            return longList;
        }

        for (int jndex = 0; jndex < offsetDateTimes.size() - 1; ++jndex) {
            longList.add(
                    chronoUnit.between(
                            offsetDateTimes.get(jndex + 1),
                            offsetDateTimes.get(jndex)
                    )
            );
        }

        return longList;
    }

    /**
     * Function that gets called periodically to
     * find valid channels on each guild
     * to have their slowmode changed.
     *
     * @param guild The guild that will have the channels monitored.
     */
    public void monitorChannels(Guild guild) {
        if (guild == null) {
            return;
        }

        // get raw list of channels to monitor from db
        ArrayList<String> databaseMonitoredChannels = DataSource.queryStringArray("SELECT CHANNELS.CHANNEL_ID FROM CHANNELS " +
                "JOIN CHANNEL_SETTINGS ON (CHANNELS.CHANNEL_ID = CHANNEL_SETTINGS.CHANNEL_ID) " +
                "WHERE CHANNELS.GUILD_ID = ? AND CHANNEL_SETTINGS.MONITORED = 1", guild.getId());

        if (databaseMonitoredChannels == null) { return; }

        // keeps monitored channels list up to date
        // by adding newly monitored channels
        for (String it : databaseMonitoredChannels) {
            boolean channelMatch = false;
            for (WorkerChannel jt : channelsToMonitor) {
                if (it.equals(jt.getChannelId())) {
                    channelMatch = true;
                    break;
                }
            }
            if (!channelMatch) {
                channelsToMonitor.add(new WorkerChannel(it));
            }
        }

        try {
            for (int index = 0; index < channelsToMonitor.size(); ++index) {
                // grabs current worker in the loop in an
                // iterator-free way due to concurrency issues
                WorkerChannel currentWorker = channelsToMonitor.get(index);

                if (!currentWorker.messageList.isEmpty()) {
                    ChronoUnit unit = ChronoUnit.MILLIS;
                    OffsetDateTime nowTime = OffsetDateTime.now().toInstant().atOffset(ZoneOffset.UTC).truncatedTo(unit);

                    int min = DataSource.queryInt("SELECT MIN_SLOW FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = ?", currentWorker.getChannelId());

                    if ((
                            unit.between(currentWorker.messageList.get(0), nowTime) > 64000) ||
                            DataSource.checkDatabaseForData("SELECT * FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = ?" +
                                    " AND MONITORED = 0", currentWorker.getChannelId())
                    ) {
                        // removes object, emptying list
                        channelsToMonitor.remove(currentWorker);
                        putSlowmode(guild.getTextChannelById(currentWorker.getChannelId()), -99999999, TextChannel.MAX_SLOWMODE, min);
                    } else {
                        OffsetDateTime firstMessageTime = currentWorker.messageList.get(0);
                        List<Long> delays = getListOfDelays(currentWorker.messageList, unit);

                        slowmodeSwitch(
                                guild.getTextChannelById(currentWorker.getChannelId()),
                                calculateAverageTime(delays),
                                unit.between(firstMessageTime, nowTime)
                        );
                    }
                }
            }
        } catch (Exception ex) {
            Logger lgr = LoggerFactory.getLogger(DataSource.class);
            lgr.error(ex.getMessage(), ex);
        }
    }
}
