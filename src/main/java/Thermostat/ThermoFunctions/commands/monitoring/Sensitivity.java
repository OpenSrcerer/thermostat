package thermostat.thermoFunctions.commands.monitoring;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;
import thermostat.preparedStatements.ErrorEmbeds;
import thermostat.mySQL.DataSource;
import thermostat.preparedStatements.HelpEmbeds;
import thermostat.thermoFunctions.Messages;
import thermostat.thermoFunctions.commands.CommandEvent;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static thermostat.thermoFunctions.Functions.parseMention;

public class Sensitivity implements CommandEvent {

    public Sensitivity(ArrayList<String> args, @Nonnull Guild eventGuild, @Nonnull TextChannel eventChannel, @Nonnull Member eventMember, String prefix) {

    }

    @Override
    public void checkPermissions() {

    }

    @NotNull
    @Override
    public EnumSet<Permission> findMissingPermissions(EnumSet<Permission> permissionsToSeek, EnumSet<Permission> givenPermissions) {
        return null;
    }

    @Override
    public void execute() {
        // checks if event member has permission
        if (!eventMember.hasPermission(Permission.MANAGE_CHANNEL)) {
            Messages.sendMessage(eventChannel, ErrorEmbeds.specifyChannels());
            return;
        }

        // wrong command format
        if (args.size() < 2) {
            Messages.sendMessage(eventChannel, HelpEmbeds.helpSensitivity(prefix));
            return;
        }

        // catch to remove command initiation with prefix
        args.remove(0);

        String nonValid = "",
                noText = "",
                complete = "",
                badSensitivity = "";

        // shows if there were arguments before
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

        float offset;
        try {
            offset = Float.parseFloat(args.get(args.size() - 1));
        } catch (NumberFormatException ex) {
            Messages.sendMessage(eventChannel, ErrorEmbeds.invalidSensitivity());
            return;
        }

        if (args.size() >= 2) {
            for (int index = 0; index < args.size() - 1; ++index) {
                try {
                    if (offset >= -10 && offset <= 10) {
                        DataSource.update("UPDATE CHANNEL_SETTINGS SET SENSOFFSET = ? WHERE CHANNEL_ID = ?",
                                Arrays.asList(Float.toString(1f + offset / 20f), args.get(index)));
                        complete = complete.concat("<#" + args.get(index) + "> ");
                    } else {
                        badSensitivity = badSensitivity.concat("<#" + args.get(index) + "> ");
                    }

                } catch (Exception ex) {
                    nonValid = nonValid.concat("\"" + args.get(index) + "\" ");
                }
            }
        } else if (!removed) {
            try {
                if (offset >= -10 && offset <= 10) {
                    DataSource.update("UPDATE CHANNEL_SETTINGS SET SENSOFFSET = ? WHERE CHANNEL_ID = ?",
                            Arrays.asList(Float.toString(1f + offset / 20f), eventChannel.getId()));
                    complete = complete.concat("<#" + eventChannel.getId() + "> ");
                } else {
                    badSensitivity = badSensitivity.concat("<#" + eventChannel.getId() + "> ");
                }
            } catch (Exception ex) {
                Messages.sendMessage(eventChannel, ErrorEmbeds.errFatal());
                return;
            }
        }

        embed.setColor(0xffff00);
        if (!complete.isEmpty()) {
            embed.addField("Channels given a new sensitivity of " + offset + ":", complete, false);
            embed.setColor(0x00ff00);
        }

        if (!badSensitivity.isEmpty()) {
            embed.addField("Offset value was not appropriate:", badSensitivity, false);
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
