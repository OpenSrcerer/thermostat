package thermostat.commands.informational;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.Embeds.ErrorEmbeds;
import thermostat.Embeds.GenericEmbeds;
import thermostat.Embeds.HelpEmbeds;
import thermostat.commands.Command;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.mySQL.DataSource;
import thermostat.util.Functions;
import thermostat.util.enumeration.CommandType;

import javax.annotation.Nonnull;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static thermostat.util.ArgumentParser.parseArguments;
import static thermostat.util.Functions.parseMention;

/**
 * Command that when called, shows an embed
 * with the settings of a specific channel.
 */
@SuppressWarnings("ConstantConditions")
public class SettingsCommand implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(SettingsCommand.class);

    private GuildMessageReceivedEvent data;
    private Map<String, List<String>> parameters;
    private final String prefix;
    private final long commandId;

    public SettingsCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) {
        this.commandId = Functions.getCommandId();
        this.prefix = prefix;

        try {
            this.parameters = parseArguments(arguments);
        } catch (Exception ex) {
            ResponseDispatcher.commandFailed(this, ErrorEmbeds.inputError(ex.getLocalizedMessage(), this.commandId), ex);
            return;
        }

        if (validateEvent(data)) {
            this.data = data;
        } else {
            ResponseDispatcher.commandFailed(this, ErrorEmbeds.error("Event was not valid. Please try again."), "Event had a null member.");
            return;
        }

        checkPermissionsAndQueue(this);
    }

    /**
     * Command form: th!settings
     * Switches:
     * -c
     */
    @Override
    public void run() {
        List<String> channels = parameters.get("c");
        String channelId;

        if (channels == null) {
            ResponseDispatcher.commandFailed(this,
                    HelpEmbeds.expandedHelpSettings(prefix),
                    "User did not provide a bound argument.");
            return;
        }

        // Check if user has given a channel. If they have not, assign
        // command channel to the channel where the event was triggered.
        if (channels.isEmpty()) {
            channelId = data.getChannel().getId();
        } else {
            channelId = parseMention(channels.get(0), "#");

            // if channel doesn't exist, show error msg
            if (channelId.isEmpty() || data.getGuild().getTextChannelById(channelId) == null) {
                ResponseDispatcher.commandFailed(this,
                        ErrorEmbeds.inputError("Channel \"" + channels.get(0) + "\" was not found.", commandId),
                        "Channel that user provided wasn't found.");
                return;
            }
        }

        settingsAction(channelId);
    }

    private void settingsAction(String channelId) {
        // Retrieve the settings values from the database and send a response.
        try {
            DataSource.execute(conn -> {
                int min = 0, max = 0;
                float sens = 0;
                boolean monitored = false, filtered = false;

                PreparedStatement statement = conn.prepareStatement("SELECT MIN_SLOW, MAX_SLOW, SENSOFFSET, MONITORED, FILTERED " +
                        "FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = ?");
                statement.setString(1, channelId);
                ResultSet rs = statement.executeQuery();

                if (rs.next()) {
                    min = rs.getInt(1);
                    // Max
                    max = rs.getInt(2);
                    // Sens
                    sens = rs.getFloat(3);
                    // Monitored
                    monitored = rs.getBoolean(4);
                    // Filtered
                    filtered = rs.getBoolean(5);
                }

                TextChannel settingsChannel = data.getGuild().getTextChannelById(channelId);

                if (settingsChannel != null) {
                    ResponseDispatcher.commandSucceeded(this,
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
                }

                return null;
            });
        } catch (SQLException ex) {
            ResponseDispatcher.commandFailed(this,
                    ErrorEmbeds.error(ex.getLocalizedMessage(), commandId),
                    ex);
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

    @Override
    public long getId() {
        return commandId;
    }
}
