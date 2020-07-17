package Thermostat.ThermoFunctions.Commands;

import Thermostat.Embeds;
import Thermostat.ThermoFunctions.Messages;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * <h1>Invite Command</h1>
 * <p>
 * Class that manages the th!invite command. Sends
 * an Invite embed when command is called.
 */
public class Invite extends ListenerAdapter
{
    public void onGuildMessageReceived(GuildMessageReceivedEvent ev) {
        ArrayList<String> args = new ArrayList<>(Arrays.asList(ev.getMessage().getContentRaw().split("\\s+")));

        if (
                args.get(0).equalsIgnoreCase(Thermostat.thermostat.prefix + "invite") ||
                        args.get(0).equalsIgnoreCase(Thermostat.thermostat.prefix + "server")
        ) {
            // checks if member sending request is a bot
            if (ev.getMember().getUser().isBot()) {
                return;
            }

            Messages.sendMessage(ev.getChannel(), Embeds.inviteServer(ev.getAuthor().getAsTag()));
        }
    }
}
