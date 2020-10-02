package thermostat.thermoFunctions.commands.informational;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.preparedStatements.ErrorEmbeds;
import thermostat.preparedStatements.GenericEmbeds;
import thermostat.mySQL.DataSource;
import thermostat.thermoFunctions.Messages;
import thermostat.thermoFunctions.commands.CommandEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumSet;

import static thermostat.thermoFunctions.Functions.parseMention;

/**
 * Command that when called, shows an embed
 * with the settings of a specific channel.
 */
public class Settings implements CommandEvent {

    private static final Logger lgr = LoggerFactory.getLogger(Settings.class);

    public Settings(ArrayList<String> args, @Nonnull Guild eventGuild, @Nonnull TextChannel eventChannel, @Nonnull Member eventMember) {

    }

    @Override
    public void checkPermissions() {

    }

    @NotNull
    @Override
    public EnumSet<Permission> findMissingPermissions(EnumSet<Permission> permissionsToSeek, EnumSet<Permission> givenPermissions) {
        return null;
    }

    @Override
    public void execute() {

        // checks if event member has permission
        if (!eventMember.hasPermission(Permission.MANAGE_CHANNEL)) {
            Messages.sendMessage(eventChannel, ErrorEmbeds.specifyChannels());
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
            if (args.get(0).isBlank() || eventGuild.getTextChannelById(args.get(0)) == null) {
                Messages.sendMessage(eventChannel, ErrorEmbeds.channelNotFound());
                return;
            }
            // if only th!s is sent
        } else {
            channelId = eventChannel.getId();
        }

        // connects to database and creates channel
        try {
            int max = DataSource.queryInt("SELECT MAX_SLOW FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = ?", channelId);

            if (max == -1) {
                Messages.sendMessage(eventChannel, GenericEmbeds.channelNeverMonitored());
                return;
            }

            TextChannel settingsChannel = eventGuild.getTextChannelById(channelId);

            if (settingsChannel != null) {
                Messages.sendMessage(eventChannel,
                        GenericEmbeds.channelSettings(settingsChannel.getName(),
                                eventMember.getUser().getAsTag(),
                                eventMember.getUser().getAvatarUrl(),
                                max,
                                DataSource.queryInt("SELECT MIN_SLOW FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = ?", channelId),
                                DataSource.querySens("SELECT SENSOFFSET FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = ?", channelId),
                                DataSource.queryBool("SELECT MONITORED FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = ?", channelId),
                                DataSource.queryBool("SELECT FILTERED FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = ?", channelId)
                        )
                );
            }

        } catch (Exception ex) {
            lgr.error(ex.getMessage(), ex);
            Messages.sendMessage(eventChannel, ErrorEmbeds.errFatal());
        }
    }
}
