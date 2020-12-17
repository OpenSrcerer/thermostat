package thermostat.thermoFunctions.commands.other;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.preparedStatements.GenericEmbeds;
import thermostat.thermoFunctions.Messages;
import thermostat.thermoFunctions.commands.Command;
import thermostat.thermoFunctions.entities.CommandType;

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

    public InviteCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) {
        this.data = data;
        this.arguments = arguments;
        this.prefix = prefix;

        if (validateEvent(data)) {
            checkPermissionsAndQueue(this);
        }
    }

    /**
     * Command form: th!invite
     */
    @Override
    public void run() {
        Messages.sendMessage(data.getChannel(), GenericEmbeds.inviteServer(data.getMember().getUser().getAsTag(), data.getMember().getUser().getAvatarUrl()));
        lgr.info("Successfully executed on (" + data.getChannel().getGuild().getName() + "/" + data.getChannel().getGuild().getId() + ").");
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
}
