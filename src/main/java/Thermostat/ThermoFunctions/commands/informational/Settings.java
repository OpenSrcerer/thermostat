package thermostat.thermoFunctions.commands.informational;

import thermostat.Embeds;
import thermostat.mySQL.Create;
import thermostat.mySQL.DataSource;
import thermostat.thermoFunctions.Messages;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;

import static thermostat.thermoFunctions.Functions.parseMention;

/**
 * Command that when called, shows an embed
 * with the settings of a specific channel.
 */
public class Settings
{
    public static void execute(ArrayList<String> args, @Nonnull Guild eventGuild, @Nonnull TextChannel eventChannel, @Nonnull Member eventMember) {
        
        // checks if event member has permission
        if (!eventMember.hasPermission(Permission.MANAGE_CHANNEL)) {
            Messages.sendMessage(eventChannel, Embeds.specifyChannels());
            return;
        }

        // to contain channel id for modification
        String channelId;

        // if there are more than two arguments
        // (command init + channel)
        if (args.size() >= 2) {
            args.remove(0);
            args.set(0, parseMention(args.get(0), "#"));
            channelId = args.get(0);

            // if channel doesn't exist, show error msg
            if (args.get(0).isBlank() || eventGuild.getTextChannelById(args.get(0)) == null)
            {
                Messages.sendMessage(eventChannel, Embeds.channelNotFound());
                return;
            }
        // if only th!s is sent
        } else {
            channelId = eventChannel.getId();
        }

        // connects to database and creates channel
        try {
            // silent guild adder
            if (!DataSource.checkDatabaseForData("SELECT * FROM GUILDS WHERE GUILD_ID = " + eventGuild.getId()))
                Create.Guild(eventGuild.getId());

            {
                int max = DataSource.queryInt("SELECT MAX_SLOW FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = " + channelId);

                if (max == -1) {
                    Messages.sendMessage(eventChannel, Embeds.channelNeverMonitored());
                    return;
                }

                TextChannel settingsChannel = eventGuild.getTextChannelById(channelId);

                if (settingsChannel != null) {
                    Messages.sendMessage(eventChannel,
                            Embeds.channelSettings(settingsChannel.getName(),
                                    eventMember.getUser().getAsTag(),
                                    max,
                                    DataSource.queryInt("SELECT MIN_SLOW FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = " + channelId),
                                    DataSource.querySens("SELECT SENSOFFSET FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = " + channelId),
                                    DataSource.queryBool("SELECT MONITORED FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = " + channelId)
                            )
                    );
                }
            }

        } catch (Exception ex) {
            Logger lgr = LoggerFactory.getLogger(Settings.class);
            lgr.error(ex.getMessage(), ex);
            Messages.sendMessage(eventChannel, Embeds.fatalError());
        }
    }
}
