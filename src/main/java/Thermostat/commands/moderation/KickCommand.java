package thermostat.commands.moderation;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.Functions;
import thermostat.commands.Command;
import thermostat.enumeration.CommandType;

import javax.annotation.Nonnull;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public class KickCommand implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(KickCommand.class);

    private final GuildMessageReceivedEvent data;
    private List<String> arguments;
    private final String prefix;
    private final long commandId;

    public KickCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) {
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
        final Map<String, List<String>> parameters;

        try {
            parameters = parseArguments(arguments);
        } catch (ParseException ex) {
            // cmdfailed (error in arguments)
            return;
        }

        List<String> users = parameters.get("u");

        if (hasArguments(users)) {
            kickUsers(users);
        } else {
            // cmdfailed (no users)
        }
    }

    private static void kickUsers(@Nonnull List<String> users) {

    }

    @Override
    public GuildMessageReceivedEvent getEvent() {
        return data;
    }

    @Override
    public CommandType getType() {
        return CommandType.KICK;
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
