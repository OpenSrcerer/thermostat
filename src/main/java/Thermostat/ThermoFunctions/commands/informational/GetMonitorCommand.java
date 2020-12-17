package thermostat.thermoFunctions.commands.informational;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.mySQL.DataSource;
import thermostat.preparedStatements.DynamicEmbeds;
import thermostat.thermoFunctions.Messages;
import thermostat.thermoFunctions.commands.Command;
import thermostat.thermoFunctions.entities.CommandType;

import javax.annotation.Nonnull;
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

    public GetMonitorCommand(@Nonnull GuildMessageReceivedEvent data) {
        this.data = data;

        if (validateEvent(data)) {
            checkPermissionsAndQueue(this);
        }
    }

    /**
     * Command form: th!getmonitor
     */
    @Override
    public void run() {
        String monitoredString = "None.", filteredString = "None.";

        // #1 - Grab monitored and filtered channels from the DB
        List<String> monitoredList = DataSource.queryStringArray("SELECT CHANNELS.CHANNEL_ID FROM CHANNELS " +
                        "JOIN CHANNEL_SETTINGS ON (CHANNELS.CHANNEL_ID = CHANNEL_SETTINGS.CHANNEL_ID) " +
                        "WHERE CHANNELS.GUILD_ID = ? AND CHANNEL_SETTINGS.MONITORED = 1",
                data.getGuild().getId());

        List<String> filteredList = DataSource.queryStringArray("SELECT CHANNELS.CHANNEL_ID FROM CHANNELS " +
                        "JOIN CHANNEL_SETTINGS ON (CHANNELS.CHANNEL_ID = CHANNEL_SETTINGS.CHANNEL_ID) " +
                        "WHERE CHANNELS.GUILD_ID = ? AND CHANNEL_SETTINGS.FILTERED = 1",
                data.getGuild().getId());

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
        Messages.sendMessage(data.getChannel(), DynamicEmbeds.dynamicEmbed(
                Arrays.asList(
                        "Channels currently being monitored:",
                        monitoredString,
                        "Channels currently being filtered:",
                        filteredString
                ),
                data.getMember().getUser()
        ));
        lgr.info("Successfully executed on (" + data.getGuild().getName() + "/" + data.getGuild().getId() + ").");
    }

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
}