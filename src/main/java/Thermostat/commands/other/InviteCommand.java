package thermostat.commands.other;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.preparedStatements.GenericEmbeds;
import thermostat.util.Functions;
import thermostat.commands.Command;
import thermostat.util.enumeration.CommandType;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Sends an Invite embed when command is called.
 */
@SuppressWarnings("ConstantConditions")
public class InviteCommand implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(InviteCommand.class);

    private final GuildMessageReceivedEvent data;
    private final List<String> arguments;
    private final String prefix;
    private final long commandId;

    public InviteCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) {
        this.data = data;
        this.arguments = arguments;
        this.prefix = prefix;
        this.commandId = Functions.getCommandId();

        if (validateEvent(data)) {
            checkPermissionsAndQueue(this);
        }
    }

    /**
     * Command form: th!invite
     */
    @Override
    public void run() {
        ResponseDispatcher.commandSucceeded(this, GenericEmbeds.inviteServer(data.getMember().getUser().getAsTag(), data.getMember().getUser().getAvatarUrl()));
    }

    @Override
    public GuildMessageReceivedEvent getEvent() {
        return data;
    }

    @Override
    public CommandType getType() {
        return CommandType.INVITE;
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
