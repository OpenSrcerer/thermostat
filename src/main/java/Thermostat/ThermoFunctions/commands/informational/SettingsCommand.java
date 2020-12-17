package thermostat.thermoFunctions.commands.informational;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.mySQL.DataSource;
import thermostat.preparedStatements.ErrorEmbeds;
import thermostat.preparedStatements.GenericEmbeds;
import thermostat.thermoFunctions.Messages;
import thermostat.thermoFunctions.commands.Command;
import thermostat.thermoFunctions.entities.CommandType;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.List;

import static thermostat.thermoFunctions.Functions.parseMention;

/**
 * Command that when called, shows an embed
 * with the settings of a specific channel.
 */
@SuppressWarnings("ConstantConditions")
public class SettingsCommand implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(SettingsCommand.class);

    private final GuildMessageReceivedEvent data;
    private final List<String> arguments;
    private final String prefix;

    public SettingsCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) {
        this.data = data;
        this.arguments = arguments;
        this.prefix = prefix;

        if (validateEvent(data)) {
            checkPermissionsAndQueue(this);
        }
    }

    /**
     * Command form: th!settings [channel]
     */
    @Override
    public void run() {
        // to contain channel id for modification
        String channelId;

        // #1 - find the number of arguments provided
        // th!settings
        if (arguments.isEmpty()) {
            channelId = data.getChannel().getId();
        }
        // th!settings [channel]
        else {
            channelId = parseMention(arguments.get(0), "#");

            // if channel doesn't exist, show error msg
            if (channelId.isEmpty() || data.getGuild().getTextChannelById(channelId) == null) {
                Messages.sendMessage(data.getChannel(), ErrorEmbeds.channelNotFound(arguments.get(0)));
                return;
            }
        }

        // #2 - Check if channel has been monitored before.
        final int min, max;
        final float sens;
        final boolean monitored, filtered;

        try {
                addIfNotInDb(data.getGuild().getId(), channelId);
                List<Object> objects = DataSource.getSettingsPackage(channelId);

                min = (int) objects.get(0);
                max = (int) objects.get(1);
                sens = (float) objects.get(2);
                monitored = (boolean) objects.get(3);
                filtered = (boolean) objects.get(4);
        } catch (SQLException ex) {
            Messages.sendMessage(data.getChannel(), ErrorEmbeds.errFatal("running the command again", ex.getLocalizedMessage()));
            lgr.warn("(" + data.getGuild().getName() + "/" + data.getGuild().getId() + ") - " + ex.toString());
            return;
        }

        TextChannel settingsChannel = data.getGuild().getTextChannelById(channelId);

        if (settingsChannel != null) {
            Messages.sendMessage(data.getChannel(),
                    GenericEmbeds.channelSettings(settingsChannel.getName(),
                            data.getMember().getUser().getAsTag(),
                            data.getMember().getUser().getAvatarUrl(),
                            min,
                            max,
                            sens,
                            monitored,
                            filtered
                    )
            );
            lgr.info("Successfully executed on (" + data.getGuild().getName() + "/" + data.getGuild().getId() + ").");
        }
    }

    @Override
    public GuildMessageReceivedEvent getEvent() {
        return data;
    }

    @Override
    public CommandType getType() {
        return CommandType.SETTINGS;
    }

    @Override
    public Logger getLogger() {
        return lgr;
    }
}
