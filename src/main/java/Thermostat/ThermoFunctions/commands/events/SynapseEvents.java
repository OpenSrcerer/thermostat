package thermostat.thermoFunctions.commands.events;

import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.UnavailableGuildJoinedEvent;
import net.dv8tion.jda.api.events.guild.UnavailableGuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import thermostat.managers.SynapseManager;

/**
 * Manages all JDA Events that relate to Synapses.
 */
public class SynapseEvents extends ListenerAdapter {

    /**
     * Adds a new Synapse if a new Guild adds Thermostat.
     */
    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        SynapseManager.addSynapse(event.getGuild().getId());
    }

    /**
     * Adds a new Synapse if a new Guild adds Thermostat.
     */
    @Override
    public void onUnavailableGuildJoined(@NotNull UnavailableGuildJoinedEvent event) {
        SynapseManager.addSynapse(event.getGuildId());
    }

    /**
     * If a TextChannel is deleted from a Guild, it will be removed from the
     * Synapse's monitored channels cache.
     */
    @Override
    public void onTextChannelDelete(@NotNull TextChannelDeleteEvent event) {
        SynapseManager.getSynapse(event.getGuild().getId()).removeChannel(event.getChannel().getId());
    }

    /**
     * Removes a Synapse if a Guild removes Thermostat.
     */
    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        SynapseManager.removeSynapse(event.getGuild().getId());
    }

    /**
     * Removes a Synapse if a Guild removes Thermostat.
     */
    @Override
    public void onUnavailableGuildLeave(@NotNull UnavailableGuildLeaveEvent event) {
        SynapseManager.removeSynapse(event.getGuildId());
    }
}
