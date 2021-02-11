package thermostat.commands.informational;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.util.MessageHandler;
import thermostat.commands.Command;
import thermostat.dispatchers.CommandDispatcher;
import thermostat.dispatchers.MenuDispatcher;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.embeds.Embeds;
import thermostat.util.ArgumentParser;
import thermostat.util.entities.CommandData;
import thermostat.util.enumeration.CommandType;
import thermostat.util.enumeration.EmbedType;
import thermostat.util.enumeration.MenuType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/**
 * Class that manages the th!info command. Sends
 * an Info embed when th!info is called.
 */
public class InfoCommand implements Command {
    /**
     * Logger for this class.
     */
    private static final Logger lgr = LoggerFactory.getLogger(InfoCommand.class);

    /**
     * Command's Data.
     */
    private final CommandData data;

    public InfoCommand(final GuildMessageReceivedEvent data, final List<String> arguments, final String prefix) {
        this.data = new CommandData(data, arguments, prefix);
        CommandDispatcher.checkPermissionsAndQueue(this);
    }

    /**
     * Command form: th!info/help [Command Name]
     */
    @Override
    public void run() {
        if (this.data.parameters != null) {
            List<String> commandType = data.parameters.get("-type");

            if (ArgumentParser.hasArguments(commandType)) {
                EmbedType type = matchArgumentToEmbed(commandType.get(0));
                if (type != null) {
                    ResponseDispatcher.commandSucceeded(this, Embeds.getEmbed(type, data));
                    return;
                }
            }
        }

        sendGenericInfoMenu();
    }

    private void sendGenericInfoMenu() {
        MessageHandler.sendMessage(data.event.getChannel(), Embeds.getEmbed(EmbedType.SELECTION),
        message -> {
            try {
                MessageHandler.addReactions(data.event.getChannel(), message.getId(), Arrays.asList("🌡", "🔧", "ℹ", "❌"));
                MenuDispatcher.addMenu(MenuType.SELECTION, message.getId(), this);
                ResponseDispatcher.commandSucceeded(this, null);
            } catch (Exception ex) {
                ResponseDispatcher.commandFailed(this, Embeds.getEmbed(EmbedType.ERR, data, ex.getMessage()), ex);
            }
        });
    }

    @Nullable
    private static EmbedType matchArgumentToEmbed(@Nonnull final String argument) {
        for (final CommandType t : CommandType.class.getEnumConstants()) {
            if (argument.equalsIgnoreCase(t.alias1)) {
                return t.getEmbedType();
            }
        }
        return null;
    }

    @Override
    public CommandType getType() {
        return CommandType.INFO;
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
