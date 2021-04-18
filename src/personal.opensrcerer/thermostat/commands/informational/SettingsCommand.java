package thermostat.commands.informational;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.commands.Command;
import thermostat.dispatchers.CommandDispatcher;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.embeds.Embeds;
import thermostat.mySQL.DataSource;
import thermostat.util.Constants;
import thermostat.util.entities.CommandContext;
import thermostat.util.entities.SettingsData;
import thermostat.util.enumeration.CommandType;
import thermostat.util.enumeration.EmbedType;

import javax.annotation.Nonnull;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static thermostat.util.ArgumentParser.hasArguments;
import static thermostat.util.ArgumentParser.parseMention;

/**
 * Command that when called, shows an embed
 * with the settings of a specific channel.
 */
public class SettingsCommand implements Command {
    /**
     * Logger for this class.
     */
    private static final Logger lgr = LoggerFactory.getLogger(SettingsCommand.class);

    /**
     * Data for this command.
     */
    private final CommandContext data;

    public SettingsCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) {
        this.data = new CommandContext(data, arguments, prefix);

        if (this.data.parameters == null) {
            ResponseDispatcher.commandFailed(
                    this,
                    Embeds.getEmbed(EmbedType.HELP_SETTINGS, this.data),
                    "Bad arguments.");
            return;
        }

        CommandDispatcher.checkPermissionsAndQueue(this);
    }

    /**
     * Command form: th!settings
     * Switches:
     * -c
     */
    @Override
    public void run() {
        final List<String> channels = data.parameters.get("c");
        TextChannel channel;

        // Check if user has given a channel. If they have not, assign
        // command channel to the channel where the event was triggered.
        if (!hasArguments(channels)) {
            channel = data.event.getChannel();
        } else {
            channel = data.event.getGuild().getTextChannelById(parseMention(channels.get(0), "#"));
        }

        // if channel doesn't exist, show error msg
        if (channel == null) {
            ResponseDispatcher.commandFailed(this,
                    Embeds.getEmbed(EmbedType.ERR, data,
                            "Channel \"" + channels.get(0) + "\" was not found.)"),
                    "Channel that user provided wasn't found."
            );
            return;
        }

        settingsAction(channel);
    }

    private void settingsAction(final @Nonnull TextChannel channel) {
        // Retrieve the settings values from the database and send a response.
        try {
            DataSource.demand(conn -> {
                int min = 0, max = 0, cachingSize = Constants.DEFAULT_CACHING_SIZE;
                float sens = 0;
                boolean monitored = false, filtered = false;

                PreparedStatement statement = conn.prepareStatement("SELECT MIN_SLOW, MAX_SLOW, SENSOFFSET, MONITORED, FILTERED " +
                        "FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = ?");
                statement.setString(1, channel.getId());
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

                statement = conn.prepareStatement("SELECT CACHING_SIZE FROM GUILDS WHERE GUILD_ID = ?");
                statement.setString(1, channel.getGuild().getId());
                rs = statement.executeQuery();

                if (rs.next()) {
                    cachingSize = rs.getInt(1);
                }

                SettingsData settingsData = new SettingsData(channel.getName(), min, max, cachingSize, sens, monitored, filtered);
                ResponseDispatcher.commandSucceeded(this,
                        Embeds.getEmbed(EmbedType.CHANNEL_SETTINGS, data, settingsData)
                );

                return null;
            });
        } catch (SQLException ex) {
            ResponseDispatcher.commandFailed(this,
                    Embeds.getEmbed(EmbedType.ERR, data, ex.getMessage()),
                    ex);
        }
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
    public CommandContext getData() {
        return data;
    }
}
