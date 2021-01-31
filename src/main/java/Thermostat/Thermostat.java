package thermostat;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.exceptions.ParsingException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.jetbrains.annotations.NotNull;
import thermostat.commands.events.Ready;
import thermostat.mySQL.DataSource;
import thermostat.util.Constants;

import java.io.InputStream;
import java.util.EnumSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

/**
 * Thermostat
 * thermostat.java is the main file used to initiate the
 * needed variables, flags, and gateway intents for
 * running the bot.
 *
 * @author OpenSrcerer
 * @version 1.0.0
 * @since 2020-04-17
 */
public abstract class Thermostat {
    /**
     * Pool of threads used by every non JDA related action in this application.
     */
    public static final ScheduledExecutorService executor;

    static {
        ThreadFactory threadFactory = new ThreadFactory() {
            private int counter = 1;

            @Override
            public Thread newThread(@NotNull Runnable r) {
                return new Thread(r, "CommandDispatch-" + counter++);
            }
        };

        executor = Executors.newScheduledThreadPool(4, threadFactory);
    }

    /**
     * Singular JDA instance for thermostat.
     */
    public static JDA thermo;

    /**
     * Start Thermostat and initialize all needed variables.
     * @throws Exception Any Exception that may occur.
     * @throws Error Any Error that may occur while loading.
     */
    protected static void initializeThermostat() throws Exception, Error {
        String[] config = initializeTokens();

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

        thermo.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.competing("loading..."));
        DataSource.initializeDataSource();
        Constants.setConstants(config[0], thermo.getSelfUser().getId(), thermo.getSelfUser().getAvatarUrl());
        //MiscellaneousDispatcher.initApis(config[2], config[3]);
    }

    /**
     * Reads the config.json file and parses the data into a usable
     * array of strings.
     * @return Array of configuration tokens.
     * @throws ParsingException If I/O operations had an issue.
     */
    private static String[] initializeTokens() throws ParsingException {
        String[] tokens = new String[4];

        InputStream configFile = Thermostat.class.getClassLoader().getResourceAsStream("config.json");

        if (configFile == null) {
            Run.lgr.error("JSON config file not found.");
            return tokens;
        }

        DataObject config = DataObject.fromJson(configFile);

        tokens[0] = config.getString("Prefix");
        tokens[1] = config.getString("Token");
        tokens[2] = config.getString("DBLToken");
        tokens[3] = config.getString("BoatsToken");

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
