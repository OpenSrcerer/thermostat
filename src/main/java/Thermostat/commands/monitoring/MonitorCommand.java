package thermostat.commands.monitoring;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.Functions;
import thermostat.Messages;
import thermostat.commands.Command;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.entities.ReactionMenu;
import thermostat.enumeration.CommandType;
import thermostat.enumeration.MenuType;
import thermostat.mySQL.Create;
import thermostat.mySQL.DataSource;
import thermostat.preparedStatements.DynamicEmbeds;
import thermostat.preparedStatements.ErrorEmbeds;
import thermostat.preparedStatements.GenericEmbeds;
import thermostat.preparedStatements.HelpEmbeds;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Adds channels to the database provided in
 * db.properties, upon user running the
 * command.
 */
@SuppressWarnings("ConstantConditions")
public class MonitorCommand implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(MonitorCommand.class);

    private final GuildMessageReceivedEvent data;
    private List<String> arguments;
    private final String prefix;
    private final long commandId;

    public MonitorCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) {
        this.data = data;
        this.arguments = arguments;
        this.prefix = prefix;
        this.commandId = Functions.getCommandId();

        if (validateEvent(data)) {
            checkPermissionsAndQueue(this);
        }
    }

    /**
     * Command form: th!monitor <true/false> [channel(s)/category(ies)]
     */
    @Override
    public void run() {
        if (arguments.isEmpty()) {
            ResponseDispatcher.commandFailed(this,
                    HelpEmbeds.helpMonitor(prefix),
                    "User did not provide any arguments.");
            return;
        } else if (arguments.size() >= 2) {
            if (arguments.get(1).equalsIgnoreCase("all")) {
                arguments.subList(0, 1).clear();
                unMonitorAll();
                return;
            }
        }

        int monitor = Functions.convertToBooleanInteger(arguments.get(0));
        if (monitor == -1) {
            ResponseDispatcher.commandFailed(
                    this, ErrorEmbeds.inputError("Please provide a correct action. Example: `" + prefix + "monitor on`", this.commandId),
                    "User did not provide a correct action."
            );
            return;
        }

        String message1, message2;
        arguments.remove(0);

        StringBuilder nonValid,
                noText,
                complete = new StringBuilder(),
                monitored = new StringBuilder();

        // #1 - Retrieve target channels
        {
            List<?> results = parseChannelArgument(data.getChannel(), arguments);

            nonValid = (StringBuilder) results.get(0);
            noText = (StringBuilder) results.get(1);
            //noinspection unchecked
            arguments = ((ArrayList<String>) results.get(2));
        }

        // #2 - Monitor target channels
        for (String arg : arguments) {
            if (DataSource.queryInt("SELECT MONITORED FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = ?", arg) == monitor) {
                monitored.append("<#").append(arg).append("> ");
            } else {
                Create.Monitor(data.getGuild().getId(), arg, monitor);
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

        // #6 - Send the results embed to manager
        ResponseDispatcher.commandSucceeded(this,
                DynamicEmbeds.dynamicEmbed(
                        Arrays.asList(
                                message1,
                                complete.toString(),
                                message2,
                                monitored.toString(),
                                "Channels that were not valid or found:",
                                nonValid.toString(),
                                "Categories with no Text Channels:",
                                noText.toString()
                        ), data.getMember().getUser(), commandId
                )
        );
    }

    private void unMonitorAll() {
        // add reaction & start message listener
        Consumer<Message> consumer = message -> {
            try {
                Messages.addReaction(message, "☑");
                new ReactionMenu(
                        MenuType.UNMONITORALL, data.getMember().getId(),
                        message.getId(), data.getChannel()
                );
                ResponseDispatcher.commandSucceeded(this, null);
            } catch (Exception ex) {
                ResponseDispatcher.commandFailed(this, ErrorEmbeds.error(ex.getCause().toString(), this.getId()), ex);
            }
        };

        Messages.sendMessage(
                data.getChannel(),
                GenericEmbeds.promptEmbed(data.getMember().getUser().getAsTag(), data.getMember().getUser().getAvatarUrl()),
                consumer
        );
    }

    @Override
    public GuildMessageReceivedEvent getEvent() {
        return data;
    }

    @Override
    public CommandType getType() {
        return CommandType.MONITOR;
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
