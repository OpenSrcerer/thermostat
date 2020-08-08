package Thermostat.ThermoFunctions.Commands;

import Thermostat.Embeds;
import Thermostat.MySQL.Create;
import Thermostat.MySQL.DataSource;
import Thermostat.ThermoFunctions.Messages;
import Thermostat.thermostat;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static Thermostat.ThermoFunctions.Functions.parseMention;

public class Sensitivity extends ListenerAdapter
{
    private static EmbedBuilder embed = new EmbedBuilder();

    public void onGuildMessageReceived(GuildMessageReceivedEvent ev) {
        // gets guild prefix from database. if it doesn't have one, use default
        String prefix = DataSource.queryString("SELECT GUILD_PREFIX FROM GUILDS WHERE GUILD_ID = " + ev.getGuild().getId());
        if (prefix == null) { prefix = thermostat.prefix; }

        // gets given arguments and passes them to a list
        ArrayList<String> args = new ArrayList<>(Arrays.asList(ev.getMessage().getContentRaw().split("\\s+")));

        if (
                args.get(0).equalsIgnoreCase(prefix + "sensitivity") ||
                args.get(0).equalsIgnoreCase(prefix + "sens")
        ) {
            // checks if member sending request is a bot
            if (ev.getMember().getUser().isBot()) {
                return;
            }

            // checks if event member has permission
            if (!ev.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
                Messages.sendMessage(ev.getChannel(), Embeds.userNoPermission());
                return;
            }

            //
            if (args.size() < 2) {
                Messages.sendMessage(ev.getChannel(), Embeds.helpSensitivity(prefix));
                return;
            }

            // catch to remove command initiation with prefix
            args.remove(0);

            String nonValid = "",
                    noText = "",
                    complete = "",
                    badSensitivity = "";
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

                if (args.get(index).isBlank()) {
                    nonValid = nonValid.concat("\"" + originalArgument + "\" ");
                    args.remove(index);
                    removed = true;
                    --index;
                }

                // if given argument is a category get channels from it
                // and pass them to the arguments ArrayList
                else if (ev.getGuild().getCategoryById(args.get(index)) != null) {
                    // firstly creates an immutable list of the channels in the category
                    List<TextChannel> TextChannels = ev.getGuild().getCategoryById(args.get(index)).getTextChannels();
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
                else if (ev.getGuild().getTextChannelById(args.get(index)) == null) {
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
                Messages.sendMessage(ev.getChannel(), Embeds.invalidSensitivity());
                return;
            }


            if (args.size() >= 2)
            {
                for (int index = 0; index < args.size() - 1; ++index) {
                    try {
                        // silent guild adder
                        if (!DataSource.checkDatabaseForData("SELECT * FROM GUILDS WHERE GUILD_ID = " + ev.getGuild().getId()))
                            Create.Guild(ev.getGuild().getId());

                        // check db if channel exists and create it if not
                        if (!DataSource.checkDatabaseForData("SELECT * FROM CHANNELS WHERE CHANNEL_ID = " + args.get(index)))
                            Create.Channel(ev.getGuild().getId(), args.get(index), 0);

                        if (offset >= -10 && offset <= 10)
                        {
                            DataSource.update("UPDATE CHANNEL_SETTINGS SET SENSOFFSET = " + (1f + offset/20f) + " WHERE CHANNEL_ID = " + args.get(index));
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
                    if (!DataSource.checkDatabaseForData("SELECT * FROM GUILDS WHERE GUILD_ID = " + ev.getGuild().getId()))
                        Create.Guild(ev.getGuild().getId());

                    // check db if channel exists and create it if not
                    if (!DataSource.checkDatabaseForData("SELECT * FROM CHANNELS WHERE CHANNEL_ID = " + ev.getChannel().getId()))
                        Create.Channel(ev.getGuild().getId(), ev.getChannel().getId(), 0);

                    if (offset >= -10 && offset <= 10)
                    {
                        DataSource.update("UPDATE CHANNEL_SETTINGS SET SENSOFFSET = " + (1f + offset/20f) + " WHERE CHANNEL_ID = " + ev.getChannel().getId());
                        complete = complete.concat("<#" + ev.getChannel().getId() + "> ");
                    } else {
                        badSensitivity = badSensitivity.concat("<#" + ev.getChannel().getId() + "> ");
                    }
                } catch (Exception ex) {
                    Messages.sendMessage(ev.getChannel(), Embeds.fatalError());
                    return;
                }
            }

            embed.setColor(0xffff00);
            if (!complete.isEmpty())
            {
                embed.addField("Channels given a new sensitivity of " + offset + ":", complete, false);
                embed.setColor(0x00ff00);
            }

            if (!badSensitivity.isEmpty())
            {
                embed.addField("Offset value was not appropriate:", badSensitivity, false);
            }

            if (!nonValid.isEmpty())
            {
                embed.addField("Channels that were not valid or found:", nonValid, false);
            }

            if (!noText.isEmpty())
            {
                embed.addField("Categories with no Text Channels:", noText, false);
            }

            embed.setTimestamp(Instant.now());
            embed.setFooter("Requested by " + ev.getAuthor().getAsTag(), thermostat.thermo.getSelfUser().getAvatarUrl());
            Messages.sendMessage(ev.getChannel(), embed);

            embed.clear();
        }
    }
}
