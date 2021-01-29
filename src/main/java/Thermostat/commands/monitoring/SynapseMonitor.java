package thermostat.commands.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.commands.Command;
import thermostat.dispatchers.CommandDispatcher;
import thermostat.util.entities.CommandData;
import thermostat.util.entities.Synapse;
import thermostat.util.enumeration.CommandType;

import java.sql.SQLException;

public class SynapseMonitor implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(SynapseMonitor.class);

    /**
     * Synapse that started this monitoring event.
     */
    private final Synapse synapse;

    /**
     * Data package for this command.
     */
    private final CommandData data;

    /**
     * Create a new Monitor event for each Synapse.
     * @param synapse Synapse to take as an argument.
     */
    public SynapseMonitor(Synapse synapse) {
        this.data = new CommandData(null);
        this.synapse = synapse;

        CommandDispatcher.queueCommand(this);
    }

    @Override
    public void run() {
        try {
            synapse.monitorChannels(lgr);
        } catch (SQLException ex) {
            lgr.info("Failure in monitoring Guild " + synapse.getGuildId() + ".", ex);
            return;
        }

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
    public CommandData getData() {
        return data;
    }
}
