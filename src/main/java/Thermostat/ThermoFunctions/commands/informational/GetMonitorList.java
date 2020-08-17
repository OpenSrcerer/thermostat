package thermostat.thermoFunctions.commands.informational;

import thermostat.Embeds;
import thermostat.mySQL.Create;
import thermostat.mySQL.DataSource;
import thermostat.thermoFunctions.Messages;
import thermostat.thermostat;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.List;

/**
 * Retrieves all the currently monitored channels
 * and sends them as a single embed in the channel
 * where the command was called.
 */
public class GetMonitorList {
    // dynamic embed that gets posted
    private static final EmbedBuilder embed = new EmbedBuilder();

    public static void execute(@Nonnull Guild eventGuild, @Nonnull TextChannel eventChannel, @Nonnull Member eventMember) {
        String embedString = "";

        // checks if event member has permission
        if (!eventMember.hasPermission(Permission.MANAGE_CHANNEL)) {
            Messages.sendMessage(eventChannel, Embeds.specifyChannels());
            return;
        }

        try {
            // Adds the guild to the database if it's not in it!
            if (!DataSource.checkDatabaseForData("SELECT * FROM GUILDS WHERE GUILD_ID = " + eventGuild.getId()))
                Create.Guild(eventGuild.getId());

            List<String> guildList = DataSource.query("SELECT CHANNELS.CHANNEL_ID FROM CHANNELS " +
                    "JOIN CHANNEL_SETTINGS ON (CHANNELS.CHANNEL_ID = CHANNEL_SETTINGS.CHANNEL_ID) " +
                    "WHERE CHANNELS.GUILD_ID = " + eventGuild.getId() + " AND CHANNEL_SETTINGS.MONITORED = 1");

            if (guildList.isEmpty())
            {
                embed.addField("Channels currently being monitored:", "None.", false);
            }
            else
            {
                // iterate through retrieved array, adding
                // every monitored guild to the ending embed
                for (String it : guildList)
                {
                    TextChannel monitoredChannel = eventGuild.getTextChannelById(it);

                    if (monitoredChannel != null)
                        embedString = embedString.concat("<#" + monitoredChannel.getId() + "> ");
                    else
                        embedString = embedString.concat(it + " ");
                }
            }

        } catch (Exception ex) {
            Messages.sendMessage(eventChannel, Embeds.fatalError());
            Logger lgr = LoggerFactory.getLogger(DataSource.class);
            lgr.error(ex.getMessage(), ex);
        }

        if (!embedString.isEmpty())
            embed.addField("Channels currently being monitored:", embedString, true);

        embed.setColor(0x00aeff);
        embed.setTimestamp(Instant.now());
        embed.setFooter("Requested by " + eventMember.getUser().getAsTag(), thermostat.thermo.getSelfUser().getAvatarUrl());
        Messages.sendMessage(eventChannel, embed);

        embed.clear();
    }
}