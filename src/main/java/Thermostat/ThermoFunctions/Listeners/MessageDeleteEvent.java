package thermostat.thermoFunctions.listeners;

import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

import static thermostat.thermoFunctions.entities.MonitoredMessage.monitoredMessages;

public class MessageDeleteEvent extends ListenerAdapter
{
    public void onGuildMessageDelete (@Nonnull GuildMessageDeleteEvent event) {
        try {
            monitoredMessages.removeIf(it -> event.getMessageId().equals(it.getMessageId()));
        } catch (Exception ignored) {}
    }
}
