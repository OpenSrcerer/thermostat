package thermostat;

import thermostat.thermoFunctions.listeners.Ready;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.discordbots.api.client.DiscordBotListAPI;

import javax.security.auth.login.LoginException;
import java.util.EnumSet;

/**
 * Thermostat
 * thermostat.java is the main file used to initiate the
 * needed variables, flags, and gateway intents for
 * running the bot.
 *
 *
 * @author OpenSrcerer
 * @version 0.7.0
 * @since 2020-04-17
 */

public class thermostat {
    // Bot Initialization Variables
    public static JDA thermo;
    public static DiscordBotListAPI thermoAPI;
    // default prefix
    public static final String prefix = "th!";

    // Intents to using the Discord Gateway
    private static final EnumSet<GatewayIntent> intents = EnumSet.of(
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MESSAGE_REACTIONS
    );

    /**
     * Main run function for the bot. Event listeners
     * are defined in {@link Ready}.
     *
     * @throws LoginException Issue occurred when logging in the Discord Gateway.
     */
    public static void main(String[] args) throws LoginException {

        thermo = JDABuilder
                .create("🎂", intents)
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

        // API Variable for DiscordBotList
        thermoAPI = new DiscordBotListAPI.Builder()
                .token("🎂")
                .botId("700341788136833065")
                .build();

        thermo.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.listening("loading music..."));
    }
}
