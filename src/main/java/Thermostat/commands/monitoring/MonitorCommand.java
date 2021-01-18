package thermostat.commands.monitoring;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.Messages;
import thermostat.commands.Command;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.mySQL.Create;
import thermostat.mySQL.DataSource;
import thermostat.Embeds.DynamicEmbeds;
import thermostat.Embeds.ErrorEmbeds;
import thermostat.Embeds.GenericEmbeds;
import thermostat.util.Functions;
import thermostat.util.entities.ReactionMenu;
import thermostat.util.enumeration.CommandType;
import thermostat.util.enumeration.MenuType;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static thermostat.util.ArgumentParser.parseArguments;

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
     * Command form: th!monitor
     * Switches:
     * --on
     * --off
     * --all
     * -c
     */
    @Override
    public void run() {
        final Map<String, List<String>> parameters;

        try {
            parameters = parseArguments(arguments);
        } catch (Exception ex) {
            ResponseDispatcher.commandFailed(this, ErrorEmbeds.inputError(ex.getLocalizedMessage(), this.commandId), ex);
            return;
        }

        List<String> channels = parameters.get("c");
        final List<String> onSwitch = parameters.get("-on");
        final List<String> offSwitch = parameters.get("-off");
        final List<String> allSwitch = parameters.get("-all");

        if (offSwitch == null && onSwitch == null) {
            ResponseDispatcher.commandFailed(this,
                    ErrorEmbeds.inputError("You must insert at least an --on/--off switch.", this.commandId),
                    "User did not provide any on/off arguments.");
        } else if (allSwitch != null) {
            unMonitorAll();
        } else {
            // Initialization necessary for compiler
            int monitor = 0;
            if (onSwitch != null) {
                monitor = 1;
            } else if (offSwitch != null) {
                monitor = 0;
            }

            final StringBuilder nonValid,
                    noText,
                    complete = new StringBuilder(),
                    monitored = new StringBuilder();

            // #1 - Retrieve target channels
            {
                Arguments results = parseChannelArgument(data.getChannel(), channels);

                nonValid = results.nonValid;
                noText = results.noText;
                channels = results.newArguments;
            }

            // #2 - Monitor target channels
            for (final String channel : channels) {
                if (DataSource.queryInt("SELECT MONITORED FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = ?", channel) == monitor) {
                    monitored.append("<#").append(channel).append("> ");
                } else {
                    Create.Monitor(data.getGuild().getId(), channel, monitor);
                    complete.append("<#").append(channel).append("> ");
                }
            }

            // switch message depending on user action
            final String message1, message2;
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
                ResponseDispatcher.commandSucceeded(this, GenericEmbeds.allRemoved(
                        getEvent().getAuthor().getId(), getEvent().getAuthor().getAvatarUrl()
                ));
            } catch (Exception ex) {
                ResponseDispatcher.commandFailed(this, ErrorEmbeds.error(ex.getLocalizedMessage(), this.getId()), ex);
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
