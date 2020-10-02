package thermostat.thermoFunctions.commands.monitoring;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.preparedStatements.ErrorEmbeds;
import thermostat.mySQL.DataSource;
import thermostat.preparedStatements.HelpEmbeds;
import thermostat.thermoFunctions.Messages;
import thermostat.thermoFunctions.commands.CommandEvent;
import thermostat.thermoFunctions.entities.CommandType;
import thermostat.thermostat;

import java.sql.SQLException;
import java.time.Instant;
import java.util.*;

import static thermostat.thermoFunctions.Functions.parseSlowmode;

public class SetBounds implements CommandEvent {

    // Logging Device
    private static final Logger lgr = LoggerFactory.getLogger(SetBounds.class);

    private final Guild eventGuild;
    private final TextChannel eventChannel;
    private final Member eventMember;
    private final String eventPrefix;
    private ArrayList<String> args;

    private EnumSet<Permission> missingThermostatPerms, missingMemberPerms;

    private static final EmbedBuilder embed = new EmbedBuilder();

    private enum ActionType {
        INVALID, MINIMUM, MAXIMUM
    }

    public SetBounds(Guild eg, TextChannel tc, Member em, String px, ArrayList<String> ag)
    {
        eventGuild = eg;
        eventChannel = tc;
        eventMember = em;
        eventPrefix = px;
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
        if (args.size() < 2) {
            Messages.sendMessage(eventChannel, ErrorEmbeds.bothChannelAndSlow());
            return;
        }

        StringBuilder nonValid = new StringBuilder(),
                noText = new StringBuilder(),
                minComplete = new StringBuilder(),
                maxComplete = new StringBuilder(),
                badSlowmode = new StringBuilder();
        // type represents the action being taken
        // setMaximum or setMinimum
        ActionType type = ActionType.INVALID;
        // value given to us by user to assign as slowmode
        int argumentSlow;

        // #1 - Check the [minimum/maximum] argument
        if (args.get(0).contains("max")) {
            type = ActionType.MAXIMUM;
        } else if (args.get(0).contains("min")) {
            type = ActionType.MINIMUM;
        }

        // #2 - Check the [slowmode] argument
        try {
            argumentSlow = parseSlowmode(args.get(1));
        } catch (NumberFormatException ex) {
            Messages.sendMessage(eventChannel, ErrorEmbeds.invalidSlowmode());
            return;
        }

        // #3 - Remove the [min/max] and [slowmode] arguments
        args.subList(0, 1).clear();

        // #4 - Parse the optional <channels/categories> argument
        if (!args.isEmpty())
        {
            List<?> results = parseChannelArgument(eventGuild, args);

            nonValid = (StringBuilder) results.get(0);
            noText = (StringBuilder) results.get(1);
            args = (ArrayList<String>) results.get(2);
        }

        // #5 - If no channel arguments were provided, add
        // event channel as the target channel.
        else {
            args.add(eventChannel.getId());
        }
        // args now remains as a list of target channel(s).

        // #6 - Perform the appropriate actions
        int minimumSlow, maximumSlow;
        
        if (type != ActionType.INVALID) {
            for (String arg : args) {
                try {

                    {
                        List<Integer> channelSlowmodes = DataSource.queryInts("SELECT MIN_SLOW, MAX_SLOW FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = ?", arg);

                        minimumSlow = channelSlowmodes.get(0);
                        maximumSlow = channelSlowmodes.get(1);
                    }

                    if (argumentSlow > 21600) {
                        badSlowmode.append("<#").append(arg).append("> ");
                    } else if (argumentSlow < minimumSlow && type == ActionType.MAXIMUM) {
                        DataSource.update("UPDATE CHANNEL_SETTINGS SET MAX_SLOW = ?, MIN_SLOW = ? WHERE CHANNEL_ID = ?",
                                Arrays.asList(Integer.toString(argumentSlow), Integer.toString(argumentSlow), arg));
                        maxComplete.append("<#").append(arg).append("> ");
                    } else if (argumentSlow >= minimumSlow && type == ActionType.MAXIMUM) {
                        DataSource.update("UPDATE CHANNEL_SETTINGS SET MAX_SLOW = ? WHERE CHANNEL_ID = ?",
                                Arrays.asList(Integer.toString(argumentSlow), arg));
                        maxComplete.append("<#").append(arg).append("> ");
                    } else if (argumentSlow > maximumSlow) {
                        DataSource.update("UPDATE CHANNEL_SETTINGS SET MIN_SLOW = ?, MAX_SLOW = ? WHERE CHANNEL_ID = ?",
                                Arrays.asList(Integer.toString(argumentSlow), Integer.toString(argumentSlow), arg));
                        minComplete.append("<#").append(arg).append("> ");
                    } else if (argumentSlow <= maximumSlow) {
                        DataSource.update("UPDATE CHANNEL_SETTINGS SET MIN_SLOW = ? WHERE CHANNEL_ID = ?",
                                Arrays.asList(Integer.toString(argumentSlow), arg));
                        minComplete.append("<#").append(arg).append("> ");
                    }

                } catch (SQLException ex) {
                    nonValid.append("\"").append(arg).append("\" ");
                }
            }

        } else {
            Messages.sendMessage(eventChannel, HelpEmbeds.helpSetBounds(eventPrefix));
        }

        // #7 - Send the results embed
        embed.setColor(0xffff00);
        if (!(complete.length() == 0)) {
            embed.addField("Channels given a maximum slowmode of " + argumentSlow + ":", complete.toString(), false);
            embed.setColor(0x00ff00);
        }

        if (!(badSlowmode.length() == 0)) {
            embed.addField("Channels for which the given slowmode value was not appropriate:", badSlowmode.toString(), false);
        }

        if (!(nonValid.length() == 0)) {
            embed.addField("Channels that were not valid or found:", nonValid.toString(), false);
        }

        if (!(noText.length() == 0)) {
            embed.addField("Categories with no Text Channels:", noText.toString(), false);
        }

        embed.setTimestamp(Instant.now());
        embed.setFooter("Requested by " + eventMember.getUser().getAsTag(), eventMember.getUser().getAvatarUrl());
        Messages.sendMessage(eventChannel, embed);

        embed.clear();
    }
}
