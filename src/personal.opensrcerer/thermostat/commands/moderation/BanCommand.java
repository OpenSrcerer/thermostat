package thermostat.commands.moderation;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.commands.Command;
import thermostat.dispatchers.CommandDispatcher;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.embeds.Embeds;
import thermostat.util.ArgumentParser;
import thermostat.util.entities.CommandContext;
import thermostat.util.enumeration.CommandType;
import thermostat.util.enumeration.EmbedType;

import javax.annotation.Nonnull;
import java.util.Calendar;
import java.util.List;

import static thermostat.util.ArgumentParser.hasArguments;

public class BanCommand implements Command {

    /**
     * Logger for this class.
     */
    private static final Logger lgr = LoggerFactory.getLogger(BanCommand.class);

    /**
     * Context for this command.
     */
    private final CommandContext data;

    public BanCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) {
        this.data = new CommandContext(data, arguments, prefix);

        if (this.data.parameters == null) {
            ResponseDispatcher.commandFailed(
                    this,
                    Embeds.getEmbed(EmbedType.ERR, this.data),
                    "Bad arguments.");
            return;
        }

        CommandDispatcher.checkPermissionsAndQueue(this);
    }

    /**
     * Command form:
     */
    @Override
    public void run() {
        List<String> users = data.parameters.get("u");
        List<String> time = data.parameters.get("t");

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

    private static void banMembers(final List<String> users, final Calendar calendar) {
        if (calendar == null) {
            return;
        }
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
    public CommandContext getData() {
        return data;
    }
}
