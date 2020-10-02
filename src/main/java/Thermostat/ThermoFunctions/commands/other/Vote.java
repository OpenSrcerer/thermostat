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
 * Class that manages the th!vote command. Sends
 * a Vote embed when th!vote is called.
 */
public class Vote implements CommandEvent {
    public Vote(@Nonnull TextChannel eventChannel, @Nonnull Member eventMember) {

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
        Messages.sendMessage(eventChannel, GenericEmbeds.getVote(eventMember.getUser().getAsTag(), eventMember.getUser().getAvatarUrl()));
    }
}
