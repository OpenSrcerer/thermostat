package thermostat.thermoFunctions.commands.other;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import thermostat.Embeds;
import thermostat.thermoFunctions.Messages;

import javax.annotation.Nonnull;

/**
 * Sends an Invite embed when command is called.
 */
public class Invite {
    public static void execute(@Nonnull TextChannel eventChannel, @Nonnull Member eventMember) {
        Messages.sendMessage(eventChannel, Embeds.inviteServer(eventMember.getUser().getAsTag(), eventMember.getUser().getAvatarUrl()));
    }
}
