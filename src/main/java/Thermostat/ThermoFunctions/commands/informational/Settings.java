package thermostat.thermoFunctions.commands.informational;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.mySQL.DataSource;
import thermostat.preparedStatements.ErrorEmbeds;
import thermostat.preparedStatements.GenericEmbeds;
import thermostat.thermoFunctions.Messages;
import thermostat.thermoFunctions.commands.CommandEvent;
import thermostat.thermoFunctions.entities.CommandType;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static thermostat.thermoFunctions.Functions.parseMention;

/**
 * Command that when called, shows an embed
 * with the settings of a specific channel.
 */
public class Settings implements CommandEvent {

    private static final Logger lgr = LoggerFactory.getLogger(Settings.class);

    private final Guild eventGuild;
    private final TextChannel eventChannel;
    private final Member eventMember;
    private final ArrayList<String> args;

    public Settings(Guild eg, TextChannel tc, Member em, ArrayList<String> ag) {
        eventGuild = eg;
        eventChannel = tc;
        eventMember = em;
        args = ag;

        checkPermissionsAndExecute(CommandType.SETTINGS, eventMember, eventChannel, lgr);
    }

    /**
     * Command form: th!settings [channel]
     */
    @Override
    public void execute() {
        // to contain channel id for modification
        String channelId;

        // #1 - find the number of arguments provided
        // th!settings
        if (args.isEmpty()) {
            channelId = eventChannel.getId();
        }
        // th!settings [channel]
        else {
            channelId = parseMention(args.get(0), "#");

            // if channel doesn't exist, show error msg
            if (channelId.isEmpty() || eventGuild.getTextChannelById(channelId) == null) {
                Messages.sendMessage(eventChannel, ErrorEmbeds.channelNotFound(args.get(0)));
                return;
            }
        }

        // #2 - Check if channel has been monitored before.
        final int min, max;
        final float sens;
        final boolean monitored, filtered;

        try {
                addIfNotInDb(eventGuild.getId(), channelId);
                List<Object> objects = DataSource.getSettingsPackage(channelId);

                min = (int) objects.get(0);
                max = (int) objects.get(1);
                sens = (float) objects.get(2);
                monitored = (boolean) objects.get(3);
                filtered = (boolean) objects.get(4);
        } catch (SQLException ex) {
            Messages.sendMessage(eventChannel, ErrorEmbeds.errFatal("running the command again", ex.getLocalizedMessage()));
            lgr.warn("(" + eventGuild.getName() + "/" + eventGuild.getId() + ") - " + ex.toString());
            return;
        }

        TextChannel settingsChannel = eventGuild.getTextChannelById(channelId);

        if (settingsChannel != null) {
            Messages.sendMessage(eventChannel,
                    GenericEmbeds.channelSettings(settingsChannel.getName(),
                            eventMember.getUser().getAsTag(),
                            eventMember.getUser().getAvatarUrl(),
                            min,
                            max,
                            sens,
                            monitored,
                            filtered
                    )
            );
            lgr.info("Successfully executed on (" + eventGuild.getName() + "/" + eventGuild.getId() + ").");
        }
    }
}
