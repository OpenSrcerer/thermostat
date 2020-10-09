package thermostat.thermoFunctions.commands.other;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.preparedStatements.ErrorEmbeds;
import thermostat.preparedStatements.GenericEmbeds;
import thermostat.mySQL.DataSource;
import thermostat.preparedStatements.HelpEmbeds;
import thermostat.thermoFunctions.Messages;
import thermostat.thermoFunctions.commands.CommandEvent;
import thermostat.thermoFunctions.entities.CommandType;
import thermostat.thermostat;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.regex.Pattern;

public class Prefix implements CommandEvent {

    private static final Logger lgr = LoggerFactory.getLogger(Prefix.class);

    private final Guild eventGuild;
    private final TextChannel eventChannel;
    private final Member eventMember;
    private final String eventPrefix;
    private final ArrayList<String> args;

    private EnumSet<Permission> missingThermostatPerms, missingMemberPerms;

    public Prefix(Guild eg, TextChannel tc, Member em, String px, ArrayList<String> ag) {
        eventGuild = eg;
        eventChannel = tc;
        eventMember = em;
        eventPrefix = px;
        args = ag;

        checkPermissions();
        if (missingMemberPerms.isEmpty() && missingThermostatPerms.isEmpty()) {
            execute();
        } else {
            lgr.info("Missing permissions on (" + eventGuild.getName() + "/" + eventGuild.getId() + "):" +
                    " [" + missingThermostatPerms.toString() + "] [" + missingMemberPerms.toString() + "]");
            Messages.sendMessage(eventChannel, ErrorEmbeds.errPermission(missingThermostatPerms, missingMemberPerms));
        }
    }

    @Override
    public void checkPermissions() {
        eventGuild
                .retrieveMember(thermostat.thermo.getSelfUser())
                .map(thermostat -> {
                    missingThermostatPerms = findMissingPermissions(CommandType.PREFIX.getThermoPerms(), thermostat.getPermissions());
                    return thermostat;
                })
                .queue();

        missingMemberPerms = findMissingPermissions(CommandType.PREFIX.getMemberPerms(), eventMember.getPermissions());
    }

    /**
     * Command form: th!prefix <prefix>
     */
    @Override
    public void execute() {
        if (args.isEmpty()) {
            Messages.sendMessage(eventChannel, HelpEmbeds.helpPrefix(eventPrefix));
            return;
        }

        try {
            prefixAction(args, eventChannel, eventMember, eventGuild.getId(), eventPrefix);
        } catch (SQLException ex) {
            Messages.sendMessage(eventChannel, ErrorEmbeds.errFatal("Try setting the prefix again."));
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
        if (args.size() > 1 && args.get(0).equalsIgnoreCase("set")) {
            if (Pattern.matches("[!-~]*", args.get(1)) && args.get(1).length() <= 10 && !args.get(1).equalsIgnoreCase(currentPrefix)) {
                Messages.sendMessage(channel, GenericEmbeds.setPrefix(member.getUser().getAsTag(), member.getUser().getAvatarUrl(), args.get(1)));
                DataSource.update("UPDATE GUILDS SET GUILD_PREFIX = '?' WHERE GUILD_ID = ?",
                        Arrays.asList(args.get(1), guildId));
            } else if (args.get(1).equalsIgnoreCase(currentPrefix)) {
                Messages.sendMessage(channel, GenericEmbeds.samePrefix(currentPrefix));
            } else {
                Messages.sendMessage(channel, ErrorEmbeds.incorrectPrefix());
            }
        } else if (args.size() == 1 && args.get(0).equalsIgnoreCase("set")) {
            Messages.sendMessage(channel, ErrorEmbeds.insertPrefix());
        } else if (args.size() >= 1 && args.get(0).equalsIgnoreCase("reset")) {
            DataSource.update("UPDATE GUILDS SET GUILD_PREFIX = NULL WHERE GUILD_ID = ?", guildId);
            Messages.sendMessage(channel, GenericEmbeds.resetPrefix());
        } else {
            Messages.sendMessage(channel, GenericEmbeds.getPrefix(member.getUser().getAsTag(), member.getUser().getAvatarUrl(), currentPrefix));
        }
    }
}
