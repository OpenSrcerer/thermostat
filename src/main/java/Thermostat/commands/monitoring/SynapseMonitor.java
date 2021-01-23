package thermostat.commands.monitoring;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.dispatchers.CommandDispatcher;
import thermostat.util.MiscellaneousFunctions;
import thermostat.commands.Command;
import thermostat.util.entities.Synapse;
import thermostat.util.enumeration.CommandType;

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
        this.commandId = MiscellaneousFunctions.getCommandId();

        CommandDispatcher.queueCommand(this);
    }

    @Override
    public void run() {
        synapse.monitorChannels(lgr);
        lgr.info(this.toString());
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
