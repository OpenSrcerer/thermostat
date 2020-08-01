package Thermostat.ThermoFunctions.Commands;

import Thermostat.Embeds;
import Thermostat.MySQL.DataSource;
import Thermostat.ThermoFunctions.Commands.Objects.MenuType;
import Thermostat.ThermoFunctions.Commands.Objects.MonitoredMessage;
import Thermostat.ThermoFunctions.Messages;

import static Thermostat.ThermoFunctions.Commands.Objects.MonitoredMessage.monitoredMessages;

import Thermostat.thermostat;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * <h1>Info</h1>
 * <p>
 * Class that manages the th!info command. Sends
 * an Info embed when th!info is called.
 */
public class Info extends ListenerAdapter
{
    public void onGuildMessageReceived(GuildMessageReceivedEvent ev) {
        // gets guild prefix from database. if it doesn't have one, use default
        String prefix = DataSource.queryString("SELECT GUILD_PREFIX FROM GUILDS WHERE GUILD_ID = " + ev.getGuild().getId());
        if (prefix == null) { prefix = thermostat.prefix; }

        ArrayList<String> args = new ArrayList<>(Arrays.asList(ev.getMessage().getContentRaw().split("\\s+")));

        if (
                args.get(0).equalsIgnoreCase(prefix + "info") ||
                        args.get(0).equalsIgnoreCase(prefix + "i") ||
                        args.get(0).equalsIgnoreCase(prefix + "help") ||
                        args.get(0).equalsIgnoreCase(prefix + "h")
        ) {
            // checks if member sending request is a bot
            if (ev.getMember().getUser().isBot()) {
                return;
            }

            Consumer<Message> consumer = message -> {
                try {
                    Messages.addReactions(message, Arrays.asList("🌡", "ℹ", "❌"));
                    // create the information message object
                    // to be added to the monitored message
                    // ArrayList
                    MonitoredMessage infoMessage = new MonitoredMessage(
                            message.getId(),
                            ev.getAuthor().getId(),
                            MenuType.SELECTION
                    );
                    infoMessage.resetDestructionTimer(ev.getChannel());
                    // adds the object to the list
                    monitoredMessages.add(infoMessage);
                } catch (PermissionException ignored) {}
            };
            Messages.sendMessage(ev.getChannel(), Embeds.getInfoSelection(), consumer);
        }
    }
}
