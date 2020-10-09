package thermostat.thermoFunctions.commands.other;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.preparedStatements.ErrorEmbeds;
import thermostat.preparedStatements.GenericEmbeds;
import thermostat.preparedStatements.HelpEmbeds;
import thermostat.thermoFunctions.Messages;
import thermostat.thermoFunctions.commands.CommandEvent;
import thermostat.thermoFunctions.entities.CommandType;
import thermostat.thermoFunctions.entities.MenuType;
import thermostat.thermoFunctions.entities.MonitoredMessage;
import thermostat.thermostat;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;

import static thermostat.thermoFunctions.entities.MonitoredMessage.monitoredMessages;

/**
 * Class that manages the th!info command. Sends
 * an Info embed when th!info is called.
 */
public class Info implements CommandEvent {

    private static final Logger lgr = LoggerFactory.getLogger(Info.class);

    private final TextChannel eventChannel;
    private final Member eventMember;
    private final String eventArgument, eventPrefix;

    private EnumSet<Permission> missingThermostatPerms, missingMemberPerms;

    public Info(@Nonnull TextChannel tc, @Nonnull Member em, String px, List<String> args) {
        eventChannel = tc;
        eventMember = em;
        if (!args.isEmpty()) {
            eventArgument = args.get(0);
        } else {
            eventArgument = "";
        }
        eventPrefix = px;

        checkPermissions();
        if (missingMemberPerms.isEmpty() && missingThermostatPerms.isEmpty()) {
            execute();
        } else {
            lgr.info("Missing permissions on (" + eventChannel.getGuild().getName() + "/" + eventChannel.getGuild().getId() + "):" +
                    " [" + missingThermostatPerms.toString() + "] [" + missingMemberPerms.toString() + "]");
            Messages.sendMessage(eventChannel, ErrorEmbeds.errPermission(missingThermostatPerms, missingMemberPerms));
        }
    }

    @Override
    public void checkPermissions() {
        eventChannel.getGuild()
                .retrieveMember(thermostat.thermo.getSelfUser())
                .map(thermostat -> {
                    missingThermostatPerms = findMissingPermissions(CommandType.INFO.getThermoPerms(), thermostat.getPermissions());
                    return thermostat;
                })
                .queue();

        missingMemberPerms = findMissingPermissions(CommandType.INFO.getMemberPerms(), eventMember.getPermissions());
    }

    /**
     * Command form: th!info/help [cmdname]
     */
    @Override
    public void execute() {
        Consumer<Message> consumer = message -> {
            try {
                Messages.addReactions(message, Arrays.asList("🌡", "🔧", "ℹ", "❌"));
                // create the information message object
                // to be added to the monitored message
                // ArrayList
                MonitoredMessage infoMessage = new MonitoredMessage(
                        message.getId(),
                        eventMember.getId(),
                        MenuType.SELECTION
                );
                infoMessage.resetDestructionTimer(eventChannel);
                // adds the object to the list
                monitoredMessages.add(infoMessage);
            } catch (PermissionException ignored) {
            }
        };

        if (!eventArgument.isEmpty()) {
            if (eventArgument.equalsIgnoreCase(CommandType.CHART.getAlias1())) {
                Messages.sendMessage(eventChannel, HelpEmbeds.expandedHelpChart(eventPrefix));
            } else if (eventArgument.equalsIgnoreCase(CommandType.GETMONITOR.getAlias1())) {
                Messages.sendMessage(eventChannel, HelpEmbeds.expandedHelpGetMonitor(eventPrefix));
            } else if (eventArgument.equalsIgnoreCase(CommandType.SETTINGS.getAlias1())) {
                Messages.sendMessage(eventChannel, HelpEmbeds.expandedHelpSettings(eventPrefix));
            } else if (eventArgument.equalsIgnoreCase(CommandType.MONITOR.getAlias1())) {
                Messages.sendMessage(eventChannel, HelpEmbeds.expandedHelpMonitor(eventPrefix));
            } else if (eventArgument.equalsIgnoreCase(CommandType.SENSITIVITY.getAlias1())) {
                Messages.sendMessage(eventChannel, HelpEmbeds.expandedHelpSensitivity(eventPrefix));
            } else if (eventArgument.equalsIgnoreCase(CommandType.SETBOUNDS.getAlias1())) {
                Messages.sendMessage(eventChannel, HelpEmbeds.expandedHelpSetBounds(eventPrefix));
            } else if (eventArgument.equalsIgnoreCase(CommandType.INVITE.getAlias1())) {
                Messages.sendMessage(eventChannel, HelpEmbeds.expandedHelpInvite(eventPrefix));
            } else if (eventArgument.equalsIgnoreCase(CommandType.PREFIX.getAlias1())) {
                Messages.sendMessage(eventChannel, HelpEmbeds.expandedHelpPrefix(eventPrefix));
            } else if (eventArgument.equalsIgnoreCase(CommandType.VOTE.getAlias1())) {
                Messages.sendMessage(eventChannel, HelpEmbeds.expandedHelpVote(eventPrefix));
            } else if (eventArgument.equalsIgnoreCase(CommandType.FILTER.getAlias1())) {
                Messages.sendMessage(eventChannel, HelpEmbeds.expandedHelpFilter(eventPrefix));
            } else if (eventArgument.equalsIgnoreCase(CommandType.INFO.getAlias1())) {
                Messages.sendMessage(eventChannel, HelpEmbeds.expandedHelpInfo(eventPrefix));
            }
            return;
        }

        Messages.sendMessage(eventChannel, GenericEmbeds.getInfoSelection(), consumer);
        lgr.info("Successfully executed on (" + eventChannel.getGuild().getName() + "/" + eventChannel.getGuild().getId() + ").");
    }
}
