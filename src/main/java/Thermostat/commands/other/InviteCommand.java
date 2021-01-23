package thermostat.commands.other;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.Embeds.GenericEmbeds;
import thermostat.commands.Command;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.util.MiscellaneousFunctions;
import thermostat.util.enumeration.CommandType;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

/**
 * Sends an Invite embed when command is called.
 */
@SuppressWarnings("ConstantConditions")
public class InviteCommand implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(InviteCommand.class);

    private GuildMessageReceivedEvent data = null;
    private Map<String, List<String>> parameters = null;
    private final String prefix;
    private final long commandId;

    public InviteCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) {
        this.commandId = MiscellaneousFunctions.getCommandId();
        this.prefix = prefix;
        this.parameters = null;
        this.data = null;

        checkPermissionsAndQueue(this);
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
