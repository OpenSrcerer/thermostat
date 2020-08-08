package Thermostat.ThermoFunctions.Commands;

import Thermostat.Embeds;
import Thermostat.MySQL.DataSource;
import Thermostat.ThermoFunctions.Messages;
import Thermostat.thermostat;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Class that manages the th!invite command. Sends
 * an Invite embed when command is called.
 */
public class Invite extends ListenerAdapter
{
    public void onGuildMessageReceived(GuildMessageReceivedEvent ev) {
        // gets guild prefix from database. if it doesn't have one, use default
        String prefix = DataSource.queryString("SELECT GUILD_PREFIX FROM GUILDS WHERE GUILD_ID = " + ev.getGuild().getId());
        if (prefix == null) { prefix = thermostat.prefix; }

        ArrayList<String> args = new ArrayList<>(Arrays.asList(ev.getMessage().getContentRaw().split("\\s+")));

        if (
                args.get(0).equalsIgnoreCase(prefix + "invite") ||
                args.get(0).equalsIgnoreCase(prefix + "server")
        ) {
            // checks if member sending request is a bot
            if (ev.getMember().getUser().isBot()) {
                return;
            }

            Messages.sendMessage(ev.getChannel(), Embeds.inviteServer(ev.getAuthor().getAsTag()));
        }
    }
}
