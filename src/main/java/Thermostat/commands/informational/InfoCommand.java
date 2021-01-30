package thermostat.commands.informational;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.Messages;
import thermostat.commands.Command;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.embeds.Embeds;
import thermostat.util.entities.CommandData;
import thermostat.util.entities.ReactionMenu;
import thermostat.util.enumeration.CommandType;
import thermostat.util.enumeration.EmbedType;
import thermostat.util.enumeration.MenuType;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

/**
 * Class that manages the th!info command. Sends
 * an Info embed when th!info is called.
 */
@SuppressWarnings("ConstantConditions")
public class InfoCommand implements Command {
    /**
     * Logger for this class.
     */
    private static final Logger lgr = LoggerFactory.getLogger(InfoCommand.class);

    private final CommandData data;
    private final EmbedType type;

    /* TBA
    private static final HashMap<String, EmbedBuilder> embeds = new HashMap<>() {{
        put(CommandType.CHART.getAlias1(), HelpEmbeds.expandedHelpChart())
    }};*/

    public InfoCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) {
        this.data = new CommandData(data, prefix);

        if (!arguments.isEmpty()) {
            type = matchArgumentToEmbed(arguments.get(0));
        } else {
            type = null;
        }

        checkPermissionsAndQueue(this);
    }

    /**
     * Command form: th!info/help [Command Name]
     */
    @Override
    public void run() {
        if (type == null) {
            sendGenericInfoMenu();
        } else {
            ResponseDispatcher.commandSucceeded(this, Embeds.getEmbed(type, data));
        }
    }

    private void sendGenericInfoMenu() {
        Messages.sendMessage(data.event.getChannel(), Embeds.getEmbed(EmbedType.SELECTION, data),
        message -> {
            try {
                Messages.addReactions(data.event.getChannel(), message.getId(), Arrays.asList("🌡", "🔧", "ℹ", "❌"));
                new ReactionMenu(
                        MenuType.SELECTION, data.event.getMember().getId(),
                        message.getId(), data.event.getChannel()
                );
                ResponseDispatcher.commandSucceeded(this, null);
            } catch (Exception ex) {
                ResponseDispatcher.commandFailed(this, Embeds.getEmbed(EmbedType.ERR, data, ex.getMessage()), ex);
            }
        });
    }

    @Nonnull
    private static EmbedType matchArgumentToEmbed(@Nonnull final String argument) {
        if (argument.equalsIgnoreCase(CommandType.CHART.getAlias1())) {
            return EmbedType.HELP_CHART;
        } else if (argument.equalsIgnoreCase(CommandType.GETMONITOR.getAlias1())) {
            return EmbedType.HELP_GETMONITOR;
        } else if (argument.equalsIgnoreCase(CommandType.SETTINGS.getAlias1())) {
            return EmbedType.HELP_SETTINGS;
        } else if (argument.equalsIgnoreCase(CommandType.MONITOR.getAlias1())) {
            return EmbedType.HELP_MONITOR;
        } else if (argument.equalsIgnoreCase(CommandType.SENSITIVITY.getAlias1())) {
            return EmbedType.HELP_SENSITIVITY;
        } else if (argument.equalsIgnoreCase(CommandType.SETBOUNDS.getAlias1())) {
            return EmbedType.HELP_SETBOUNDS;
        } else if (argument.equalsIgnoreCase(CommandType.INVITE.getAlias1())) {
            return EmbedType.HELP_INVITE;
        } else if (argument.equalsIgnoreCase(CommandType.PREFIX.getAlias1())) {
            return EmbedType.HELP_PREFIX;
        } else if (argument.equalsIgnoreCase(CommandType.VOTE.getAlias1())) {
            return EmbedType.HELP_VOTE;
        } else if (argument.equalsIgnoreCase(CommandType.FILTER.getAlias1())) {
            return EmbedType.HELP_FILTER;
        } else if (argument.equalsIgnoreCase(CommandType.INFO.getAlias1())) {
            return EmbedType.HELP_INFO;
        } else {
            return null;
        }
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
