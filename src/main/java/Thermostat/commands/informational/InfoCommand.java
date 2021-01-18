package thermostat.commands.informational;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.Messages;
import thermostat.commands.Command;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.Embeds.ErrorEmbeds;
import thermostat.Embeds.GenericEmbeds;
import thermostat.Embeds.HelpEmbeds;
import thermostat.util.Functions;
import thermostat.util.entities.ReactionMenu;
import thermostat.util.enumeration.CommandType;
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

    /* TBA
    private static final HashMap<String, EmbedBuilder> embeds = new HashMap<>() {{
        put(CommandType.CHART.getAlias1(), HelpEmbeds.expandedHelpChart())
    }};*/

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
            EmbedBuilder builder;

            if (argument.equalsIgnoreCase(CommandType.CHART.getAlias1())) {
                builder = HelpEmbeds.expandedHelpChart(prefix);
            } else if (argument.equalsIgnoreCase(CommandType.GETMONITOR.getAlias1())) {
                builder = HelpEmbeds.expandedHelpGetMonitor(prefix);
            } else if (argument.equalsIgnoreCase(CommandType.SETTINGS.getAlias1())) {
                builder = HelpEmbeds.expandedHelpSettings(prefix);
            } else if (argument.equalsIgnoreCase(CommandType.MONITOR.getAlias1())) {
                builder = HelpEmbeds.expandedHelpMonitor(prefix);
            } else if (argument.equalsIgnoreCase(CommandType.SENSITIVITY.getAlias1())) {
                builder = HelpEmbeds.expandedHelpSensitivity(prefix);
            } else if (argument.equalsIgnoreCase(CommandType.SETBOUNDS.getAlias1())) {
                builder = HelpEmbeds.expandedHelpSetBounds(prefix);
            } else if (argument.equalsIgnoreCase(CommandType.INVITE.getAlias1())) {
                builder = HelpEmbeds.expandedHelpInvite(prefix);
            } else if (argument.equalsIgnoreCase(CommandType.PREFIX.getAlias1())) {
                builder = HelpEmbeds.expandedHelpPrefix(prefix);
            } else if (argument.equalsIgnoreCase(CommandType.VOTE.getAlias1())) {
                builder = HelpEmbeds.expandedHelpVote(prefix);
            } else if (argument.equalsIgnoreCase(CommandType.FILTER.getAlias1())) {
                builder = HelpEmbeds.expandedHelpFilter(prefix);
            } else if (argument.equalsIgnoreCase(CommandType.INFO.getAlias1())) {
                builder = HelpEmbeds.expandedHelpInfo(prefix);
            } else {
                ResponseDispatcher.commandFailed(
                        this,
                        ErrorEmbeds.inputError(
                                "Invalid command was given. Please insert a valid command name as an argument.",
                                this.commandId),
                        "User failed to provide a proper command for help."
                );
                return;
            }

            ResponseDispatcher.commandSucceeded(this, builder);
        } else {
            sendGenericInfoMenu();
        }
    }

    private void sendGenericInfoMenu() {
        Messages.sendMessage(data.getChannel(), GenericEmbeds.getInfoSelection(),
        message -> {
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
        });
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
