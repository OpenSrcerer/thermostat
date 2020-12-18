package thermostat.thermoFunctions.commands.monitoring.synapses;

import thermostat.thermoFunctions.entities.SynapseState;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.stream.Stream;

public class Synapse {
    private SynapseState state = SynapseState.ACTIVE;
    private final String guildId;
    private final ArrayList<String> monitoredChannels;

    public Synapse(@Nonnull String guildId) {
        this.guildId = guildId;
        this.monitoredChannels = SynapseFunctions.initializeMonitoredChannels(guildId);
    }

    public synchronized void setState(SynapseState state) {
        this.state = state;
    }

    public Stream<String> getChannels() {
        return monitoredChannels.stream();
    }

    public String getGuildId() {
        return guildId;
    }

    public SynapseState getState() {
        return state;
    }
}
