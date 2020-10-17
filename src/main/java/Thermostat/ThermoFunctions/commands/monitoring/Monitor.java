package thermostat.thermoFunctions.commands.monitoring;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.mySQL.Create;
import thermostat.mySQL.DataSource;
import thermostat.preparedStatements.DynamicEmbeds;
import thermostat.preparedStatements.GenericEmbeds;
import thermostat.preparedStatements.HelpEmbeds;
import thermostat.thermoFunctions.Functions;
import thermostat.thermoFunctions.Messages;
import thermostat.thermoFunctions.commands.CommandEvent;
import thermostat.thermoFunctions.entities.CommandType;
import thermostat.thermoFunctions.entities.MenuType;
import thermostat.thermoFunctions.entities.MonitoredMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static thermostat.thermoFunctions.entities.MonitoredMessage.monitoredMessages;

/**
 * Adds channels to the database provided in
 * db.properties, upon user running the
 * command.
 */
public class Monitor implements CommandEvent {

    private static final Logger lgr = LoggerFactory.getLogger(Monitor.class);

    private final Guild eventGuild;
    private final TextChannel eventChannel;
    private final Member eventMember;
    private final String eventPrefix;
    private ArrayList<String> args;

    public Monitor(Guild eg, TextChannel tc, Member em, String px, ArrayList<String> ag) {
        eventGuild = eg;
        eventChannel = tc;
        eventMember = em;
        eventPrefix = px;
        args = ag;

        checkPermissionsAndExecute(CommandType.MONITOR, eventMember, eventChannel, lgr);
    }

    /**
     * Command form: th!monitor <true/false> [channel(s)/category(ies)]
     */
    @Override
    public void execute() {
        if (args.isEmpty()) {
            Messages.sendMessage(eventChannel, HelpEmbeds.helpMonitor(eventPrefix));
            return;
        } else if (args.size() >= 2) {
            if (args.get(1).equalsIgnoreCase("all")) {
                args.subList(0, 1).clear();
                unMonitorAll();
                return;
            }
        }

        int monitor = Functions.convertToBooleanInteger(args.get(0));
        String message1, message2;
        args.remove(0);

        StringBuilder nonValid,
                noText,
                complete = new StringBuilder(),
                monitored = new StringBuilder();

        // #1 - Retrieve target channels
        {
            List<?> results = parseChannelArgument(eventChannel, args);

            nonValid = (StringBuilder) results.get(0);
            noText = (StringBuilder) results.get(1);
            // Suppressing is okay because type for
            // results.get(3) is always ArrayList<String>
            //noinspection unchecked
            args = (ArrayList<String>) results.get(2);
        }

        // #2 - Monitor target channels
        for (String arg : args) {
            if (DataSource.queryInt("SELECT MONITORED FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = ?", arg) == monitor) {
                monitored.append("<#").append(arg).append("> ");
            } else {
                Create.Monitor(eventGuild.getId(), arg, monitor);
                complete.append("<#").append(arg).append("> ");
            }
        }

        // switch message depending on user action
        if (monitor == 1) {
            message1 = "Successfully monitored:";
            message2 = "Channels that were already being monitored:";
        } else {
            message1 = "Successfully unmonitored:";
            message2 = "Channels that were already not being monitored:";
        }

        // #6 - Send the results embed to user
        Messages.sendMessage(eventChannel, DynamicEmbeds.dynamicEmbed(
                Arrays.asList(
                        message1,
                        complete.toString(),
                        message2,
                        monitored.toString(),
                        "Channels that were not valid or found:",
                        nonValid.toString(),
                        "Categories with no Text Channels:",
                        noText.toString()
                ),
                eventMember.getUser()
        ));
        lgr.info("Successfully executed on (" + eventGuild.getName() + "/" + eventGuild.getId() + ").");
    }

    private void unMonitorAll() {
        // add reaction & start message listener
        Consumer<Message> consumer = message -> {
            try {
                Messages.addReaction(message, "☑");
                MonitoredMessage unMonitorAllMessage = new MonitoredMessage(
                        message.getId(),
                        eventMember.getId(),
                        MenuType.UNMONITORALL
                );
                unMonitorAllMessage.resetDestructionTimer(eventChannel);
                // adds the object to the list
                monitoredMessages.add(unMonitorAllMessage);
            } catch (PermissionException ignored) {
            }
        };

        Messages.sendMessage(eventChannel, GenericEmbeds.promptEmbed(eventMember.getUser().getAsTag(), eventMember.getUser().getAvatarUrl()), consumer);
    }
}
