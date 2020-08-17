package thermostat.thermoFunctions.commands.other;

import thermostat.Embeds;
import thermostat.thermoFunctions.Messages;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.annotation.Nonnull;

/**
 * Class that manages the th!vote command. Sends
 * a Vote embed when th!vote is called.
 */
public class Vote
{
    public static void execute(@Nonnull TextChannel eventChannel, @Nonnull Member eventMember)
    {
        Messages.sendMessage(eventChannel, Embeds.getVote(eventMember.getUser().getAsTag()));
    }
}
