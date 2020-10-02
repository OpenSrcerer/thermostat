package thermostat.thermoFunctions.commands.other;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;
import org.jetbrains.annotations.NotNull;
import thermostat.preparedStatements.GenericEmbeds;
import thermostat.thermoFunctions.Messages;
import thermostat.thermoFunctions.commands.CommandEvent;
import thermostat.thermoFunctions.entities.MenuType;
import thermostat.thermoFunctions.entities.MonitoredMessage;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.function.Consumer;

import static thermostat.thermoFunctions.entities.MonitoredMessage.monitoredMessages;

/**
 * Class that manages the th!info command. Sends
 * an Info embed when th!info is called.
 */
public class Info implements CommandEvent {
    public Info(@Nonnull TextChannel eventChannel, @Nonnull Member eventMember) {

    }

    @Override
    public void checkPermissions() {

    }

    @NotNull
    @Override
    public EnumSet<Permission> findMissingPermissions(EnumSet<Permission> permissionsToSeek, EnumSet<Permission> givenPermissions) {
        return null;
    }

    @Override
    public void execute() {
        Consumer<Message> consumer = message -> {
            try {
                Messages.addReactions(message, Arrays.asList("🌡", "ℹ", "🔧", "❌"));
                // create the information message object
                // to be added to the monitored message
                // ArrayList
                MonitoredMessage infoMessage = new MonitoredMessage(
                        message.getId(),
                        eventMember.getId(),
                        MenuType.SELECTION
                );
                infoMessage.resetDestructionTimer(eventChannel);
                // adds the object to the list
                monitoredMessages.add(infoMessage);
            } catch (PermissionException ignored) {
            }
        };
        Messages.sendMessage(eventChannel, GenericEmbeds.getInfoSelection(), consumer);
    }
}
