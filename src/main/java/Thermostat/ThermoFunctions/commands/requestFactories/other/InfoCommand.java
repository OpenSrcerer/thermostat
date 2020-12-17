package thermostat.thermoFunctions.commands.requestFactories.other;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.exceptions.PermissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.preparedStatements.GenericEmbeds;
import thermostat.preparedStatements.HelpEmbeds;
import thermostat.thermoFunctions.Messages;
import thermostat.thermoFunctions.commands.CommandData;
import thermostat.thermoFunctions.commands.requestFactories.Command;
import thermostat.thermoFunctions.entities.RequestType;
import thermostat.thermoFunctions.entities.MenuType;
import thermostat.thermoFunctions.entities.MonitoredMessage;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.function.Consumer;

import static thermostat.thermoFunctions.entities.MonitoredMessage.monitoredMessages;

/**
 * Class that manages the th!info command. Sends
 * an Info embed when th!info is called.
 */
public class InfoCommand implements Command {

    private static final Logger lgr = LoggerFactory.getLogger(InfoCommand.class);

    private final CommandData data;
    private final String eventArgument;

    public InfoCommand(@Nonnull CommandData data) {
        this.data = data;
        
        if (!data.arguments().isEmpty()) {
            eventArgument = data.arguments().get(0);
        } else {
            eventArgument = "";
        }

        checkPermissionsAndExecute(RequestType.INFO, data.member(), data.channel(), lgr);
    }

    /**
     * Command form: th!info/help [cmdname]
     * @return
     */
    @Override
    public MessageEmbed execute() {
        Consumer<Message> consumer = message -> {
            try {
                Messages.addReactions(message, Arrays.asList("🌡", "🔧", "ℹ", "❌"));
                // create the information message object
                // to be added to the monitored message
                // ArrayList
                MonitoredMessage infoMessage = new MonitoredMessage(
                        message.getId(),
                        data.member().getId(),
                        MenuType.SELECTION
                );
                infoMessage.resetDestructionTimer(data.channel());
                // adds the object to the list
                monitoredMessages.add(infoMessage);
            } catch (PermissionException ignored) {
            }
        };

        if (!eventArgument.isEmpty()) {
            if (eventArgument.equalsIgnoreCase(RequestType.CHART.getAlias1())) {
                Messages.sendMessage(data.channel(), HelpEmbeds.expandedHelpChart(data.prefix()));
            } else if (eventArgument.equalsIgnoreCase(RequestType.GETMONITOR.getAlias1())) {
                Messages.sendMessage(data.channel(), HelpEmbeds.expandedHelpGetMonitor(data.prefix()));
            } else if (eventArgument.equalsIgnoreCase(RequestType.SETTINGS.getAlias1())) {
                Messages.sendMessage(data.channel(), HelpEmbeds.expandedHelpSettings(data.prefix()));
            } else if (eventArgument.equalsIgnoreCase(RequestType.MONITOR.getAlias1())) {
                Messages.sendMessage(data.channel(), HelpEmbeds.expandedHelpMonitor(data.prefix()));
            } else if (eventArgument.equalsIgnoreCase(RequestType.SENSITIVITY.getAlias1())) {
                Messages.sendMessage(data.channel(), HelpEmbeds.expandedHelpSensitivity(data.prefix()));
            } else if (eventArgument.equalsIgnoreCase(RequestType.SETBOUNDS.getAlias1())) {
                Messages.sendMessage(data.channel(), HelpEmbeds.expandedHelpSetBounds(data.prefix()));
            } else if (eventArgument.equalsIgnoreCase(RequestType.INVITE.getAlias1())) {
                Messages.sendMessage(data.channel(), HelpEmbeds.expandedHelpInvite(data.prefix()));
            } else if (eventArgument.equalsIgnoreCase(RequestType.PREFIX.getAlias1())) {
                Messages.sendMessage(data.channel(), HelpEmbeds.expandedHelpPrefix(data.prefix()));
            } else if (eventArgument.equalsIgnoreCase(RequestType.VOTE.getAlias1())) {
                Messages.sendMessage(data.channel(), HelpEmbeds.expandedHelpVote(data.prefix()));
            } else if (eventArgument.equalsIgnoreCase(RequestType.FILTER.getAlias1())) {
                Messages.sendMessage(data.channel(), HelpEmbeds.expandedHelpFilter(data.prefix()));
            } else if (eventArgument.equalsIgnoreCase(RequestType.INFO.getAlias1())) {
                Messages.sendMessage(data.channel(), HelpEmbeds.expandedHelpInfo(data.prefix()));
            }
            return;
        }

        Messages.sendMessage(data.channel(), GenericEmbeds.getInfoSelection(), consumer);
        lgr.info("Successfully executed on (" + data.channel().getGuild().getName() + "/" + data.channel().getGuild().getId() + ").");
    }

    @Override
    public CommandData getData() {
        return data;
    }
}
