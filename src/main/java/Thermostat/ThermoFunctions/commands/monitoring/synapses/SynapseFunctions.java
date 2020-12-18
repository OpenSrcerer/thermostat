package thermostat.thermoFunctions.commands.monitoring.synapses;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import thermostat.mySQL.DataSource;
import thermostat.mySQL.Delete;
import thermostat.thermostat;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class SynapseFunctions {

    @Nonnull
    protected static ArrayList<String> initializeMonitoredChannels(String guildId) {
        ArrayList<String> databaseMonitoredChannels = DataSource.queryStringArray("SELECT CHANNELS.CHANNEL_ID FROM CHANNELS " +
                "JOIN CHANNEL_SETTINGS ON (CHANNELS.CHANNEL_ID = CHANNEL_SETTINGS.CHANNEL_ID) " +
                "WHERE CHANNELS.GUILD_ID = ? AND CHANNEL_SETTINGS.MONITORED = 1", guildId);
        ArrayList<String> monitoredChannels = new ArrayList<>();

        Guild guild = thermostat.thermo.getGuildById(guildId);

        if (guild == null || databaseMonitoredChannels == null) {
            return new ArrayList<>();
        }

        // Get all channel ids from list of text channels
        List<String> channelsInGuild = guild.getTextChannels().stream().map(ISnowflake::getId).collect(Collectors.toList());

        for (String channel : databaseMonitoredChannels) {
            if (channelsInGuild.contains(channel)) {
                monitoredChannels.add(channel);
            } else {
                Delete.Channel(guildId, channel);
            }
        }

        return monitoredChannels;
    }
}
