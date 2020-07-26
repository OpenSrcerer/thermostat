package Thermostat.ThermoFunctions.Commands;

import Thermostat.Embeds;
import Thermostat.ThermoFunctions.Commands.Objects.MenuType;
import Thermostat.ThermoFunctions.Commands.Objects.MonitoredMessage;
import Thermostat.ThermoFunctions.Messages;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;

import static Thermostat.ThermoFunctions.Commands.Objects.MonitoredMessage.monitoredMessages;

/**
 * <h1>UnMonitorAll Command</h1>
 * <p>
 * Removes all channels from monitoring.
 */
public class UnMonitorAll extends ListenerAdapter
{
    public void onGuildMessageReceived(GuildMessageReceivedEvent ev)
    {
        // gets given arguments and passes them to a list
        ArrayList<String> args = new ArrayList<>(Arrays.asList(ev.getMessage().getContentRaw().split("\\s+")));

        if (
                args.get(0).equalsIgnoreCase(Thermostat.thermostat.prefix + "unmonitorall") ||
                        args.get(0).equalsIgnoreCase(Thermostat.thermostat.prefix + "unmonall") ||
                        args.get(0).equalsIgnoreCase(Thermostat.thermostat.prefix + "uma")
        ) {
            // checks if member sending request is a bot
            if (ev.getMember().getUser().isBot()) {
                return;
            }

            // checks if event member has permission
            if (!ev.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
                Messages.sendMessage(ev.getChannel(), Embeds.specifyChannels());
                return;
            }

            if (!ev.getGuild().getSelfMember().hasPermission(ev.getChannel(), Permission.MESSAGE_HISTORY))
            {
                Messages.sendMessage(ev.getChannel(), Embeds.insufficientReact("Read Message History"));
                return;
            }

            else if (!ev.getGuild().getSelfMember().hasPermission(ev.getChannel(), Permission.MESSAGE_ADD_REACTION))
            {
                Messages.sendMessage(ev.getChannel(), Embeds.insufficientReact());
                return;
            }

            // Custom consumer in order to also add reaction
            // and start the monitoring thread
            Consumer<Message> consumer = message -> {
                try {
                    Messages.addReaction(message, "☑");
                    MonitoredMessage unmonitorallMessage = new MonitoredMessage(
                            message.getId(),
                            ev.getAuthor().getId(),
                            MenuType.UNMONITORALL
                    );
                    unmonitorallMessage.resetDestructionTimer(ev.getChannel());
                    // adds the object to the list
                    monitoredMessages.add(unmonitorallMessage);
                } catch (PermissionException ignored) {}
            };
            Messages.sendMessage(ev.getChannel(), Embeds.promptEmbed(ev.getAuthor().getAsTag()), consumer);
        }
    }
}
