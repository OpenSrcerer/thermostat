package Thermostat.ThermoFunctions.Commands;

import Thermostat.Embeds;
import Thermostat.MySQL.Create;
import Thermostat.MySQL.DataSource;
import Thermostat.ThermoFunctions.Messages;
import Thermostat.thermostat;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class for the th!getmonitor command call.
 * Retrieves all the currently monitored channels
 * and sends them as a single embed in the channel
 * where the command was called.
 */
public class GetMonitorList extends ListenerAdapter {
    // dynamic embed that gets posted
    private static EmbedBuilder embed = new EmbedBuilder();

    /**
     * JDA Event Listener that grabs any message sent in a Guild.
     * @param ev The called event.
     */
    public void onGuildMessageReceived(GuildMessageReceivedEvent ev) {
        // gets guild prefix from database. if it doesn't have one, use default
        String prefix = DataSource.queryString("SELECT GUILD_PREFIX FROM GUILDS WHERE GUILD_ID = " + ev.getGuild().getId());
        if (prefix == null) { prefix = thermostat.prefix; }

        // gets given arguments and passes them to a list
        ArrayList<String> args = new ArrayList<>(Arrays.asList(ev.getMessage().getContentRaw().split("\\s+")));
        String embedString = "";

        if (
                args.get(0).equalsIgnoreCase(prefix + "getmonitor") ||
                        args.get(0).equalsIgnoreCase(prefix + "getmon") ||
                        args.get(0).equalsIgnoreCase(prefix + "gm")
        ) {
            // checks if member sending request is a bot
            if (ev.getMember().getUser().isBot()) {
                return;
            }

            // checks if event member has permission
            if (!ev.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
                Messages.sendMessage(ev.getChannel(), Embeds.userNoPermission());
                return;
            }

            try {
                // Adds the guild to the database if it's not in it!
                if (!DataSource.checkDatabaseForData("SELECT * FROM GUILDS WHERE GUILD_ID = " + ev.getGuild().getId()))
                    Create.Guild(ev.getGuild().getId());

                List<String> guildList = DataSource.query("SELECT CHANNELS.CHANNEL_ID FROM CHANNELS " +
                        "JOIN CHANNEL_SETTINGS ON (CHANNELS.CHANNEL_ID = CHANNEL_SETTINGS.CHANNEL_ID) " +
                        "WHERE CHANNELS.GUILD_ID = " + ev.getGuild().getId() + " AND CHANNEL_SETTINGS.MONITORED = 1");

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
                        embedString = embedString.concat("<#" + ev.getGuild().getTextChannelById(it).getId() + "> ");
                    }
                }

            } catch (Exception ex) {
                Messages.sendMessage(ev.getChannel(), Embeds.fatalError());
                Logger lgr = LoggerFactory.getLogger(DataSource.class);
                lgr.error(ex.getMessage(), ex);
            }

            if (!embedString.isEmpty())
                embed.addField("Channels currently being monitored:", embedString, true);

            embed.setColor(0x00aeff);
            embed.setTimestamp(Instant.now());
            embed.setFooter("Requested by " + ev.getAuthor().getAsTag(), thermostat.thermo.getSelfUser().getAvatarUrl());
            Messages.sendMessage(ev.getChannel(), embed);

            embed.clear();
        }
    }
}