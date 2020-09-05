package thermostat.thermoFunctions.commands.monitoring;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Removes channels from the database provided in
 * db.properties, upon user running the
 * command.
 */
public class UnMonitor {
    private static final EmbedBuilder embed = new EmbedBuilder();

    public static void execute(ArrayList<String> args, @Nonnull Guild eventGuild, @Nonnull TextChannel eventChannel, @Nonnull Member eventMember) {

        if (args.size() == 1) {
            Messages.sendMessage(eventChannel, Embeds.specifyChannels());
            return;
        }

        // catch to remove command initation with prefix
        args.remove(0);

        // checks if event member has permission
        if (!eventMember.hasPermission(Permission.MANAGE_CHANNEL)) {
            Messages.sendMessage(eventChannel, Embeds.userNoPermission("MANAGE_CHANNEL"));
            return;
        }

        String nonValid = "",
                noText = "",
                complete = "",
                unmonitored = "";

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

            // if given argument is a category get channels from it
            // and pass them to the arguments ArrayList
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

        // connects to database and removes channel
        for (String it : args) {
            try {
                // silent guild adder
                if (!DataSource.checkDatabaseForData("SELECT * FROM GUILDS WHERE GUILD_ID = ?", eventGuild.getId()))
                    Create.Guild(eventGuild.getId());
                // checks db if channel exists
                if (DataSource.checkDatabaseForData("SELECT * FROM CHANNELS JOIN CHANNEL_SETTINGS " +
                        "ON (CHANNELS.CHANNEL_ID = CHANNEL_SETTINGS.CHANNEL_ID) WHERE CHANNELS.CHANNEL_ID = ?" +
                        " AND CHANNEL_SETTINGS.MONITORED = 1", it)) {
                    Create.ChannelMonitor(eventGuild.getId(), it, 0);
                    complete = complete.concat("<#" + it + "> ");
                }
                // if not, do not do anything
                else
                    unmonitored = unmonitored.concat("<#" + it + "> ");
            } catch (Exception ex) {
                Logger lgr = LoggerFactory.getLogger(DataSource.class);
                lgr.error(ex.getMessage(), ex);
                Messages.sendMessage(eventChannel, Embeds.fatalError());
            }
        }

        embed.setColor(0xffff00);
        if (!complete.isEmpty()) {
            embed.addField("Successfully unmonitored:", complete, false);
            embed.setColor(0x00ff00);
        }

        if (!unmonitored.isEmpty()) {
            embed.addField("Already were not being monitored:", unmonitored, false);
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
