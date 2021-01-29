package thermostat.commands.other;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.commands.Command;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.embeds.Embeds;
import thermostat.util.entities.CommandData;
import thermostat.util.enumeration.CommandType;
import thermostat.util.enumeration.EmbedType;

import javax.annotation.Nonnull;

/**
 * Sends an Invite embed when command is called.
 */
public class InviteCommand implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(InviteCommand.class);
    private final CommandData data;

    public InviteCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull String prefix) {
        this.data = new CommandData(data, prefix);
        checkPermissionsAndQueue(this);
    }

    /**
     * Command form: th!invite
     */
    @Override
    public void run() {
        ResponseDispatcher.commandSucceeded(this, Embeds.getEmbed(EmbedType.INVITE_SERVER, data));
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
    public CommandData getData() {
        return data;
    }
}
