package thermostat.thermoFunctions.commands.other;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;
import thermostat.preparedStatements.GenericEmbeds;
import thermostat.thermoFunctions.Messages;
import thermostat.thermoFunctions.commands.CommandEvent;

import javax.annotation.Nonnull;
import java.util.EnumSet;

/**
 * Sends an Invite embed when command is called.
 */
public class Invite implements CommandEvent {
    public Invite(@Nonnull TextChannel eventChannel, @Nonnull Member eventMember) {

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
        Messages.sendMessage(eventChannel, GenericEmbeds.inviteServer(eventMember.getUser().getAsTag(), eventMember.getUser().getAvatarUrl()));
    }
}
