package thermostat;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.discordbots.api.client.DiscordBotListAPI;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.thermoFunctions.listeners.Ready;

import javax.security.auth.login.LoginException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.EnumSet;

/**
 * Thermostat
 * thermostat.java is the main file used to initiate the
 * needed variables, flags, and gateway intents for
 * running the bot.
 *
 * @author OpenSrcerer
 * @version 0.8.0
 * @since 2020-04-17
 */

public class thermostat {
    // Bot Initialization Variables
    public static JDA thermo;
    public static DiscordBotListAPI thermoAPI;
    private static final Logger logger = LoggerFactory.getLogger(thermostat.class);

    // Intents to using the Discord Gateway
    private static final EnumSet<GatewayIntent> intents = EnumSet.of(
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MESSAGE_REACTIONS
    );

    public static String prefix, JDAToken, DBLToken;

    /**
     * Main run function for the bot. Event listeners
     * are defined in {@link Ready}.
     */
    public static void main(String[] args) {

        {
            JSONParser parser = new JSONParser();

            try {
                URL JSONConfig = thermostat.class.getClassLoader().getResource("config.json");

                if (JSONConfig == null) {
                    logger.error("JSON config file not found.");
                    return;
                }

                Object obj = parser.parse(new FileReader(JSONConfig.getFile()));
                JSONObject jsonObject = (JSONObject) obj;

                prefix = jsonObject.get("Prefix").toString();
                JDAToken = jsonObject.get("Token").toString();
                DBLToken = jsonObject.get("DBLToken").toString();

            } catch (FileNotFoundException ex) {
                logger.error("JSON config file not found.");
                return;
            } catch (ParseException ex) {
                logger.error("Parsing error!", ex);
                return;
            } catch (IOException ex) {
                logger.error("I/O Error while parsing JSON file.", ex);
                return;
            }
        }

        try {
            thermo = JDABuilder
                    .create(JDAToken, intents)
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
        } catch (LoginException ex) {
            logger.error("Error while logging in the Discord Gateway!", ex);
        }

        thermoAPI = new DiscordBotListAPI.Builder()
                .token(DBLToken)
                .botId(thermo.getSelfUser().getId())
                .build();

        thermo.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.listening("loading sounds..."));
    }
}
