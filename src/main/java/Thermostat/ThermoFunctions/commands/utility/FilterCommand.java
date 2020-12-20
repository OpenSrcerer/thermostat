package thermostat.thermoFunctions.commands.utility;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.mySQL.Create;
import thermostat.preparedStatements.DynamicEmbeds;
import thermostat.preparedStatements.ErrorEmbeds;
import thermostat.preparedStatements.HelpEmbeds;
import thermostat.thermoFunctions.Functions;
import thermostat.thermoFunctions.commands.Command;
import thermostat.thermoFunctions.commands.monitoring.SetBoundsCommand;
import thermostat.thermoFunctions.entities.CommandType;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static thermostat.thermoFunctions.Functions.convertToBooleanInteger;

@SuppressWarnings("ConstantConditions")
public class FilterCommand implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(SetBoundsCommand.class);

    private final GuildMessageReceivedEvent data;
    private List<String> arguments;
    private final String prefix;
    private final long commandId;

    public FilterCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) {
        this.data = data;
        this.arguments = arguments;
        this.prefix = prefix;
        this.commandId = Functions.getCommandId();

        if (validateEvent(data)) {
            checkPermissionsAndQueue(this);
        }
    }

    /**
     * Command form: th!filter <true/false> [channel(s)/category(ies)]
     */
    @Override
    public void run() {
        if (arguments.isEmpty()) {
            ResponseDispatcher.commandFailed(this,
                    HelpEmbeds.helpFilter(prefix),
                    "User did not provide arguments.");
        }

        int filtered = convertToBooleanInteger(arguments.get(0));
        String message;

        arguments.remove(0);
        StringBuilder nonValid,
                noText,
                complete;

        {
            List<?> results = parseChannelArgument(data.getChannel(), arguments);

            nonValid = (StringBuilder) results.get(0);
            noText = (StringBuilder) results.get(1);
            // Suppressing is okay because type for
            // results.get(3) is always ArrayList<String>
            //noinspection unchecked
            arguments = ((ArrayList<String>) results.get(2));
        }
        // arguments now remains as a list of target channel(s).

        // individually enable filtering in every channel
        // after checking whether the channel exists in the db
        try {
            addIfNotInDb(data.getGuild().getId(), arguments);
            complete = Create.setFilter(Integer.toString(filtered), arguments);
        } catch (SQLException ex) {
            ResponseDispatcher.commandFailed(this,
                    ErrorEmbeds.error(ex.getLocalizedMessage(), Functions.getCommandId()),
                    ex);
            return;
        }

        // switch message depending on user action
        if (filtered == 1) {
            message = "Enabled filtering on:";
        } else {
            message = "Disabled filtering on:";
        }

        ResponseDispatcher.commandSucceeded(this,
                DynamicEmbeds.dynamicEmbed(
                        Arrays.asList(
                                message,
                                complete.toString(),
                                "Channels that were not valid or found:",
                                nonValid.toString(),
                                "Categories with no Text Channels:",
                                noText.toString()
                        ),
                        data.getMember().getUser()
                )
        );
    }

    @Override
    public GuildMessageReceivedEvent getEvent() {
        return data;
    }

    @Override
    public CommandType getType() {
        return CommandType.FILTER;
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
