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

public class Sensitivity {
    private static final EmbedBuilder embed = new EmbedBuilder();

    public static void execute(ArrayList<String> args, @Nonnull Guild eventGuild, @Nonnull TextChannel eventChannel, @Nonnull Member eventMember, String prefix) {

        // checks if event member has permission
        if (!eventMember.hasPermission(Permission.MANAGE_CHANNEL)) {
            Messages.sendMessage(eventChannel, Embeds.specifyChannels());
            return;
        }

        //
        if (args.size() < 2) {
            Messages.sendMessage(eventChannel, Embeds.helpSensitivity(prefix));
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
            Messages.sendMessage(eventChannel, Embeds.invalidSensitivity());
            return;
        }


        if (args.size() >= 2) {
            for (int index = 0; index < args.size() - 1; ++index) {
                try {
                    // silent guild adder
                    if (!DataSource.checkDatabaseForData("SELECT * FROM GUILDS WHERE GUILD_ID = " + eventGuild.getId()))
                        Create.Guild(eventGuild.getId());

                    // check db if channel exists and create it if not
                    if (!DataSource.checkDatabaseForData("SELECT * FROM CHANNELS WHERE CHANNEL_ID = " + args.get(index)))
                        Create.Channel(eventGuild.getId(), args.get(index), 0);

                    if (offset >= -10 && offset <= 10) {
                        DataSource.update("UPDATE CHANNEL_SETTINGS SET SENSOFFSET = " + (1f + offset / 20f) + " WHERE CHANNEL_ID = " + args.get(index));
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
                // silent guild adder
                if (!DataSource.checkDatabaseForData("SELECT * FROM GUILDS WHERE GUILD_ID = " + eventGuild.getId()))
                    Create.Guild(eventGuild.getId());

                // check db if channel exists and create it if not
                if (!DataSource.checkDatabaseForData("SELECT * FROM CHANNELS WHERE CHANNEL_ID = " + eventChannel.getId()))
                    Create.Channel(eventGuild.getId(), eventChannel.getId(), 0);

                if (offset >= -10 && offset <= 10) {
                    DataSource.update("UPDATE CHANNEL_SETTINGS SET SENSOFFSET = " + (1f + offset / 20f) + " WHERE CHANNEL_ID = " + eventChannel.getId());
                    complete = complete.concat("<#" + eventChannel.getId() + "> ");
                } else {
                    badSensitivity = badSensitivity.concat("<#" + eventChannel.getId() + "> ");
                }
            } catch (Exception ex) {
                Messages.sendMessage(eventChannel, Embeds.fatalError());
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
