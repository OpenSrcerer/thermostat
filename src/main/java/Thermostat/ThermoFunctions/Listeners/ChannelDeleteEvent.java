package thermostat.thermoFunctions.listeners;

import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import thermostat.mySQL.DataSource;
import thermostat.mySQL.Delete;

import javax.annotation.Nonnull;

public class ChannelDeleteEvent extends ListenerAdapter {
    @Override
    public void onTextChannelDelete(@Nonnull TextChannelDeleteEvent ev) {
        if (DataSource.queryString("SELECT CHANNEL_ID FROM CHANNELS WHERE GUILD_ID = " + ev.getGuild().getId()) != null) {
            Delete.Channel(ev.getGuild().getId(), ev.getChannel().getId());
        }
    }
}
