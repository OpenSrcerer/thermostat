package thermostat.thermoFunctions.commands.requestFactories.monitoring;

import net.dv8tion.jda.api.entities.*;
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
import thermostat.thermoFunctions.commands.CommandData;
import thermostat.thermoFunctions.commands.requestFactories.Command;
import thermostat.thermoFunctions.entities.RequestType;
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
public class MonitorCommand implements Command {

    private static final Logger lgr = LoggerFactory.getLogger(MonitorCommand.class);

    private final CommandData data;

    public MonitorCommand(CommandData data) {
        this.data = data;

        checkPermissionsAndExecute(RequestType.MONITOR, data.member(), data.channel(), lgr);
    }

    /**
     * Command form: th!monitor <true/false> [channel(s)/category(ies)]
     * @return
     */
    @Override
    public MessageEmbed execute() {
        if (data.arguments().isEmpty()) {
            Messages.sendMessage(data.channel(), HelpEmbeds.helpMonitor(data.prefix()));
            return;
        } else if (data.arguments().size() >= 2) {
            if (data.arguments().get(1).equalsIgnoreCase("all")) {
                data.arguments().subList(0, 1).clear();
                unMonitorAll();
                return;
            }
        }

        int monitor = Functions.convertToBooleanInteger(data.arguments().get(0));
        String message1, message2;
        data.arguments().remove(0);

        StringBuilder nonValid,
                noText,
                complete = new StringBuilder(),
                monitored = new StringBuilder();

        // #1 - Retrieve target channels
        {
            List<?> results = parseChannelArgument(data.channel(), data.arguments());

            nonValid = (StringBuilder) results.get(0);
            noText = (StringBuilder) results.get(1);
            // Suppressing is okay because type for
            // results.get(3) is always ArrayList<String>
            //noinspection unchecked
            data.replaceArguments((ArrayList<String>) results.get(2));
        }

        // #2 - Monitor target channels
        for (String arg : data.arguments()) {
            if (DataSource.queryInt("SELECT MONITORED FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = ?", arg) == monitor) {
                monitored.append("<#").append(arg).append("> ");
            } else {
                Create.Monitor(data.guild().getId(), arg, monitor);
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
        Messages.sendMessage(data.channel(), DynamicEmbeds.dynamicEmbed(
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
                data.member().getUser()
        ));
        lgr.info("Successfully executed on (" + data.guild().getName() + "/" + data.guild().getId() + ").");
    }

    private void unMonitorAll() {
        // add reaction & start message listener
        Consumer<Message> consumer = message -> {
            try {
                Messages.addReaction(message, "☑");
                MonitoredMessage unMonitorAllMessage = new MonitoredMessage(
                        message.getId(),
                        data.member().getId(),
                        MenuType.UNMONITORALL
                );
                unMonitorAllMessage.resetDestructionTimer(data.channel());
                // adds the object to the list
                monitoredMessages.add(unMonitorAllMessage);
            } catch (PermissionException ignored) {
            }
        };

        Messages.sendMessage(data.channel(), GenericEmbeds.promptEmbed(data.member().getUser().getAsTag(), data.member().getUser().getAvatarUrl()), consumer);
    }

    @Override
    public CommandData getData() {
        return data;
    }
}
