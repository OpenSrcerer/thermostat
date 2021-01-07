package thermostat.commands.moderation;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.Functions;
import thermostat.commands.Command;
import thermostat.enumeration.CommandType;

import javax.annotation.Nonnull;
import java.util.List;

public class BanCommand implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(BanCommand.class);

    private final GuildMessageReceivedEvent data;
    private List<String> arguments;
    private final String prefix;
    private final long commandId;

    public BanCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) {
        this.data = data;
        this.arguments = arguments;
        this.prefix = prefix;
        this.commandId = Functions.getCommandId();

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
        return null;
    }

    @Override
    public Logger getLogger() {
        return null;
    }

    @Override
    public long getId() {
        return 0;
    }
}