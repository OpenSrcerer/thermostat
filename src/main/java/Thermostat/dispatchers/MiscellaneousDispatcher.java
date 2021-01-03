package thermostat.dispatchers;

import org.discordbots.api.client.DiscordBotListAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.Thermostat;

import java.util.concurrent.TimeUnit;

import static thermostat.Thermostat.thermo;

public final class MiscellaneousDispatcher {
    private static final Logger lgr = LoggerFactory.getLogger(MiscellaneousDispatcher.class);
    private static DiscordBotListAPI dblApi;

    public static void setDblApi(String token) {
        dblApi = new DiscordBotListAPI.Builder().token(token)
                .botId(thermo.getSelfUser().getId())
                .build();

        Runnable run = () -> {
            int currentServers = thermo.getGuilds().size();
            dblApi.setStats(currentServers);
            lgr.info("Current Guilds: [" + currentServers + "]");
        };

        Thermostat.executor.scheduleAtFixedRate(run, 10, 120, TimeUnit.SECONDS);
    }
}
