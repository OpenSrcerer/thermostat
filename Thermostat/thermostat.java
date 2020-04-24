package Thermostat;

import Thermostat.ThermoFunctions.Commands.Monitor;
import Thermostat.ThermoFunctions.Commands.UnMonitor;
import Thermostat.ThermoFunctions.MonitorThreads.ChannelListener;
import Thermostat.ThermoFunctions.Listeners.GuildJoin;

import Thermostat.ThermoFunctions.Listeners.GuildLeave;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import java.util.Arrays;
import java.util.Collection;

/**
 * <h1>Thermostat Bot</h1>*
 * thermostat.java is the main file used to initiate the
 * needed variables, listeners, and gateway intents for
 * running the bot.
 * <p>
 *
 *
 * @author  Weed-Pot
 * @version 0.0.1
 * @since   2020-04-17
 */

public class thermostat {
    // Bot Initialization Variables
    public static JDA thermo;
    public static String prefix = "th!";

    // Intents to using the Discord Gateway
    private static Collection<GatewayIntent> intents = Arrays.asList(
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.GUILD_MESSAGES
    );

    /**
     * Main run function for the bot, where all
     * the Event Listeners are initiated and channels
     * are monitored.
     * @param args default java main function
     * @throws LoginException
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
                .build();

        // Main Channel Monitoring Class Call
        ChannelListener CL = new ChannelListener(thermo);

        // Commands Listeners
        thermo.addEventListener(new Monitor());
        thermo.addEventListener(new UnMonitor());

        // Other Event Listeners
        thermo.addEventListener(new GuildJoin());
        thermo.addEventListener(new GuildLeave());
    }
}
