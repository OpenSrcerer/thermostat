package thermostat.thermoFunctions.commands.informational;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.mySQL.DataSource;
import thermostat.preparedStatements.DynamicEmbeds;
import thermostat.thermoFunctions.Messages;
import thermostat.thermoFunctions.commands.CommandEvent;
import thermostat.thermoFunctions.entities.CommandType;

import java.util.Arrays;
import java.util.List;

/**
 * Retrieves all the currently monitored channels
 * and sends them as a single embed in the channel
 * where the command was called.
 */
public class GetMonitor implements CommandEvent {

    private static final Logger lgr = LoggerFactory.getLogger(GetMonitor.class);

    private final Guild eventGuild;
    private final TextChannel eventChannel;
    private final Member eventMember;

    public GetMonitor(Guild eg, TextChannel tc, Member em) {
        eventGuild = eg;
        eventChannel = tc;
        eventMember = em;

        checkPermissionsAndExecute(CommandType.GETMONITOR, eventMember, eventChannel, lgr);
    }

    /**
     * Command form: th!getmonitor
     */
    @Override
    public void execute() {
        String monitoredString = "None.", filteredString = "None.";

        // #1 - Grab monitored and filtered channels from the DB
        List<String> monitoredList = DataSource.queryStringArray("SELECT CHANNELS.CHANNEL_ID FROM CHANNELS " +
                        "JOIN CHANNEL_SETTINGS ON (CHANNELS.CHANNEL_ID = CHANNEL_SETTINGS.CHANNEL_ID) " +
                        "WHERE CHANNELS.GUILD_ID = ? AND CHANNEL_SETTINGS.MONITORED = 1",
                eventGuild.getId());

        List<String> filteredList = DataSource.queryStringArray("SELECT CHANNELS.CHANNEL_ID FROM CHANNELS " +
                        "JOIN CHANNEL_SETTINGS ON (CHANNELS.CHANNEL_ID = CHANNEL_SETTINGS.CHANNEL_ID) " +
                        "WHERE CHANNELS.GUILD_ID = ? AND CHANNEL_SETTINGS.FILTERED = 1",
                eventGuild.getId());

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
        Messages.sendMessage(eventChannel, DynamicEmbeds.dynamicEmbed(
                Arrays.asList(
                        "Channels currently being monitored:",
                        monitoredString,
                        "Channels currently being filtered:",
                        filteredString
                ),
                eventMember.getUser()
        ));
        lgr.info("Successfully executed on (" + eventGuild.getName() + "/" + eventGuild.getId() + ").");
    }

    private String getEmbedString(List<String> list) {

        StringBuilder string = new StringBuilder();

        // iterate through retrieved array, adding
        // every monitored/filtered guild to the ending embed
        for (String it : list) {
            TextChannel filteredChannel = eventGuild.getTextChannelById(it);

            if (filteredChannel != null)
                string.append("<#").append(filteredChannel.getId()).append("> ");
            else
                string.append(it).append(" ");
        }

        return string.toString();
    }
}