package thermostat.commands.other;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.preparedStatements.GenericEmbeds;
import thermostat.preparedStatements.HelpEmbeds;
import thermostat.Functions;
import thermostat.Messages;
import thermostat.commands.Command;
import thermostat.enumeration.CommandType;
import thermostat.enumeration.MenuType;
import thermostat.entities.MonitoredMessage;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static thermostat.entities.MonitoredMessage.monitoredMessages;

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
        Consumer<Message> consumer = message -> {
            try {
                Messages.addReactions(message, Arrays.asList("🌡", "🔧", "ℹ", "❌"));
                // create the information message object
                // to be added to the monitored message
                // ArrayList
                MonitoredMessage infoMessage = new MonitoredMessage(
                        message.getId(),
                        data.getMember().getId(),
                        MenuType.SELECTION
                );
                infoMessage.resetDestructionTimer(data.getChannel());
                // adds the object to the list
                monitoredMessages.add(infoMessage);
            } catch (PermissionException ignored) {
            }
        };

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

        ResponseDispatcher.commandSucceeded(this,
                GenericEmbeds.getInfoSelection(),
                consumer);
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
