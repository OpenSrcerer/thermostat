package thermostat.commands.other;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.commands.informational.InfoCommand;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.preparedStatements.GenericEmbeds;
import thermostat.Functions;
import thermostat.commands.Command;
import thermostat.enumeration.CommandType;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Class that manages the th!vote command. Sends
 * a Vote embed when th!vote is called.
 */
@SuppressWarnings("ConstantConditions")
public class VoteCommand implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(InfoCommand.class);

    private final GuildMessageReceivedEvent data;
    private final List<String> arguments;
    private final String prefix;
    private final long commandId;

    public VoteCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) {
        this.data = data;
        this.arguments = arguments;
        this.prefix = prefix;
        this.commandId = Functions.getCommandId();

        if (validateEvent(data)) {
            checkPermissionsAndQueue(this);
        }
    }

    /**
     * Command form: th!vote
     */
    @Override
    public void run() {
        ResponseDispatcher.commandSucceeded(this, GenericEmbeds.getVote(data.getMember().getUser().getAsTag(), data.getMember().getUser().getAvatarUrl()));
    }

    @Override
    public GuildMessageReceivedEvent getEvent() {
        return data;
    }

    @Override
    public CommandType getType() {
        return CommandType.VOTE;
    }

    @Override
    public Logger getLogger() {
        return lgr;
    }

    @Override
    public long getId() {
        return commandId;
    }
}
