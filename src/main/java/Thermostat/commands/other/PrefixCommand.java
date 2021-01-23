package thermostat.commands.other;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.commands.Command;
import thermostat.commands.CommandTrigger;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.mySQL.DataSource;
import thermostat.Embeds.ErrorEmbeds;
import thermostat.Embeds.GenericEmbeds;
import thermostat.util.Constants;
import thermostat.util.MiscellaneousFunctions;
import thermostat.util.enumeration.CommandType;

import javax.annotation.Nonnull;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static thermostat.util.ArgumentParser.hasArguments;
import static thermostat.util.ArgumentParser.parseArguments;

@SuppressWarnings("ConstantConditions")
public class PrefixCommand implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(PrefixCommand.class);

    private GuildMessageReceivedEvent data;
    private Map<String, List<String>> parameters;
    private final String prefix;
    private final long commandId;

    public PrefixCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) {
        this.commandId = MiscellaneousFunctions.getCommandId();
        this.prefix = prefix;

        try {
            this.parameters = parseArguments(arguments);
        } catch (Exception ex) {
            ResponseDispatcher.commandFailed(this, ErrorEmbeds.inputError(ex.getLocalizedMessage(), this.commandId), ex);
            return;
        }

        if (validateEvent(data)) {
            this.data = data;
        } else {
            ResponseDispatcher.commandFailed(this, ErrorEmbeds.error("Event was not valid. Please try again."), "Event had a null member.");
            return;
        }

        checkPermissionsAndQueue(this);
    }

    /**
     * Command form: th!prefix <prefix>
     */
    @Override
    public void run() {
        List<String> prefixParameters = parameters.get("p");
        List<String> resetSwitch = parameters.get("-reset");

        try {
            prefixAction(prefixParameters, resetSwitch);
        } catch (SQLException ex) {
            ResponseDispatcher.commandFailed(this,
                    ErrorEmbeds.error(ex.getLocalizedMessage(), "Please try again.", this.commandId),
                    "SQL Error thrown while issuing prefix change."
            );
        }
    }

    /**
     * Code to run when the command is called.
     * @throws SQLException If some error went wrong with the DB conn.
     */
    private void prefixAction(final List<String> prefixParameters, final List<String> resetSwitch) throws SQLException {
        // --reset switch
        if (resetSwitch != null) {
            DataSource.execute(conn -> {
                PreparedStatement statement = conn.prepareStatement("UPDATE GUILDS SET GUILD_PREFIX = NULL WHERE GUILD_ID = ?;");
                statement.setString(1, data.getGuild().getId());
                return null;
            });
            CommandTrigger.updateEntry(data.getGuild().getId(), Constants.DEFAULT_PREFIX);
            ResponseDispatcher.commandSucceeded(this, GenericEmbeds.resetPrefix());
            return;
        }

        // -p switch
        if (hasArguments(prefixParameters)) {
            String newPrefix = prefixParameters.get(0);

            if (Pattern.matches("[!-~]*", newPrefix) && newPrefix.length() <= 5 && !newPrefix.equalsIgnoreCase(prefix)) {
                DataSource.execute(conn -> {
                    PreparedStatement statement = conn.prepareStatement("UPDATE GUILDS SET GUILD_PREFIX = ? WHERE GUILD_ID = ?;");
                    statement.setString(1, newPrefix);
                    statement.setString(2, data.getGuild().getId());
                    return null;
                });
                CommandTrigger.updateEntry(data.getGuild().getId(), newPrefix);
                ResponseDispatcher.commandSucceeded(
                        this, GenericEmbeds.setPrefix(
                                data.getMember().getUser().getAsTag(),
                                data.getMember().getUser().getAvatarUrl(),
                                newPrefix
                        )
                );
            } else if (newPrefix.equalsIgnoreCase(prefix)) {
                ResponseDispatcher.commandFailed(this,
                        GenericEmbeds.samePrefix(prefix),
                        "User inserted the same prefix."
                );
            } else {
                ResponseDispatcher.commandFailed(this,
                        ErrorEmbeds.incorrectPrefix(this.getId()),
                        "User inserted an incorrect prefix."
                );
            }
        } else {
            ResponseDispatcher.commandSucceeded(
                    this, GenericEmbeds.getPrefix(
                            data.getMember().getUser().getAsTag(),
                            data.getMember().getUser().getAvatarUrl(),
                            prefix, data.getGuild().getName()
                    )
            );
        }
    }

    @Override
    public GuildMessageReceivedEvent getEvent() {
        return data;
    }

    @Override
    public CommandType getType() {
        return CommandType.PREFIX;
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
