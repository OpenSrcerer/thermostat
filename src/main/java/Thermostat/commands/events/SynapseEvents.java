package thermostat.commands.events;

import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.UnavailableGuildLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.mySQL.PreparedActions;
import thermostat.util.GuildCache;
import thermostat.util.entities.Synapse;
import thermostat.util.enumeration.SynapseState;

/**
 * Manages all JDA Events that relate to Synapses.
 */
public class SynapseEvents extends ListenerAdapter {
    /**
     * Logger for SynapseEvents.
     */
    private static final Logger lgr = LoggerFactory.getLogger(SynapseEvents.class);

    /**
     * Lazy-loader for Synapses that is tied to a GuildMessageReceivedEvent.
     *
     * @param event Event that contains the ID of the Guild that
     *              the Synapse will be initialized on.
     */
    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        Synapse synapse = GuildCache.getSynapse(event.getGuild().getId());

        // Re-activate Synapse if it was disabled due to inactivity.
        if (synapse.getState() == SynapseState.INACTIVE && synapse.getChannels().contains(event.getChannel().getId())) {
            synapse.setState(SynapseState.ACTIVE);
            lgr.info("Synapse reactivated! Guild: " + event.getGuild().getName());
        }

        synapse.addMessage(event.getChannel().getId(), event.getMessage().getTimeCreated());
    }

    /**
     * If a TextChannel is deleted from a Guild, it will be removed from the
     * Synapse's monitored channels cache.
     */
    @Override
    public void onTextChannelDelete(@NotNull TextChannelDeleteEvent event) {
        GuildCache.getSynapse(event.getGuild().getId()).removeChannel(event.getChannel().getId());
    }

    /**
     * Removes a Synapse if a Guild removes Thermostat.
     */
    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        GuildCache.expungeGuild(event.getGuild().getId());
        PreparedActions.deleteGuild(event.getGuild().getId());
    }

    /**
     * Removes a Synapse if a Guild removes Thermostat.
     */
    @Override
    public void onUnavailableGuildLeave(@NotNull UnavailableGuildLeaveEvent event) {
        GuildCache.expungeGuild(event.getGuildId());
    }
}
