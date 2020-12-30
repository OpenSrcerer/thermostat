package thermostat;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import thermostat.dispatchers.MiscellaneousDispatcher;
import thermostat.mySQL.DataSource;
import thermostat.thermoFunctions.commands.events.Ready;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
 * @version 1.0.0_beta2
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

    /**
     * Start Thermostat and initialize all needed variables.
     * @throws Exception Any Exception that may occur.
     * @throws Error Any Error that may occur while loading.
     */
    public static void initializeThermostat() throws Exception, Error {
        String[] config = initializeTokens();
        DataSource.initializeDataSource();

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

        MiscellaneousDispatcher.setDblApi(config[2]);
        thermo.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.competing("fast loading..."));
    }

    /**
     * Reads the config.json file and parses the data into a usable
     * array of strings.
     * @return Array of configuration tokens.
     * @throws Exception If I/O operations had an issue.
     */
    private static String[] initializeTokens() throws Exception {
        String[] tokens = new String[3];

        InputStream configFile = Thermostat.class.getClassLoader().getResourceAsStream("config.json");

        if (configFile == null) {
            Run.lgr.error("JSON config file not found.");
            return tokens;
        }

        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(
                new InputStreamReader(configFile, StandardCharsets.UTF_8
                )
        );

        tokens[0] = jsonObject.get("Prefix").toString();
        tokens[1] = jsonObject.get("Token").toString();
        tokens[2] = jsonObject.get("DBLToken").toString();

        return tokens;
    }

    /**
     * Shuts down Thermostat in case of an Error/Exception thrown
     * or failure to initialize necessary configuration files.
     */
    public static void shutdownThermostat() {
        executor.shutdown();

        if (thermo != null) {
            thermo.shutdown();
        }

        DataSource.closeDataSource();

        System.exit(-1);
    }
}
