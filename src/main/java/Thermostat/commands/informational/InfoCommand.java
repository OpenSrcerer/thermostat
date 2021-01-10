package thermostat.commands.informational;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.util.Functions;
import thermostat.Messages;
import thermostat.commands.Command;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.util.entities.ReactionMenu;
import thermostat.util.enumeration.CommandType;
import thermostat.util.enumeration.MenuType;
import thermostat.preparedStatements.ErrorEmbeds;
import thermostat.preparedStatements.GenericEmbeds;
import thermostat.preparedStatements.HelpEmbeds;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Class that manages the th!info command. Sends
 * an Info embed when th!info is called.
 */
@SuppressWarnings("ConstantConditions")
public class InfoCommand implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(InfoCommand.class);

    private final GuildMessageReceivedEvent data;
    private final String argument;
    private final String prefix;
    private final long commandId;

    public InfoCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) {
        this.data = data;
        this.prefix = prefix;
        this.commandId = Functions.getCommandId();
        
        if (!arguments.isEmpty()) {
            argument = arguments.get(0);
        } else {
            argument = "";
        }

        if (validateEvent(data)) {
            checkPermissionsAndQueue(this);
        }
    }

    /**
     * Command form: th!info/help [cmdname]
     */
    @Override
    public void run() {
        if (!argument.isEmpty()) {
            if (argument.equalsIgnoreCase(CommandType.CHART.getAlias1())) {
                Messages.sendMessage(data.getChannel(), HelpEmbeds.expandedHelpChart(prefix));
            } else if (argument.equalsIgnoreCase(CommandType.GETMONITOR.getAlias1())) {
                Messages.sendMessage(data.getChannel(), HelpEmbeds.expandedHelpGetMonitor(prefix));
            } else if (argument.equalsIgnoreCase(CommandType.SETTINGS.getAlias1())) {
                Messages.sendMessage(data.getChannel(), HelpEmbeds.expandedHelpSettings(prefix));
            } else if (argument.equalsIgnoreCase(CommandType.MONITOR.getAlias1())) {
                Messages.sendMessage(data.getChannel(), HelpEmbeds.expandedHelpMonitor(prefix));
            } else if (argument.equalsIgnoreCase(CommandType.SENSITIVITY.getAlias1())) {
                Messages.sendMessage(data.getChannel(), HelpEmbeds.expandedHelpSensitivity(prefix));
            } else if (argument.equalsIgnoreCase(CommandType.SETBOUNDS.getAlias1())) {
                Messages.sendMessage(data.getChannel(), HelpEmbeds.expandedHelpSetBounds(prefix));
            } else if (argument.equalsIgnoreCase(CommandType.INVITE.getAlias1())) {
                Messages.sendMessage(data.getChannel(), HelpEmbeds.expandedHelpInvite(prefix));
            } else if (argument.equalsIgnoreCase(CommandType.PREFIX.getAlias1())) {
                Messages.sendMessage(data.getChannel(), HelpEmbeds.expandedHelpPrefix(prefix));
            } else if (argument.equalsIgnoreCase(CommandType.VOTE.getAlias1())) {
                Messages.sendMessage(data.getChannel(), HelpEmbeds.expandedHelpVote(prefix));
            } else if (argument.equalsIgnoreCase(CommandType.FILTER.getAlias1())) {
                Messages.sendMessage(data.getChannel(), HelpEmbeds.expandedHelpFilter(prefix));
            } else if (argument.equalsIgnoreCase(CommandType.INFO.getAlias1())) {
                Messages.sendMessage(data.getChannel(), HelpEmbeds.expandedHelpInfo(prefix));
            }
            return;
        }

        Consumer<Message> consumer = message -> {
            try {
                Messages.addReactions(message, Arrays.asList("🌡", "🔧", "ℹ", "❌"));
                new ReactionMenu(
                        MenuType.SELECTION, data.getMember().getId(),
                        message.getId(), data.getChannel()
                );
                ResponseDispatcher.commandSucceeded(this, null);
            } catch (Exception ex) {
                ResponseDispatcher.commandFailed(this, ErrorEmbeds.error(ex.getCause().toString(), this.getId()), ex);
            }
        };

        Messages.sendMessage(data.getChannel(), GenericEmbeds.getInfoSelection(), consumer);
    }

    @Override
    public GuildMessageReceivedEvent getEvent() {
        return data;
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
    public long getId() {
        return commandId;
    }
}
