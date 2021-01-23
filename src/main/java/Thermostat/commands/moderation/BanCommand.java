package thermostat.commands.moderation;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.util.ArgumentParser;
import thermostat.util.MiscellaneousFunctions;
import thermostat.commands.Command;
import thermostat.util.enumeration.CommandType;

import javax.annotation.Nonnull;
import java.text.ParseException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static thermostat.util.ArgumentParser.hasArguments;
import static thermostat.util.ArgumentParser.parseArguments;

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
        this.commandId = MiscellaneousFunctions.getCommandId();

        if (validateEvent(data)) {
            checkPermissionsAndQueue(this);
        }
    }

    /**
     * Command form:
     */
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
        List<String> time = parameters.get("t");

        if (hasArguments(users)) {
            Calendar calendar = null;
            if (hasArguments(time)) {
                calendar = ArgumentParser.parseTime(String.join("", time));
            }

            banMembers(users, calendar);
        } else {
            // cmdfailed (no users)
        }
    }

    private static void banMembers(List<String> users, Calendar calendar) {
        if (calendar == null) {

        }
    }


    @Override
    public GuildMessageReceivedEvent getEvent() {
        return data;
    }

    @Override
    public CommandType getType() {
        return CommandType.BAN;
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
