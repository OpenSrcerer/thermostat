package thermostat.commands.other;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.commands.Command;
import thermostat.commands.CommandTrigger;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.embeds.Embeds;
import thermostat.mySQL.DataSource;
import thermostat.util.Constants;
import thermostat.util.entities.CommandData;
import thermostat.util.enumeration.CommandType;
import thermostat.util.enumeration.EmbedType;

import javax.annotation.Nonnull;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

import static thermostat.util.ArgumentParser.hasArguments;

public class PrefixCommand implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(PrefixCommand.class);
    private final CommandData data;

    public PrefixCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) {
        this.data = new CommandData(data, arguments, prefix);

        if (this.data.parameters == null) {
            ResponseDispatcher.commandFailed(
                    this,
                    Embeds.getEmbed(EmbedType.ERR, this.data),
                    "Bad arguments.");
            return;
        }

        checkPermissionsAndQueue(this);
    }

    /**
     * Command form: th!prefix <prefix>
     */
    @Override
    public void run() {
        List<String> prefixParameters = data.parameters.get("p");
        List<String> resetSwitch = data.parameters.get("-reset");

        try {
            prefixAction(prefixParameters, resetSwitch);
        } catch (SQLException ex) {
            ResponseDispatcher.commandFailed(this,
                    Embeds.getEmbed(EmbedType.ERR, data, ex.getMessage()),
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
                statement.setString(1, data.event.getGuild().getId());
                return null;
            });
            CommandTrigger.updateEntry(data.event.getGuild().getId(), Constants.DEFAULT_PREFIX);
            ResponseDispatcher.commandSucceeded(this, Embeds.getEmbed(EmbedType.RESET_PREFIX, data));
            return;
        }

        // -p switch
        if (hasArguments(prefixParameters)) {
            String newPrefix = prefixParameters.get(0);

            if (Pattern.matches("[!-~]*", newPrefix) && newPrefix.length() <= 5 && !newPrefix.equalsIgnoreCase(data.prefix)) {
                DataSource.execute(conn -> {
                    PreparedStatement statement = conn.prepareStatement("UPDATE GUILDS SET GUILD_PREFIX = ? WHERE GUILD_ID = ?;");
                    statement.setString(1, newPrefix);
                    statement.setString(2, data.event.getGuild().getId());
                    return null;
                });
                CommandTrigger.updateEntry(data.event.getGuild().getId(), newPrefix);
                ResponseDispatcher.commandSucceeded(
                        this,
                        Embeds.getEmbed(EmbedType.NEW_PREFIX, data, newPrefix)
                );
            } else if (newPrefix.equalsIgnoreCase(data.prefix)) {
                ResponseDispatcher.commandFailed(this,
                        Embeds.getEmbed(EmbedType.SAME_PREFIX, data),
                        "User inserted the same prefix."
                );
            } else {
                ResponseDispatcher.commandFailed(this,
                        Embeds.getEmbed(EmbedType.ERR_INPUT, data, "You have inserted an invalid prefix."),
                        "User inserted an invalid prefix."
                );
            }
        } else {
            ResponseDispatcher.commandSucceeded(this,
                    Embeds.getEmbed(EmbedType.GET_PREFIX, data)
            );
        }
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
    public CommandData getData() {
        return data;
    }
}
