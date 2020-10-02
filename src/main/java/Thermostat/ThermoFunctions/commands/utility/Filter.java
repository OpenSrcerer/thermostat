package thermostat.thermoFunctions.commands.utility;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import thermostat.preparedStatements.ErrorEmbeds;
import thermostat.preparedStatements.GenericEmbeds;
import thermostat.mySQL.DataSource;
import thermostat.thermoFunctions.Messages;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static thermostat.thermoFunctions.Functions.*;

public class Filter {
    private static final EmbedBuilder embed = new EmbedBuilder();

    public static void execute(ArrayList<String> args, @Nonnull Guild eventGuild, @Nonnull TextChannel eventChannel, @Nonnull Member eventMember) {

        // checks if event member has permission
        if (!eventMember.hasPermission(Permission.MANAGE_CHANNEL, Permission.MANAGE_WEBHOOKS)) {
            Messages.sendMessage(eventChannel, GenericEmbeds.userNoPermission("MANAGE_CHANNEL, MANAGE_WEBHOOKS"));
            return;
        }

        if (args.size() >= 2) {
            args.remove(0);
        } else {
            args.set(0, eventChannel.getId());
        }

        String nonValid = "",
                noText = "",
                complete = "";

        // shows if there were arguments before
        // but were removed due to channel not being found
        boolean removed = false;

        // parses arguments into usable IDs, checks if channels exist
        // up to args.size() - 1 because the last argument is the filter value
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

        String filtered = convertToBooleanString(args.get(args.size() - 1));

        if (args.size() >= 2) {
            for (int index = 0; index < args.size() - 1; ++index) {
                complete = setDatabase(filtered, args.get(index), eventChannel, complete);
            }
        } else if (!removed) {
            complete = setDatabase(filtered, eventChannel.getId(), eventChannel, complete);
        }

        embed.setColor(0xffff00);
        if (!complete.isEmpty()) {
            if (filtered.equals("1"))
                embed.addField("Enabled filtering on:", complete, false);
            else
                embed.addField("Disabled filtering on:", complete, false);
            embed.setColor(0x00ff00);
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

    public static String setDatabase(String filtered, String targetChannel, TextChannel eventChannel, String complete) {
        try {
            if (filtered.equals("0")) {
                DataSource.update("UPDATE CHANNEL_SETTINGS SET FILTERED = ?, WEBHOOK_URL = \"N/A\" WHERE CHANNEL_ID = ?",
                        Arrays.asList(filtered, targetChannel));
            } else {
                DataSource.update("UPDATE CHANNEL_SETTINGS SET FILTERED = ? WHERE CHANNEL_ID = ?",
                        Arrays.asList(filtered, targetChannel));
            }
            complete = complete.concat("<#" + targetChannel + "> ");
        } catch (Exception ex) {
            Messages.sendMessage(eventChannel, ErrorEmbeds.errFatal());
        }
        return complete;
    }
}
