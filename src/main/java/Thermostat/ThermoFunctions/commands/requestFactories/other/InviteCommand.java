package thermostat.thermoFunctions.commands.requestFactories.other;

import net.dv8tion.jda.api.entities.MessageEmbed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.preparedStatements.GenericEmbeds;
import thermostat.thermoFunctions.Messages;
import thermostat.thermoFunctions.commands.CommandData;
import thermostat.thermoFunctions.commands.requestFactories.Command;
import thermostat.thermoFunctions.entities.RequestType;

import javax.annotation.Nonnull;

/**
 * Sends an Invite embed when command is called.
 */
public class InviteCommand implements Command {

    private static final Logger lgr = LoggerFactory.getLogger(InviteCommand.class);

    private final CommandData data;

    public InviteCommand(@Nonnull CommandData data) {
        this.data = data;

        checkPermissionsAndExecute(RequestType.INVITE, data.member(), data.channel(), lgr);
    }

    /**
     * Command form: th!invite
     * @return
     */
    @Override
    public MessageEmbed execute() {
        Messages.sendMessage(data.channel(), GenericEmbeds.inviteServer(data.member().getUser().getAsTag(), data.member().getUser().getAvatarUrl()));
        lgr.info("Successfully executed on (" + data.channel().getGuild().getName() + "/" + data.channel().getGuild().getId() + ").");
    }

    @Override
    public CommandData getData() {
        return data;
    }
}
