package thermostat.managers;

import net.dv8tion.jda.api.entities.ISnowflake;
import thermostat.mySQL.DataSource;
import thermostat.mySQL.Delete;
import thermostat.thermoFunctions.commands.monitoring.synapses.Synapse;
import thermostat.thermostat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class SynapseManager {
    private static final ArrayList<Synapse> synapses = new ArrayList<>();

    private static ArrayList<Synapse> initializeSynapses() {
        ArrayList<Synapse> synapses = new ArrayList<>();
        List<String> databaseGuilds = DataSource.queryStringArray("SELECT GUILD_ID FROM GUILDS", "");
        List<String> thermostatGuilds = thermostat.thermo.getGuilds().stream().map(ISnowflake::getId).collect(Collectors.toList());

        if (databaseGuilds == null) {
            return new ArrayList<>();
        }

        for (String guild : databaseGuilds) {
            if (thermostatGuilds.contains(guild)) {
                synapses.add(new Synapse(guild));
            } else {
                Delete.Guild(guild);
            }
        }

        return synapses;
    }
}