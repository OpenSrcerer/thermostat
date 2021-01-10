package thermostat.commands.other;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.Thermostat;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.mySQL.DataSource;
import thermostat.preparedStatements.ErrorEmbeds;
import thermostat.preparedStatements.GenericEmbeds;
import thermostat.util.Functions;
import thermostat.commands.Command;
import thermostat.commands.CommandTrigger;
import thermostat.enumeration.CommandType;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

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
        if (arguments.isEmpty()) {
            ResponseDispatcher.commandFailed(this,
                    ErrorEmbeds.inputError("No arguments provided. Please insert a valid prefix.", commandId),
                    "User did not provide arguments.");
            return;
        }

        try {
            prefixAction(data, arguments, prefix);
        } catch (SQLException ex) {
            ResponseDispatcher.commandFailed(this,
                    ErrorEmbeds.error(ex.getLocalizedMessage(),
                            Functions.getCommandId()), ex
            );
        }
    }

    /**
     * Code to run when the command is called.
     *
     * @param data CommandData object that stores information about the command triggerer.
     * @throws SQLException If some error went wrong with the DB conn.
     */
    public void prefixAction(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) throws SQLException {
        if (arguments.size() > 1 && arguments.get(0).equalsIgnoreCase("set")) {
            if (Pattern.matches("[!-~]*", arguments.get(1)) && arguments.get(1).length() <= 10 && !arguments.get(1).equalsIgnoreCase(prefix)) {
                DataSource.update("UPDATE GUILDS SET GUILD_PREFIX = '?' WHERE GUILD_ID = ?",
                        arguments.get(1), data.getGuild().getId());
                CommandTrigger.updateEntry(data.getGuild().getId(), arguments.get(1));
                ResponseDispatcher.commandSucceeded(
                        this, GenericEmbeds.setPrefix(
                                data.getMember().getUser().getAsTag(),
                                data.getMember().getUser().getAvatarUrl(),
                                arguments.get(1)
                        )
                );
            } else if (arguments.get(1).equalsIgnoreCase(prefix)) {
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
        } else if (arguments.size() == 1 && arguments.get(0).equalsIgnoreCase("set")) {
            ResponseDispatcher.commandFailed(this,
                    ErrorEmbeds.insertPrefix(this.getId()),
                    "User did not insert a prefix."
            );
        } else if (arguments.size() >= 1 && arguments.get(0).equalsIgnoreCase("reset")) {
            DataSource.update("UPDATE GUILDS SET GUILD_PREFIX = NULL WHERE GUILD_ID = ?", data.getGuild().getId());
            CommandTrigger.updateEntry(data.getGuild().getId(), Thermostat.prefix);
            ResponseDispatcher.commandSucceeded(this, GenericEmbeds.resetPrefix());
        } else {
            ResponseDispatcher.commandSucceeded(this, GenericEmbeds.getPrefix(data.getMember().getUser().getAsTag(), data.getMember().getUser().getAvatarUrl(), prefix));
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
