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
    private final ArrayList<String> args;

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
            Messages.sendMessage(eventChannel, Embeds.permissionError(missingThermostatPerms, missingMemberPerms));
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

        StringBuilder nonValid = new StringBuilder(),
                noText = new StringBuilder(),
                complete = new StringBuilder(),
                badSlowmode = new StringBuilder();
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
            String originalArgument = args.get(index);
            args.set(index, parseMention(args.get(index), "#"));

            // Category holder for null checking
            Category channelContainer = eventGuild.getCategoryById(args.get(index));

            if (args.get(index).isBlank()) {
                nonValid.append("\"").append(originalArgument).append("\" ");
                args.remove(index);
                removed = true;
                --index;

            } else if (channelContainer != null) {
                // firstly creates an immutable list of the channels in the category
                List<TextChannel> TextChannels = channelContainer.getTextChannels();
                // if list is empty add that it is in msg
                if (TextChannels.isEmpty()) {
                    noText.append("<#").append(originalArgument).append("> ");
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
                nonValid.append("\"").append(originalArgument).append("\" ");
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
                Messages.sendMessage(eventChannel, Embeds.fatalError());
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
