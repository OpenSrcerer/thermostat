package thermostat.thermoFunctions.monitorThreads;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class MessageReceived extends ListenerAdapter {

    /**
     * Listener used to update a list of messages in a
     * WorkerChannel object, whenever a message is
     * received.
     *
     * @param ev Event when a message is received.
     */
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent ev) {
        // adds and pops messages from list
        ArrayList<Worker> workers = WorkerManager.getActiveWorkers();

        workers.forEach(worker -> worker.channelsToMonitor.forEach(channel -> {
            if (ev.getChannel().getId().equals(channel.channelId)) {

                if (channel.messageList.size() < 10) {
                    channel.messageList.add(0, ev.getMessage().getTimeCreated());
                } else {
                    channel.messageList.add(0, ev.getMessage().getTimeCreated());
                    channel.messageList.remove(10);
                }
            }
        }));

        WorkerManager.setActiveWorkers(workers);
    }
}
