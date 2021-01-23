package thermostat.commands.moderation;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.util.MiscellaneousFunctions;
import thermostat.commands.Command;
import thermostat.util.enumeration.CommandType;

import javax.annotation.Nonnull;
import java.util.List;

public class MuteCommand implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(MuteCommand.class);

    private final GuildMessageReceivedEvent data;
    private List<String> arguments;
    private final String prefix;
    private final long commandId;

    public MuteCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) {
        this.data = data;
        this.arguments = arguments;
        this.prefix = prefix;
        this.commandId = MiscellaneousFunctions.getCommandId();

        if (validateEvent(data)) {
            checkPermissionsAndQueue(this);
        }
    }

    @Override
    public void run() {

    }

    @Override
    public GuildMessageReceivedEvent getEvent() {
        return data;
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
    public long getId() {
        return commandId;
    }
}
