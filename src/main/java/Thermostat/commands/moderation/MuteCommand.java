package thermostat.commands.moderation;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.commands.Command;
import thermostat.dispatchers.CommandDispatcher;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.embeds.Embeds;
import thermostat.util.entities.CommandData;
import thermostat.util.enumeration.CommandType;
import thermostat.util.enumeration.EmbedType;

import javax.annotation.Nonnull;
import java.util.List;

public class MuteCommand implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(MuteCommand.class);
    private final CommandData data;

    public MuteCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) {
        this.data = new CommandData(data, arguments, prefix);

        if (this.data.parameters == null) {
            ResponseDispatcher.commandFailed(
                    this,
                    Embeds.getEmbed(EmbedType.ERR, this.data),
                    "Bad arguments.");
            return;
        }

        CommandDispatcher.checkPermissionsAndQueue(this);
    }

    @Override
    public void run() {

    }

    @Override
    public CommandType getType() {
        return CommandType.MUTE;
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
