package thermostat.commands.internal;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.commands.Command;
import thermostat.commands.informational.InfoCommand;
import thermostat.dispatchers.CommandDispatcher;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.embeds.Embeds;
import thermostat.util.entities.CommandData;
import thermostat.util.enumeration.CommandType;
import thermostat.util.enumeration.EmbedType;

import javax.annotation.Nonnull;

/**
 * Sends a Guide embed whenever @Thermostat is called.
 */
public class SendGuide implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(InfoCommand.class);
    private final CommandData data;

    public SendGuide(@Nonnull GuildMessageReceivedEvent data, @Nonnull String prefix) {
        this.data = new CommandData(data, prefix);
        CommandDispatcher.checkPermissionsAndQueue(this);
    }

    @Override
    public void run() {
        ResponseDispatcher.commandSucceeded(this, Embeds.getEmbed(EmbedType.GUIDE, data));
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
    public CommandData getData() {
        return data;
    }
}

