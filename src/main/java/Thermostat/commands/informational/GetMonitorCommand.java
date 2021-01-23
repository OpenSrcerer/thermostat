package thermostat.commands.informational;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.Embeds.ErrorEmbeds;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.mySQL.DataSource;
import thermostat.Embeds.DynamicEmbeds;
import thermostat.util.MiscellaneousFunctions;
import thermostat.commands.Command;
import thermostat.util.enumeration.CommandType;

import javax.annotation.Nonnull;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Retrieves all the currently monitored channels
 * and sends them as a single embed in the channel
 * where the command was called.
 */
@SuppressWarnings("ConstantConditions")
public class GetMonitorCommand implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(GetMonitorCommand.class);

    private final GuildMessageReceivedEvent data;
    private final long commandId;

    public GetMonitorCommand(@Nonnull GuildMessageReceivedEvent data) {
        this.data = data;
        this.commandId = MiscellaneousFunctions.getCommandId();

        if (validateEvent(data)) {
            checkPermissionsAndQueue(this);
        }
    }

    /**
     * Command form: th!getmonitor
     * No switches, no arguments.
     */
    @Override
    public void run() {
        String monitoredString = "None.", filteredString = "None.";
        List<String> monitoredList = new ArrayList<>();
        List<String> filteredList = new ArrayList<>();

        // #1 - Grab monitored and filtered channels from the DB
        try {
            DataSource.execute(conn -> {
                PreparedStatement statement = conn.prepareStatement("SELECT CHANNELS.CHANNEL_ID FROM CHANNELS " +
                        "JOIN CHANNEL_SETTINGS ON (CHANNELS.CHANNEL_ID = CHANNEL_SETTINGS.CHANNEL_ID) " +
                        "WHERE CHANNELS.GUILD_ID = ? AND CHANNEL_SETTINGS.MONITORED = 1");
                statement.setString(1, data.getGuild().getId());
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    monitoredList.add(rs.getString(1));
                }

                statement = conn.prepareStatement("SELECT CHANNELS.CHANNEL_ID FROM CHANNELS " +
                        "JOIN CHANNEL_SETTINGS ON (CHANNELS.CHANNEL_ID = CHANNEL_SETTINGS.CHANNEL_ID) " +
                        "WHERE CHANNELS.GUILD_ID = ? AND CHANNEL_SETTINGS.FILTERED = 1");
                statement.setString(1, data.getGuild().getId());
                rs = statement.executeQuery();

                while (rs.next()) {
                    filteredList.add(rs.getString(1));
                }

                return null;
            });
        } catch (SQLException ex) {
            ResponseDispatcher.commandFailed(this,
                    ErrorEmbeds.error(ex.getLocalizedMessage(), this.commandId), ex
                    );
        }

        // #2 - Converts channel ids to mentions
        {
            if (monitoredList != null && !monitoredList.isEmpty()) {
                monitoredString = getEmbedString(monitoredList);
            }

            if (filteredList != null && !filteredList.isEmpty()) {
                filteredString = getEmbedString(filteredList);
            }
        }

        // #3 - Sends embed with information.
        ResponseDispatcher.commandSucceeded(this,
                DynamicEmbeds.dynamicEmbed(
                        Arrays.asList(
                                "Channels currently being monitored:",
                                monitoredString,
                                "Channels currently being filtered:",
                                filteredString
                        ),
                        data.getMember().getUser(), commandId
                )
        );
    }

    /**
     * Build a String that contains channelIds, to be used in an embed.
     * @param list List of channel IDs.
     * @return String that contains channelIds, to be used in an embed.
     */
    private String getEmbedString(List<String> list) {
        StringBuilder string = new StringBuilder();

        // iterate through retrieved array, adding
        // every monitored/filtered guild to the ending embed
        for (String it : list) {
            TextChannel filteredChannel = data.getGuild().getTextChannelById(it);

            if (filteredChannel != null)
                string.append("<#").append(filteredChannel.getId()).append("> ");
            else
                string.append(it).append(" ");
        }

        return string.toString();
    }

    @Override
    public GuildMessageReceivedEvent getEvent() {
        return data;
    }

    @Override
    public CommandType getType() {
        return CommandType.GETMONITOR;
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