package thermostat.thermoFunctions.commands.monitoring.synapses;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.thermoFunctions.Functions;
import thermostat.thermoFunctions.commands.Command;
import thermostat.thermoFunctions.entities.CommandType;
import thermostat.thermostat;

import javax.annotation.Nullable;

public class SynapseMonitor implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(SynapseMonitor.class);

    /**
     * Synapse that started this monitoring event.
     */
    private final Synapse synapse;

    /**
     * ID of this Command.
     */
    private final long commandId;

    /**
     * Create a new Monitor event for each Synapse.
     * @param synapse Synapse to take as an argument.
     */
    public SynapseMonitor(Synapse synapse) {
        this.synapse = synapse;
        this.commandId = Functions.getCommandId();

        checkPermissionsAndQueue(this);
    }

    @Override
    public void run() {
        Guild synapseGuild = thermostat.thermo.getGuildById(synapse.getGuildId());

        if (synapseGuild == null) {
            // do thing with null guild
            return;
        }

        for (String channel : synapse.getChannels()) {
            synapseGuild.getTextChannelById(channel)
        }

        // complete
    }

    @Override
    public CommandType getType() {
        return CommandType.SYNAPSE_MONITOR;
    }

    @Override
    public Logger getLogger() {
        return lgr;
    }

    @Override
    public long getId() {
        return commandId;
    }

    @Nullable
    @Override
    public GuildMessageReceivedEvent getEvent() {
        return null;
    }
}
