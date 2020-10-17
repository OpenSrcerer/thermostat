package thermostat.thermoFunctions.commands.other;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.preparedStatements.GenericEmbeds;
import thermostat.thermoFunctions.Messages;
import thermostat.thermoFunctions.commands.CommandEvent;
import thermostat.thermoFunctions.entities.CommandType;

import javax.annotation.Nonnull;

/**
 * Class that manages the th!vote command. Sends
 * a Vote embed when th!vote is called.
 */
public class Vote implements CommandEvent {

    private static final Logger lgr = LoggerFactory.getLogger(Info.class);

    private final TextChannel eventChannel;
    private final Member eventMember;

    public Vote(@Nonnull TextChannel tc, @Nonnull Member em) {
        eventChannel = tc;
        eventMember = em;

        checkPermissionsAndExecute(CommandType.VOTE, eventMember, eventChannel, lgr);
    }

    /**
     * Command form: th!vote
     */
    @Override
    public void execute() {
        Messages.sendMessage(eventChannel, GenericEmbeds.getVote(eventMember.getUser().getAsTag(), eventMember.getUser().getAvatarUrl()));
    }
}
