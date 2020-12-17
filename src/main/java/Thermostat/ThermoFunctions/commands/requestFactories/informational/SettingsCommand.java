package thermostat.thermoFunctions.commands.requestFactories.informational;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.mySQL.DataSource;
import thermostat.preparedStatements.ErrorEmbeds;
import thermostat.preparedStatements.GenericEmbeds;
import thermostat.thermoFunctions.Messages;
import thermostat.thermoFunctions.commands.CommandData;
import thermostat.thermoFunctions.commands.requestFactories.Command;
import thermostat.thermoFunctions.entities.RequestType;

import java.sql.SQLException;
import java.util.List;

import static thermostat.thermoFunctions.Functions.parseMention;

/**
 * Command that when called, shows an embed
 * with the settings of a specific channel.
 */
public class SettingsCommand implements Command {

    private static final Logger lgr = LoggerFactory.getLogger(SettingsCommand.class);

    private final CommandData data;

    public SettingsCommand(CommandData data) {
        this.data = data;

        checkPermissionsAndExecute(RequestType.SETTINGS, data.member(), data.channel(), lgr);
    }

    /**
     * Command form: th!settings [channel]
     * @return
     */
    @Override
    public MessageEmbed execute() {
        // to contain channel id for modification
        String channelId;

        // #1 - find the number of arguments provided
        // th!settings
        if (data.arguments().isEmpty()) {
            channelId = data.channel().getId();
        }
        // th!settings [channel]
        else {
            channelId = parseMention(data.arguments().get(0), "#");

            // if channel doesn't exist, show error msg
            if (channelId.isEmpty() || data.guild().getTextChannelById(channelId) == null) {
                Messages.sendMessage(data.channel(), ErrorEmbeds.channelNotFound(data.arguments().get(0)));
                return;
            }
        }

        // #2 - Check if channel has been monitored before.
        final int min, max;
        final float sens;
        final boolean monitored, filtered;

        try {
                addIfNotInDb(data.guild().getId(), channelId);
                List<Object> objects = DataSource.getSettingsPackage(channelId);

                min = (int) objects.get(0);
                max = (int) objects.get(1);
                sens = (float) objects.get(2);
                monitored = (boolean) objects.get(3);
                filtered = (boolean) objects.get(4);
        } catch (SQLException ex) {
            Messages.sendMessage(data.channel(), ErrorEmbeds.errFatal("running the command again", ex.getLocalizedMessage()));
            lgr.warn("(" + data.guild().getName() + "/" + data.guild().getId() + ") - " + ex.toString());
            return;
        }

        TextChannel settingsChannel = data.guild().getTextChannelById(channelId);

        if (settingsChannel != null) {
            Messages.sendMessage(data.channel(),
                    GenericEmbeds.channelSettings(settingsChannel.getName(),
                            data.member().getUser().getAsTag(),
                            data.member().getUser().getAvatarUrl(),
                            min,
                            max,
                            sens,
                            monitored,
                            filtered
                    )
            );
            lgr.info("Successfully executed on (" + data.guild().getName() + "/" + data.guild().getId() + ").");
        }
    }

    @Override
    public CommandData getData() {
        return data;
    }
}
