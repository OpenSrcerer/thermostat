package thermostat.thermoFunctions.commands.informational;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.preparedStatements.ErrorEmbeds;
import thermostat.mySQL.DataSource;
import thermostat.thermoFunctions.Messages;
import thermostat.thermoFunctions.commands.CommandEvent;
import thermostat.thermoFunctions.entities.CommandType;
import thermostat.thermostat;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Retrieves all the currently monitored channels
 * and sends them as a single embed in the channel
 * where the command was called.
 */
public class GetMonitorList implements CommandEvent {

    private static final Logger lgr = LoggerFactory.getLogger(GetMonitorList.class);

    private final Guild eventGuild;
    private final TextChannel eventChannel;
    private final Member eventMember;
    private final String eventPrefix;

    private EnumSet<Permission> missingThermostatPerms, missingMemberPerms;

    public GetMonitorList(Guild eg, TextChannel tc, Member em, String px) {
        eventGuild = eg;
        eventChannel = tc;
        eventMember = em;
        eventPrefix = px;

        checkPermissions();
        if (missingMemberPerms.isEmpty() && missingThermostatPerms.isEmpty()) {
            execute();
        } else {
            lgr.info("Missing permissions on (" + eventGuild.getName() + "/" + eventGuild.getId() + "):" +
                    " [" + missingThermostatPerms.toString() + "] [" + missingMemberPerms.toString() + "]");
            Messages.sendMessage(eventChannel, ErrorEmbeds.errPermission(missingThermostatPerms, missingMemberPerms));
        }
    }

    @Override
    public void checkPermissions() {
        eventGuild
                .retrieveMember(thermostat.thermo.getSelfUser())
                .map(thermostat -> {
                    missingThermostatPerms = findMissingPermissions(CommandType.GETMONITORLIST.getThermoPerms(), thermostat.getPermissions());
                    return thermostat;
                })
                .queue();

        missingMemberPerms = findMissingPermissions(CommandType.GETMONITORLIST.getMemberPerms(), eventMember.getPermissions());
    }

    @Override
    public void execute() {
        String embedString = "";
        String filteredString = "";

        // checks if event member has permission
        if (!eventMember.hasPermission(Permission.MANAGE_CHANNEL)) {
            Messages.sendMessage(eventChannel, ErrorEmbeds.specifyChannels());
            return;
        }

        try {
            List<String> guildList = DataSource.queryStringArray("SELECT CHANNELS.CHANNEL_ID FROM CHANNELS " +
                            "JOIN CHANNEL_SETTINGS ON (CHANNELS.CHANNEL_ID = CHANNEL_SETTINGS.CHANNEL_ID) " +
                            "WHERE CHANNELS.GUILD_ID = ? AND CHANNEL_SETTINGS.MONITORED = 1",
                    eventGuild.getId());

            List<String> filteredList = DataSource.queryStringArray("SELECT CHANNELS.CHANNEL_ID FROM CHANNELS " +
                            "JOIN CHANNEL_SETTINGS ON (CHANNELS.CHANNEL_ID = CHANNEL_SETTINGS.CHANNEL_ID) " +
                            "WHERE CHANNELS.GUILD_ID = ? AND CHANNEL_SETTINGS.FILTERED = 1",
                    eventGuild.getId());

            if (guildList == null) {
                embed.addField("Channels currently being monitored:", "None.", false);
            } else if (guildList.isEmpty()) {
                embed.addField("Channels currently being monitored:", "None.", false);
            } else {
                // iterate through retrieved array, adding
                // every monitored guild to the ending embed
                for (String it : guildList) {
                    TextChannel monitoredChannel = eventGuild.getTextChannelById(it);

                    if (monitoredChannel != null)
                        embedString = embedString.concat("<#" + monitoredChannel.getId() + "> ");
                    else
                        embedString = embedString.concat(it + " ");
                }
            }

            if (filteredList == null || filteredList.isEmpty()) {
                embed.addField("Channels currently being filtered:", "None.", false);
            } else {
                // iterate through retrieved array, adding
                // every monitored guild to the ending embed
                for (String it : filteredList) {
                    TextChannel filteredChannel = eventGuild.getTextChannelById(it);

                    if (filteredChannel != null)
                        filteredString = filteredString.concat("<#" + filteredChannel.getId() + "> ");
                    else
                        filteredString = filteredString.concat(it + " ");
                }
            }

        } catch (Exception ex) {
            Messages.sendMessage(eventChannel, ErrorEmbeds.errFatal());
            lgr.error(ex.getMessage(), ex);
        }

        if (!embedString.isEmpty())
            embed.addField("Channels currently being monitored:", embedString, false);
        if (!filteredString.isEmpty())
            embed.addField("Channels currently being filtered:", filteredString, false);

        embed.setColor(0x00aeff);
        embed.setTimestamp(Instant.now());
        embed.setFooter("Requested by " + eventMember.getUser().getAsTag(), eventMember.getUser().getAvatarUrl());
        Messages.sendMessage(eventChannel, embed);

        embed.clear();
    }
}