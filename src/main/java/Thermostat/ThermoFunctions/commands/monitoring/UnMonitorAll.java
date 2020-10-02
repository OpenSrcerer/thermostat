package thermostat.thermoFunctions.commands.monitoring;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;
import thermostat.preparedStatements.ErrorEmbeds;
import thermostat.preparedStatements.GenericEmbeds;
import thermostat.thermoFunctions.Messages;
import thermostat.thermoFunctions.entities.MenuType;
import thermostat.thermoFunctions.entities.MonitoredMessage;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

import static thermostat.thermoFunctions.entities.MonitoredMessage.monitoredMessages;

/**
 * Removes all channels from monitoring.
 */
public class UnMonitorAll {
    public static void execute(@Nonnull Guild eventGuild, @Nonnull TextChannel eventChannel, @Nonnull Member eventMember) {
        if (!eventMember.hasPermission(Permission.MANAGE_CHANNEL)) {
            Messages.sendMessage(eventChannel, ErrorEmbeds.specifyChannels());
            return;
        }

        if (!eventGuild.getSelfMember().hasPermission(eventChannel, Permission.MESSAGE_HISTORY)) {
            Messages.sendMessage(eventChannel, GenericEmbeds.insufficientReact("READ_MESSAGE_HISTORY"));
            return;
        } else if (!eventGuild.getSelfMember().hasPermission(eventChannel, Permission.MESSAGE_ADD_REACTION)) {
            Messages.sendMessage(eventChannel, GenericEmbeds.insufficientReact());
            return;
        }

        // add reaction & start message listener
        Consumer<Message> consumer = message -> {
            try {
                Messages.addReaction(message, "☑");
                MonitoredMessage unMonitorAllMessage = new MonitoredMessage(
                        message.getId(),
                        eventMember.getId(),
                        MenuType.UNMONITORALL
                );
                unMonitorAllMessage.resetDestructionTimer(eventChannel);
                // adds the object to the list
                monitoredMessages.add(unMonitorAllMessage);
            } catch (PermissionException ignored) {
            }
        };
        Messages.sendMessage(eventChannel, GenericEmbeds.promptEmbed(eventMember.getUser().getAsTag(), eventMember.getUser().getAvatarUrl()), consumer);
    }
}
