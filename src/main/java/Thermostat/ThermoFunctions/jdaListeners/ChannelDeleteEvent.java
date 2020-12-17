package thermostat.thermoFunctions.jdaListeners;

import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import thermostat.mySQL.DataSource;
import thermostat.mySQL.Delete;
import thermostat.thermoFunctions.monitorThreads.Worker;
import thermostat.thermoFunctions.monitorThreads.WorkerManager;

import javax.annotation.Nonnull;

public class ChannelDeleteEvent extends ListenerAdapter {
    @Override
    public void onTextChannelDelete(@Nonnull TextChannelDeleteEvent ev) {
        if (DataSource.queryString("SELECT CHANNEL_ID FROM CHANNELS WHERE GUILD_ID = ?", ev.getGuild().getId()) != null) {
            Worker guildWorker = WorkerManager.getActiveWorkerById(ev.getGuild().getId());

            if (guildWorker != null) {
                guildWorker.removeChannel(ev.getChannel());
            }

            Delete.Channel(ev.getGuild().getId(), ev.getChannel().getId());
        }
    }
}
