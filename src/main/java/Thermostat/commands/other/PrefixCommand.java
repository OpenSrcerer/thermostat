package thermostat.commands.other;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.commands.Command;
import thermostat.commands.CommandTrigger;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.mySQL.DataSource;
import thermostat.preparedStatements.ErrorEmbeds;
import thermostat.preparedStatements.GenericEmbeds;
import thermostat.util.Constants;
import thermostat.util.Functions;
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

    private final GuildMessageReceivedEvent data;
    private final List<String> arguments;
    private final String prefix;
    private final long commandId;

    public PrefixCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) {
        this.data = data;
        this.arguments = arguments;
        this.prefix = prefix;
        this.commandId = Functions.getCommandId();

        if (validateEvent(data)) {
            checkPermissionsAndQueue(this);
        }
    }

    /**
     * Command form: th!prefix <prefix>
     */
    @Override
    public void run() {
        try {
            pxAction(data, prefix);
        } catch (SQLException ex) {
            ResponseDispatcher.commandFailed(this,
                    ErrorEmbeds.error(ex.getLocalizedMessage(), "Please try again.", this.commandId),
                    "SQL Error thrown while issuing prefix change."
            );
        }
    }

    /**
     * Code to run when the command is called.
     *
     * @param data CommandData object that stores information about the command triggerer.
     * @throws SQLException If some error went wrong with the DB conn.
     */
    private void pxAction(@Nonnull GuildMessageReceivedEvent data, @Nonnull String prefix) throws SQLException {
        final Map<String, List<String>> parameters;

        try {
            parameters = parseArguments(arguments);
        } catch (Exception ex) {
            // cmdfailed (error in arguments)
            return;
        }

        List<String> prefixParameters = parameters.get("p");
        List<String> resetSwitch = parameters.get("-reset");

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
