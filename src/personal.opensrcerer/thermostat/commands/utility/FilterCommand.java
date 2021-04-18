package thermostat.commands.utility;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.commands.Command;
import thermostat.dispatchers.CommandDispatcher;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.embeds.Embeds;
import thermostat.mySQL.DataSource;
import thermostat.mySQL.PreparedActions;
import thermostat.util.ArgumentParser;
import thermostat.util.MiscellaneousFunctions;
import thermostat.util.RestActions;
import thermostat.util.entities.CommandArguments;
import thermostat.util.entities.CommandContext;
import thermostat.util.enumeration.CommandType;
import thermostat.util.enumeration.DBActionType;
import thermostat.util.enumeration.EmbedType;
import thermostat.util.enumeration.MenuType;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Enables/Disables the word filter for a channel.
 */
public class FilterCommand implements Command {

    /**
     * Logger for this class.
     */
    private static final Logger lgr = LoggerFactory.getLogger(FilterCommand.class);

    /**
     * Data for this command.
     */
    private final CommandContext data;

    public FilterCommand(@Nonnull GuildMessageReceivedEvent data, @Nonnull List<String> arguments, @Nonnull String prefix) {
        this.data = new CommandContext(data, arguments, prefix);

        if (this.data.parameters == null) {
            ResponseDispatcher.commandFailed(
                    this,
                    Embeds.getEmbed(EmbedType.ERR, this.data),
                    "Bad arguments.");
            return;
        }

        CommandDispatcher.checkPermissionsAndQueue(this);
    }

    /**
     * Command form: th!filter
     */
    @Override
    public void run() {
        final List<String> channels = data.parameters.get("c");
        final List<String> onSwitch = data.parameters.get("-on");
        final List<String> offSwitch = data.parameters.get("-off");
        final List<String> allSwitch = data.parameters.get("-all");
        final List<String> badSwitch = data.parameters.get("-bad");
        final List<String> goodSwitch = data.parameters.get("-good");

        if (badSwitch != null || goodSwitch != null) {
            List<Message.Attachment> attachments = data.event.getMessage().getAttachments();

            // Get the Attachment object from the Message
            Message.Attachment textAttachment = (attachments.isEmpty()) ? null : attachments.stream()
                    .filter(attachment -> Objects.equals(attachment.getFileExtension(), ".txt"))
                    .findFirst().orElse(null);

            if (textAttachment == null) {
                // ResponseDispatcher...
                return;
            }

            // Queue a request to retrieve the contents of the file and then
            // Store it in the database
            textAttachment.retrieveInputStream().thenAccept(
                    inputStream -> {
                        try {
                            updateFilterFiles(badSwitch != null, inputStream);
                        } catch (SQLException ex) {
                            // ResponseDispatcher...
                        }
                    });
        }

        if (offSwitch == null && onSwitch == null) {
            ResponseDispatcher.commandFailed(this, Embeds.getEmbed(EmbedType.HELP_FILTER, data),
                    "User did not provide arguments.");
            return;
        }

        if (allSwitch != null) {
            ResponseDispatcher.commandSucceeded(this, filterAll(onSwitch != null));
            return;
        }

        filterAction(
                ArgumentParser.parseChannelArgument(data.event.getChannel(), channels),
                MiscellaneousFunctions.getMonitorValue(onSwitch, offSwitch)
        );
    }

    private void filterAction(CommandArguments arguments, int filter) {
        DBActionType type = filter == 1 ? DBActionType.FILTER : DBActionType.UNFILTER;
        final StringBuilder complete;

        // Filter Target Channels
        try {
            complete = DataSource.demand(conn -> PreparedActions.modifyChannel(
                    conn, type, filter, data.event.getGuild().getId(), arguments.channels)
            );
        } catch (SQLException ex) {
            ResponseDispatcher.commandFailed(this,
                    Embeds.getEmbed(EmbedType.ERR, data, ex.getMessage()),
                    ex);
            return;
        }

        // Switch message depending on user action
        final String message;
        if (filter == 1) {
            message = "Enabled filtering on:";
        } else {
            message = "Disabled filtering on:";
        }

        // Send the results embed to dispatch
        ResponseDispatcher.commandSucceeded(this,
                Embeds.getEmbed(EmbedType.DYNAMIC, data,
                        Arrays.asList(
                                message,
                                complete.toString(),
                                "Channels that were not valid or found:",
                                arguments.nonValid.toString(),
                                "Categories with no Text Channels:",
                                arguments.noText.toString()
                        )
                )
        );
    }

    /**
     * @param action False = badWords; True = goodWords
     */
    private void updateFilterFiles(boolean action, InputStream stream) throws SQLException {
        String fileToSet = (action) ? "CENSORED_FILE" : "REPLACEMENT_FILE";
        DataSource.demand(conn -> {
            PreparedStatement statement = conn.prepareStatement("UPDATE GUILDS SET CENSORED_FILE = ?");
            statement.setBlob(1, InputStream.nullInputStream());
            return null;
        });
    }

    private RestAction<Void> filterAll(final boolean filter) {
        MenuType type = (filter) ? MenuType.FILTERALL : MenuType.UNFILTERALL;

        return RestActions.sendMessage(data.event.getChannel(), Embeds.getEmbed(EmbedType.PROMPT, data))
                .flatMap(message -> MiscellaneousFunctions.addNewMenu(message, type, this));
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
    public CommandContext getData() {
        return data;
    }
}
