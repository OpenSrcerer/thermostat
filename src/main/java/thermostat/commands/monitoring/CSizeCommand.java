package thermostat.commands.monitoring;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.commands.Command;
import thermostat.dispatchers.CommandDispatcher;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.embeds.Embeds;
import thermostat.mySQL.DataSource;
import thermostat.util.GuildCache;
import thermostat.util.entities.CommandData;
import thermostat.util.enumeration.CommandType;
import thermostat.util.enumeration.EmbedType;

import javax.annotation.Nonnull;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static thermostat.util.ArgumentParser.hasArguments;

public class CSizeCommand implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(CSizeCommand.class);
    private final CommandData data;

    public CSizeCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) {
        this.data = new CommandData(data, arguments, prefix);

        if (this.data.parameters == null) {
            ResponseDispatcher.commandFailed(
                    this,
                    Embeds.getEmbed(EmbedType.HELP_SETCACHE, this.data),
                    "Bad Arguments / None were provided.");
            return;
        }

        CommandDispatcher.checkPermissionsAndQueue(this);
    }

    /**
     * Command form: th!setcaching
     * -s <size>
     * -c <channels/categories>
     */
    @Override
    public void run() {
        final List<String> cachingSize = data.parameters.get("s");
        int newCachingSize;

        // Check that size has arguments
        if (!hasArguments(cachingSize)) {
            ResponseDispatcher.commandFailed(this,
                    Embeds.getEmbed(EmbedType.HELP_SETCACHE, data),
                    "User did not provide arguments.");
            return;
        }

        // Parse caching size argument
        try {
            newCachingSize = Integer.parseInt(cachingSize.get(0));

            if (newCachingSize < 5 || newCachingSize > 100) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            ResponseDispatcher.commandFailed(this,
                    Embeds.getEmbed(EmbedType.ERR_INPUT, data,
                            "Caching size must be between 5 (inclusive) and 100 (inclusive)."),
                    "Incorrect caching size.");
            return;
        }

        try {
            cacheSizeAction(newCachingSize);
        } catch (Exception ex) {
            ResponseDispatcher.commandFailed(this, Embeds.getEmbed(EmbedType.ERR, data, ex.getMessage()), ex);
            return;
        }

        GuildCache.setCacheSize(data.event.getGuild().getId(), newCachingSize); // Set CSize Cache
        ResponseDispatcher.commandSucceeded(this, Embeds.getEmbed(EmbedType.SET_CACHE, data, newCachingSize)); // Send embed to user
    }

    private void cacheSizeAction(final int newCachingSize) throws SQLException {
        DataSource.demand(conn -> {
            PreparedStatement statement = conn.prepareStatement("UPDATE GUILDS SET CACHING_SIZE = ? WHERE GUILD_ID = ?");
            statement.setInt(1, newCachingSize);
            statement.setString(2, data.event.getGuild().getId());
            statement.executeUpdate();
            return null;
        });
    }

    @Override
    public Logger getLogger() {
        return lgr;
    }

    @Override
    public CommandData getData() {
        return data;
    }

    @Override
    public CommandType getType() {
        return CommandType.SETCACHING;
    }
}
