package thermostat.thermoFunctions.commands.monitoring;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.Embeds;
import thermostat.mySQL.DataSource;
import thermostat.thermoFunctions.Messages;
import thermostat.thermoFunctions.commands.CommandEvent;
import thermostat.thermoFunctions.entities.CommandType;
import thermostat.thermostat;

import java.time.Instant;
import java.util.*;

import static thermostat.thermoFunctions.Functions.parseMention;
import static thermostat.thermoFunctions.Functions.parseSlowmode;

public class SetBounds implements CommandEvent {

    // Logging Device
    private static final Logger lgr = LoggerFactory.getLogger(SetBounds.class);

    private final Guild eventGuild;
    private final TextChannel eventChannel;
    private final Member eventMember;
    private final ArrayList<StringBuilder> args;

    private EnumSet<Permission> missingThermostatPerms, missingMemberPerms;

    private static final EmbedBuilder embed = new EmbedBuilder();

    public SetBounds(Guild eg, TextChannel tc, Member em, ArrayList<StringBuilder> ag)
    {
        eventGuild = eg;
        eventChannel = tc;
        eventMember = em;
        args = ag;

        checkPermissions();
        if (missingMemberPerms.isEmpty() && missingThermostatPerms.isEmpty()) {
            execute();
        } else {

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

    @Override
    public void execute() {
        if (args.size() < 2) {
            Messages.sendMessage(eventChannel, Embeds.bothChannelAndSlow());
            return;
        }

        // catch to remove command initiation with prefix
        args.remove(0);

        // checks if event member has permission
        if (!eventMember.hasPermission(Permission.MANAGE_CHANNEL)) {
            Messages.sendMessage(eventChannel, Embeds.userNoPermission("MANAGE_CHANNEL"));
            return;
        }

        String nonValid = "",
                noText = "",
                complete = "",
                badSlowmode = "";
        // shows us if there were arguments before
        // but were removed due to channel not being found
        boolean removed = false;

        // parses arguments into usable IDs, checks if channels exist
        // up to args.size() - 1 because the last argument is the slowmode
        for (int index = 0; index < args.size() - 1; ++index) {

            // The argument gets parsed. If it's a mention, it gets formatted
            // into an ID through the parseMention() function.
            // All letters are removed, thus the usage of the
            // originalArgument string.
            StringBuilder originalArgument = args.get(index);
            args.set(index, parseMention(args.get(index), "#"));

            // Category holder for null checking
            Category channelContainer = eventGuild.getCategoryById(args.get(index));

            if (args.get(index).isBlank()) {
                nonValid = nonValid.concat("\"" + originalArgument + "\" ");
                args.remove(index);
                removed = true;
                --index;

            } else if (channelContainer != null) {
                // firstly creates an immutable list of the channels in the category
                List<TextChannel> TextChannels = channelContainer.getTextChannels();
                // if list is empty add that it is in msg
                if (TextChannels.isEmpty()) {
                    noText = noText.concat("<#" + originalArgument + "> ");
                }
                // removes category ID from argument ArrayList
                args.remove(index);
                // iterates through every channel and adds its' id to the arg list
                for (TextChannel it : TextChannels) {
                    args.add(0, it.getId());
                }
                --index;
            }

            // removes element from arguments if it's not a valid channel ID
            else if (eventGuild.getTextChannelById(args.get(index)) == null) {
                nonValid = nonValid.concat("\"" + originalArgument + "\" ");
                args.remove(index);
                removed = true;
                --index;
            }
        }

        // Parsing the slowmode argument
        int argumentSlow;
        try {
            argumentSlow = parseSlowmode(args.get(args.size() - 1));
        } catch (NumberFormatException ex) {
            Messages.sendMessage(eventChannel, Embeds.invalidSlowmode());
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
                        complete = complete.concat("<#" + args.get(index) + "> ");
                    } else if (argumentSlow > minimumSlow && argumentSlow <= 21600) {
                        DataSource.update("UPDATE CHANNEL_SETTINGS SET MAX_SLOW = ? WHERE CHANNEL_ID = ?",
                                Arrays.asList(Integer.toString(argumentSlow), args.get(index)));
                        complete = complete.concat("<#" + args.get(index) + "> ");
                    } else {
                        badSlowmode = badSlowmode.concat("<#" + args.get(index) + "> ");
                    }

                } catch (Exception ex) {
                    nonValid = nonValid.concat("\"" + args.get(index) + "\" ");
                }
            }
        } else if (!removed) {
            try {
                int minimumSlow;
                minimumSlow = DataSource.queryInt("SELECT MIN_SLOW FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = ?", eventChannel.getId());

                if (argumentSlow <= minimumSlow && argumentSlow <= 21600) {
                    DataSource.update("UPDATE CHANNEL_SETTINGS SET MAX_SLOW = ?, MIN_SLOW = ? WHERE CHANNEL_ID = ?",
                            Arrays.asList(Integer.toString(argumentSlow), Integer.toString(argumentSlow), eventChannel.getId()));
                    complete = complete.concat("<#" + eventChannel.getId() + "> ");
                } else if (argumentSlow > minimumSlow && argumentSlow <= 21600) {
                    DataSource.update("UPDATE CHANNEL_SETTINGS SET MAX_SLOW = ? WHERE CHANNEL_ID = ?",
                            Arrays.asList(Integer.toString(argumentSlow), eventChannel.getId()));
                    complete = complete.concat("<#" + eventChannel.getId() + "> ");
                } else {
                    badSlowmode = badSlowmode.concat("<#" + eventChannel.getId() + "> ");
                }
            } catch (Exception ex) {
                Messages.sendMessage(eventChannel, Embeds.fatalError());
                return;
            }
        }

        embed.setColor(0xffff00);
        if (!complete.isEmpty()) {
            embed.addField("Channels given a maximum slowmode of " + argumentSlow + ":", complete, false);
            embed.setColor(0x00ff00);
        }

        if (!badSlowmode.isEmpty()) {
            embed.addField("Channels for which the given slowmode value was not appropriate:", badSlowmode, false);
        }

        if (!nonValid.isEmpty()) {
            embed.addField("Channels that were not valid or found:", nonValid, false);
        }

        if (!noText.isEmpty()) {
            embed.addField("Categories with no Text Channels:", noText, false);
        }

        embed.setTimestamp(Instant.now());
        embed.setFooter("Requested by " + eventMember.getUser().getAsTag(), eventMember.getUser().getAvatarUrl());
        Messages.sendMessage(eventChannel, embed);

        embed.clear();
    }
}
