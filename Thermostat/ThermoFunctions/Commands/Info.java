package Thermostat.ThermoFunctions.Commands;

import Thermostat.Embeds;
import Thermostat.ThermoFunctions.Messages;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * <h1>Monitor Command</h1>
 * <p>
 * Class that manages the th!info command. Sends
 * an Info embed when th!info is called.
 */
public class Info extends ListenerAdapter
{
    public void onGuildMessageReceived(GuildMessageReceivedEvent ev) {
        ArrayList<String> args = new ArrayList<>(Arrays.asList(ev.getMessage().getContentRaw().split("\\s+")));

        if (
                args.get(0).equalsIgnoreCase(Thermostat.thermostat.prefix + "info") ||
                        args.get(0).equalsIgnoreCase(Thermostat.thermostat.prefix + "i") ||
                        args.get(0).equalsIgnoreCase(Thermostat.thermostat.prefix + "help") ||
                        args.get(0).equalsIgnoreCase(Thermostat.thermostat.prefix + "h")
        ) {
            // checks if member sending request is a bot
            if (ev.getMember().getUser().isBot()) {
                return;
            }

            Messages.sendMessage(ev.getChannel(), Embeds.getInfo());
        }
    }
}
