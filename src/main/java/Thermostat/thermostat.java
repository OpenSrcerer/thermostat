package thermostat;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import thermostat.mySQL.DataSource;
import thermostat.thermoFunctions.commands.events.Ready;
import thermostat.thermoFunctions.threaded.InitTokens;

import javax.security.auth.login.LoginException;
import java.util.EnumSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Thermostat
 * thermostat.java is the main file used to initiate the
 * needed variables, flags, and gateway intents for
 * running the bot.
 *
 * @author OpenSrcerer
 * @version 1.0.0_beta
 * @since 2020-04-17
 */
public abstract class Thermostat {
    /**
     * Pool of threads used by every non JDA related action in this application.
     */
    public static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);

    /**
     * Static JDA instance for thermostat.
     */
    public static JDA thermo;

    /**
     * Thermostat's default prefix.
     */
    public static String prefix;

    public static void initializeThermostat() throws LoginException {
        String[] config = new InitTokens().call();
        prefix = config[0];

        thermo = JDABuilder
                .create(
                        config[1],
                        EnumSet.of(
                                GatewayIntent.GUILD_MESSAGES,
                                GatewayIntent.GUILD_MESSAGE_REACTIONS
                        )
                )
                .disableCache(
                        CacheFlag.ACTIVITY,
                        CacheFlag.EMOTE,
                        CacheFlag.CLIENT_STATUS,
                        CacheFlag.MEMBER_OVERRIDES,
                        CacheFlag.VOICE_STATE
                )
                .setMemberCachePolicy(MemberCachePolicy.NONE)
                .setChunkingFilter(ChunkingFilter.NONE)
                .setEnableShutdownHook(true)
                .addEventListeners(new Ready())
                .build();

        // MiscellaneousDispatcher.setDblApi(config[2]);
        thermo.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.competing("fast loading..."));
    }

    /**
     * Shuts down Thermostat in case of a major Error thrown.
     */
    public static void shutdownThermostat() {
        executor.shutdown();
        thermo.shutdown();
        DataSource.closeDataSource();
    }
}
