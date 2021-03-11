package thermostat.dispatchers;

import org.discordbots.api.client.DiscordBotListAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.Thermostat;
import xyz.discordboats.Boats4J.Boats4J;

import java.util.concurrent.TimeUnit;

import static thermostat.Thermostat.thermo;

/**
 * Dispatches periodic miscellaneous events like status changes, API
 * connections, etc.
 */
public final class MiscellaneousDispatcher {
    private static final Logger lgr = LoggerFactory.getLogger(MiscellaneousDispatcher.class);
    private static DiscordBotListAPI dblApi;

    public static void initApis(String dblToken, String boatsToken) {
        dblApi = new DiscordBotListAPI.Builder().token(dblToken)
                .botId(thermo.getSelfUser().getId())
                .build();

        // Post server stats
        Runnable run = () -> {
            long currentServers = thermo.getGuildCache().size();
            dblApi.setStats((int) currentServers);
            Boats4J.postStats(currentServers, thermo.getSelfUser().getId(), boatsToken);
            lgr.info("Posted server stats. Current Guilds: [" + currentServers + "]");
        };

        // Every two minutes.
        Thermostat.SCHEDULED_EXECUTOR.scheduleAtFixedRate(run, 10, 120, TimeUnit.SECONDS);
    }
}
