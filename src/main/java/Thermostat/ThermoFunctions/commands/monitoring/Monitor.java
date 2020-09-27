package thermostat.thermoFunctions.commands.monitoring;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import thermostat.Embeds;
import thermostat.mySQL.Create;
import thermostat.mySQL.DataSource;
import thermostat.thermoFunctions.Messages;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static thermostat.thermoFunctions.Functions.parseMention;

/**
 * Adds channels to the database provided in
 * db.properties, upon user running the
 * command.
 */
public class Monitor {
    private static final EmbedBuilder embed = new EmbedBuilder();

    public static void execute(ArrayList<String> args, @Nonnull Guild eventGuild, @Nonnull TextChannel eventChannel, @Nonnull Member eventMember) {
        if (args.size() == 1) {
            Messages.sendMessage(eventChannel, Embeds.specifyChannels());
            return;
        }

        // catch to remove command initiation with prefix
        args.remove(0);

        // checks if event member has permission
        if (!eventMember.hasPermission(Permission.MANAGE_CHANNEL)) {
            Messages.sendMessage(eventChannel, Embeds.userNoPermission("MANAGE_CHANNEL"));
            return;
        }

        // Strings to store information for the embed.
        // (Just got monitored, already monitored, not valid
        // to be monitored, category has no text channels
        String nonValid = "",
                noText = "",
                complete = "",
                monitored = "";

        // parses arguments into usable IDs, checks if channels exist
        for (int index = 0; index < args.size(); ++index) {

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
                --index;
            }

            // If ID isn't a channel (above) but is a category (below) proceed
            else if (channelContainer != null) {
                // firstly creates an immutable list of the channels in the category
                List<TextChannel> TextChannels = channelContainer.getTextChannels();
                // if list is empty add that it is in msg
                if (TextChannels.isEmpty()) {
                    noText = noText.concat("<#" + args.get(index) + "> ");
                }
                // removes category ID from argument ArrayList
                args.remove(index);
                // iterates through every channel and adds its' id to the arg list
                for (TextChannel it : TextChannels) {
                    args.add(it.getId());
                }
                --index;
            }

            // removes element from arguments if it's not a valid channel ID
            else if (eventGuild.getTextChannelById(args.get(index)) == null) {
                nonValid = nonValid.concat("\"" + args.get(index) + "\" ");
                args.remove(index);
                --index;
            }
        }

        // connects to database and creates channel
        for (String it : args) {
            try {
                // checks whether the channel has the monitor
                // value on the database set to 1
                if (DataSource.queryBool("SELECT MONITORED FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = ?", it)) {
                    monitored = monitored.concat("<#" + it + "> ");
                } else {
                    Create.ChannelMonitor(eventGuild.getId(), it, 1);
                    complete = complete.concat("<#" + it + "> ");
                }
            } catch (Exception ex) {
                nonValid = nonValid.concat("\"" + it + "\" ");
            }
        }

        embed.setColor(0xffff00);
        if (!complete.isEmpty()) {
            embed.addField("Successfully monitored:", complete, false);
            embed.setColor(0x00ff00);
        }

        if (!monitored.isEmpty()) {
            embed.addField("Already being monitored:", monitored, false);
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
}
