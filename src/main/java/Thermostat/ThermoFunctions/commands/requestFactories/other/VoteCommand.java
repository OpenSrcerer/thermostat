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
 * Class that manages the th!vote command. Sends
 * a Vote embed when th!vote is called.
 */
public class VoteCommand implements Command {

    private static final Logger lgr = LoggerFactory.getLogger(InfoCommand.class);

    private final CommandData data;

    public VoteCommand(@Nonnull CommandData data) {
        this.data = data;

        checkPermissionsAndExecute(RequestType.VOTE, data.member(), data.channel(), lgr);
    }

    /**
     * Command form: th!vote
     * @return
     */
    @Override
    public MessageEmbed execute() {
        Messages.sendMessage(data.channel(), GenericEmbeds.getVote(data.member().getUser().getAsTag(), data.member().getUser().getAvatarUrl()));
    }

    @Override
    public CommandData getData() {
        return data;
    }
}
