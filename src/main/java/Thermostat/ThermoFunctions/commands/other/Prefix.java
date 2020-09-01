package thermostat.thermoFunctions.commands.other;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import thermostat.Embeds;
import thermostat.mySQL.DataSource;
import thermostat.thermoFunctions.Messages;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

public class Prefix {
    public static void execute(ArrayList<String> args, @Nonnull Guild eventGuild, @Nonnull TextChannel eventChannel, @Nonnull Member eventMember, String prefix, boolean mentioned) {

        if (!mentioned) {
            args.remove(0);

            try {
                prefixAction(args, eventChannel, eventMember, eventGuild.getId(), prefix);
            } catch (SQLException ex) {
                Messages.sendMessage(eventChannel, Embeds.fatalError("Try setting the prefix again."));
            }
            return;
        }

        if (args.size() == 1) {
            return;
        }

        args.remove(0);
        args.remove(0);

        try {
            prefixAction(args, eventChannel, eventMember, eventGuild.getId(), prefix);
        } catch (SQLException ex) {
            Messages.sendMessage(eventChannel, Embeds.fatalError("Try setting the prefix again."));
        }
    }

    /**
     * Code to run when the command is called.
     *
     * @param args          User input.
     * @param channel       Channel where command was called.
     * @param guildId       ID of event guild.
     * @param currentPrefix Current prefix of thermostat.
     * @throws SQLException If some error went wrong with the DB conn.
     */
    public static void prefixAction(ArrayList<String> args, TextChannel channel, Member member, String guildId, String currentPrefix) throws SQLException {
        // if member isn't server admin, don't continue!
        if (!member.getPermissions().contains(Permission.ADMINISTRATOR)) {
            Messages.sendMessage(channel, Embeds.simpleInsufficientPerm("ADMINISTRATOR"));
            return;
        }

        if (args.size() > 1 && args.get(0).equalsIgnoreCase("set")) {
            if (Pattern.matches("[!-~]*", args.get(1)) && args.get(1).length() <= 10 && !args.get(1).equalsIgnoreCase(currentPrefix)) {
                Messages.sendMessage(channel, Embeds.setPrefix(member.getUser().getAsTag(), member.getUser().getAvatarUrl(), args.get(1)));
                DataSource.update("UPDATE GUILDS SET GUILD_PREFIX = '?' WHERE GUILD_ID = ?",
                        Arrays.asList(args.get(1), guildId));
            } else if (args.get(1).equalsIgnoreCase(currentPrefix)) {
                Messages.sendMessage(channel, Embeds.samePrefix(currentPrefix));
            } else {
                Messages.sendMessage(channel, Embeds.incorrectPrefix());
            }
        } else if (args.size() == 1 && args.get(0).equalsIgnoreCase("set")) {
            Messages.sendMessage(channel, Embeds.insertPrefix());
        } else if (args.size() >= 1 && args.get(0).equalsIgnoreCase("reset")) {
            DataSource.update("UPDATE GUILDS SET GUILD_PREFIX = NULL WHERE GUILD_ID = ?", guildId);
            Messages.sendMessage(channel, Embeds.resetPrefix());
        } else {
            Messages.sendMessage(channel, Embeds.getPrefix(member.getUser().getAsTag(), member.getUser().getAvatarUrl(), currentPrefix));
        }
    }
}
