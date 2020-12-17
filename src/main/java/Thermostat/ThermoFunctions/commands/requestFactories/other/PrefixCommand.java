package thermostat.thermoFunctions.commands.requestFactories.other;

import net.dv8tion.jda.api.entities.MessageEmbed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.mySQL.DataSource;
import thermostat.preparedStatements.ErrorEmbeds;
import thermostat.preparedStatements.GenericEmbeds;
import thermostat.preparedStatements.HelpEmbeds;
import thermostat.thermoFunctions.Messages;
import thermostat.thermoFunctions.commands.CommandData;
import thermostat.thermoFunctions.commands.requestFactories.Command;
import thermostat.thermoFunctions.entities.RequestType;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.regex.Pattern;

public class PrefixCommand implements Command {

    private static final Logger lgr = LoggerFactory.getLogger(PrefixCommand.class);

    private final CommandData data;

    public PrefixCommand(@Nonnull CommandData data) {
        this.data = data;

        checkPermissionsAndExecute(RequestType.FILTER, data.member(), data.channel(), lgr);
    }

    /**
     * Command form: th!prefix <prefix>
     * @return
     */
    @Override
    public MessageEmbed execute() {
        if (data.arguments().isEmpty()) {
            Messages.sendMessage(data.channel(), HelpEmbeds.helpPrefix(data.prefix()));
            return;
        }

        try {
            prefixAction(data);
        } catch (SQLException ex) {
            Messages.sendMessage(data.channel(), ErrorEmbeds.errFatal("Try setting the prefix again."));
        }
    }

    @Override
    public CommandData getData() {
        return data;
    }

    /**
     * Code to run when the command is called.
     *
     * @param data CommandData object that stores information about the command triggerer.
     * @throws SQLException If some error went wrong with the DB conn.
     */
    public static void prefixAction(@Nonnull CommandData data) throws SQLException {
        if (data.arguments().size() > 1 && data.arguments().get(0).equalsIgnoreCase("set")) {
            if (Pattern.matches("[!-~]*", data.arguments().get(1)) && data.arguments().get(1).length() <= 10 && !data.arguments().get(1).equalsIgnoreCase(data.prefix())) {
                Messages.sendMessage(data.channel(), GenericEmbeds.setPrefix(data.member().getUser().getAsTag(), data.member().getUser().getAvatarUrl(), data.arguments().get(1)));
                DataSource.update("UPDATE GUILDS SET GUILD_PREFIX = '?' WHERE GUILD_ID = ?",
                        Arrays.asList(data.arguments().get(1), data.guild().getId()));
            } else if (data.arguments().get(1).equalsIgnoreCase(data.prefix())) {
                Messages.sendMessage(data.channel(), GenericEmbeds.samePrefix(data.prefix()));
            } else {
                Messages.sendMessage(data.channel(), ErrorEmbeds.incorrectPrefix());
            }
        } else if (data.arguments().size() == 1 && data.arguments().get(0).equalsIgnoreCase("set")) {
            Messages.sendMessage(data.channel(), ErrorEmbeds.insertPrefix());
        } else if (data.arguments().size() >= 1 && data.arguments().get(0).equalsIgnoreCase("reset")) {
            DataSource.update("UPDATE GUILDS SET GUILD_PREFIX = NULL WHERE GUILD_ID = ?", data.guild().getId());
            Messages.sendMessage(data.channel(), GenericEmbeds.resetPrefix());
        } else {
            Messages.sendMessage(data.channel(), GenericEmbeds.getPrefix(data.member().getUser().getAsTag(), data.member().getUser().getAvatarUrl(), data.prefix()));
        }
    }
}
