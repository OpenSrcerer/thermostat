package thermostat.thermoFunctions.commands.monitoring;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.preparedStatements.ErrorEmbeds;
import thermostat.mySQL.DataSource;
import thermostat.thermoFunctions.Messages;
import thermostat.thermoFunctions.commands.CommandEvent;
import thermostat.thermoFunctions.entities.CommandType;
import thermostat.thermostat;

import java.time.Instant;
import java.util.*;

import static thermostat.thermoFunctions.Functions.parseSlowmode;

public class SetBounds implements CommandEvent {

    // Logging Device
    private static final Logger lgr = LoggerFactory.getLogger(SetBounds.class);

    private final Guild eventGuild;
    private final TextChannel eventChannel;
    private final Member eventMember;
    private ArrayList<String> args;

    private EnumSet<Permission> missingThermostatPerms, missingMemberPerms;

    private static final EmbedBuilder embed = new EmbedBuilder();

    public SetBounds(Guild eg, TextChannel tc, Member em, ArrayList<String> ag)
    {
        eventGuild = eg;
        eventChannel = tc;
        eventMember = em;
        args = ag;

        checkPermissions();
        if (missingMemberPerms.isEmpty() && missingThermostatPerms.isEmpty()) {
            execute();
        } else {
            Messages.sendMessage(eventChannel, ErrorEmbeds.errPermission(missingThermostatPerms, missingMemberPerms));
        }
    }

    public void checkPermissions() {
        eventGuild
                .retrieveMember(thermostat.thermo.getSelfUser())
                .map(thermostat -> {
                    missingThermostatPerms = findMissingPermissions(CommandType.SETBOUNDS.getThermoPerms(), thermostat.getPermissions());
                    return thermostat;
                })
                .queue();

        missingMemberPerms = findMissingPermissions(CommandType.SETBOUNDS.getMemberPerms(), eventMember.getPermissions());
    }

    /**
     * @param permissionsToSeek Permissions required by the command.
     * @param memberPermsList Permissions that the Member has.
     * @return Permissions that are needed but not assigned to a Member.
     */
    public @NotNull EnumSet<Permission> findMissingPermissions(EnumSet<Permission> permissionsToSeek, EnumSet<Permission> memberPermsList) {
        memberPermsList.forEach(permissionsToSeek::remove);
        return permissionsToSeek;
    }

    // Suppressing is okay because type for
    // results.get(3) is always ArrayList<String>
    @SuppressWarnings("unchecked")
    @Override
    public void execute() {
        if (args.size() < 3) {
            Messages.sendMessage(eventChannel, ErrorEmbeds.bothChannelAndSlow());
            return;
        }

        // command initiation with prefix removal
        args.remove(0);

        StringBuilder nonValid,
                noText,
                complete = new StringBuilder(),
                badSlowmode = new StringBuilder();

        boolean removed;
        {
            List<?> results = parseChannelArgument(eventGuild, args);

            nonValid = (StringBuilder) results.get(0);
            noText = (StringBuilder) results.get(1);
            removed = (boolean) results.get(2);
            args = (ArrayList<String>) results.get(3);
        }

        // Parsing the slowmode argument
        int argumentSlow;
        try {
            argumentSlow = parseSlowmode(args.get(args.size() - 1));
        } catch (NumberFormatException ex) {
            Messages.sendMessage(eventChannel, ErrorEmbeds.invalidSlowmode());
            return;
        }

        if (args.size() >= 2) {
            for (int index = 0; index < args.size() - 1; ++index) {
                try {
                    int minimumSlow;
                    minimumSlow = DataSource.queryInt("SELECT MIN_SLOW FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = ?", args.get(index));

                    if (argumentSlow <= minimumSlow && argumentSlow <= 21600) {
                        DataSource.update("UPDATE CHANNEL_SETTINGS SET MAX_SLOW = ?, MIN_SLOW = ? WHERE CHANNEL_ID = ?",
                                Arrays.asList(Integer.toString(argumentSlow), Integer.toString(argumentSlow), args.get(index)));
                        complete.append("<#").append(args.get(index)).append("> ");
                    } else if (argumentSlow > minimumSlow && argumentSlow <= 21600) {
                        DataSource.update("UPDATE CHANNEL_SETTINGS SET MAX_SLOW = ? WHERE CHANNEL_ID = ?",
                                Arrays.asList(Integer.toString(argumentSlow), args.get(index)));
                        complete.append("<#").append(args.get(index)).append("> ");
                    } else {
                        badSlowmode.append("<#").append(args.get(index)).append("> ");
                    }

                } catch (Exception ex) {
                    nonValid.append("\"").append(args.get(index)).append("\" ");
                }
            }
        } else if (!removed) {
            try {
                int minimumSlow;
                minimumSlow = DataSource.queryInt("SELECT MIN_SLOW FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = ?", eventChannel.getId());

                if (argumentSlow <= minimumSlow && argumentSlow <= 21600) {
                    DataSource.update("UPDATE CHANNEL_SETTINGS SET MAX_SLOW = ?, MIN_SLOW = ? WHERE CHANNEL_ID = ?",
                            Arrays.asList(Integer.toString(argumentSlow), Integer.toString(argumentSlow), eventChannel.getId()));
                    complete.append("<#").append(eventChannel.getId()).append("> ");
                } else if (argumentSlow > minimumSlow && argumentSlow <= 21600) {
                    DataSource.update("UPDATE CHANNEL_SETTINGS SET MAX_SLOW = ? WHERE CHANNEL_ID = ?",
                            Arrays.asList(Integer.toString(argumentSlow), eventChannel.getId()));
                    complete.append("<#").append(eventChannel.getId()).append("> ");
                } else {
                    badSlowmode.append("<#").append(eventChannel.getId()).append("> ");
                }
            } catch (Exception ex) {
                Messages.sendMessage(eventChannel, ErrorEmbeds.errFatal());
                return;
            }
        }

        embed.setColor(0xffff00);
        if (!(complete.length() == 0)) {
            embed.addField("Channels given a maximum slowmode of " + argumentSlow + ":", complete, false);
            embed.setColor(0x00ff00);
        }

        if (!(badSlowmode.length() == 0)) {
            embed.addField("Channels for which the given slowmode value was not appropriate:", badSlowmode, false);
        }

        if (!(nonValid.length() == 0)) {
            embed.addField("Channels that were not valid or found:", nonValid, false);
        }

        if (!(noText.length() == 0)) {
            embed.addField("Categories with no Text Channels:", noText, false);
        }

        embed.setTimestamp(Instant.now());
        embed.setFooter("Requested by " + eventMember.getUser().getAsTag(), eventMember.getUser().getAvatarUrl());
        Messages.sendMessage(eventChannel, embed);

        embed.clear();
    }
}
