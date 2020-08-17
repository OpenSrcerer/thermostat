package thermostat.thermoFunctions.commands.other;

import thermostat.Embeds;
import thermostat.thermoFunctions.Messages;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.annotation.Nonnull;

/**
 * Sends an Invite embed when command is called.
 */
public class Invite
{
    public static void execute(@Nonnull TextChannel eventChannel, @Nonnull Member eventMember) {
        Messages.sendMessage(eventChannel, Embeds.inviteServer(eventMember.getUser().getAsTag()));
    }
}
