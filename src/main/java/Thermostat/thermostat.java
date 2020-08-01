package Thermostat;

import Thermostat.ThermoFunctions.Listeners.Ready;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;

/**
 * <h1>Thermostat Bot</h1>*
 * thermostat.java is the main file used to initiate the
 * needed variables, listeners, and gateway intents for
 * running the bot.
 * <p>
 *
 * @author Weed-Pot
 * @version 0.6.6
 * @since 2020-04-17
 */

public class thermostat {
    // Bot Initialization Variables
    public static JDA thermo;
    // default prefix
    public static String prefix = "th!";

    // Intents to using the Discord Gateway
    private static EnumSet<GatewayIntent> intents = EnumSet.of(
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MESSAGE_REACTIONS
    );

    /**
     * Main run function for the bot. Event listeners
     * are defined in {@link Ready}.
     *
     * @param args default java main function
     * @throws LoginException Issue occurred when logging in.
     */
    public static void main(String[] args) throws LoginException {

        // adjust the cache flags here
        thermo = JDABuilder
                .create("YOUR-TOKEN-HERE", intents)
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
                .build();


        thermo.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.listening("loading music..."));
        thermo.addEventListener(new Ready());
    }
}
