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
 * Sends an Invite embed when command is called.
 */
public class Invite implements CommandEvent {

    private static final Logger lgr = LoggerFactory.getLogger(Invite.class);

    private final TextChannel eventChannel;
    private final Member eventMember;

    public Invite(@Nonnull TextChannel tc, @Nonnull Member em) {
        eventChannel = tc;
        eventMember = em;

        checkPermissionsAndExecute(CommandType.INVITE, eventMember, eventChannel, lgr);
    }

    /**
     * Command form: th!invite
     */
    @Override
    public void execute() {
        Messages.sendMessage(eventChannel, GenericEmbeds.inviteServer(eventMember.getUser().getAsTag(), eventMember.getUser().getAvatarUrl()));
        lgr.info("Successfully executed on (" + eventChannel.getGuild().getName() + "/" + eventChannel.getGuild().getId() + ").");
    }
}
