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

/**
 * Adds channels to the database provided in
 * db.properties, upon user running the
 * command. Extends ListenerAdapter thus must
 * be added as a listener in {@link Thermostat.thermostat}.
 */
public class Monitor extends ListenerAdapter {
    private static EmbedBuilder embed = new EmbedBuilder();

    public void onGuildMessageReceived(GuildMessageReceivedEvent ev) {
        // gets guild prefix from database. if it doesn't have one, use default
        String prefix = DataSource.queryString("SELECT GUILD_PREFIX FROM GUILDS WHERE GUILD_ID = " + ev.getGuild().getId());
        if (prefix == null) { prefix = thermostat.prefix; }

        // gets given arguments and passes them to a list
        ArrayList<String> args = new ArrayList<>(Arrays.asList(ev.getMessage().getContentRaw().split("\\s+")));

        if (
                args.get(0).equalsIgnoreCase(prefix + "monitor") ||
                args.get(0).equalsIgnoreCase(prefix + "mon") ||
                args.get(0).equalsIgnoreCase(prefix + "m")
        ) {
            // checks if member sending request is a bot
            if (ev.getMember().getUser().isBot()) {
                return;
            }

            if (args.size() == 1) {
                Messages.sendMessage(ev.getChannel(), Embeds.specifyChannels());
                return;
            }

            // catch to remove command initiation with prefix
            args.remove(0);

            // checks if event member has permission
            if (!ev.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
                Messages.sendMessage(ev.getChannel(), Embeds.userNoPermission());
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

                if (args.get(index).isBlank()) {
                    nonValid = nonValid.concat("\"" + originalArgument + "\" ");
                    args.remove(index);
                    --index;
                }

                // if given argument is a category get channels from it
                // and pass them to the arguments ArrayList
                else if (ev.getGuild().getCategoryById(args.get(index)) != null) {
                    // firstly creates an immutable list of the channels in the category
                    List<TextChannel> TextChannels = ev.getGuild().getCategoryById(args.get(index)).getTextChannels();
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
                else if (ev.getGuild().getTextChannelById(args.get(index)) == null) {
                    nonValid = nonValid.concat("\"" + args.get(index) + "\" ");
                    args.remove(index);
                    --index;
                }
            }

            // connects to database and creates channel
            for (String it : args) {
                try {
                    // silent guild adder
                    if (!DataSource.checkDatabaseForData("SELECT * FROM GUILDS WHERE GUILD_ID = " + ev.getGuild().getId()))
                        Create.Guild(ev.getGuild().getId());
                    // check db if channel exists
                    if (!DataSource.checkDatabaseForData("SELECT * FROM CHANNELS WHERE CHANNEL_ID = " + it)) {
                        Create.Channel(ev.getGuild().getId(), it, 1);
                        complete = complete.concat("<#" + it + "> ");
                    } else {
                        // checks whether the channel has the monitor
                        // value on the database set to 1
                        // table CHANNEL_SETTINGS
                        if (DataSource.queryBool("SELECT MONITORED FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = " + it))
                        {
                            monitored = monitored.concat("<#" + it + "> ");
                        } else {
                            Create.ChannelMonitor(ev.getGuild().getId(), it, 1);
                            complete = complete.concat("<#" + it + "> ");
                        }
                    }
                } catch (Exception ex) {
                    nonValid = nonValid.concat("\"" + it + "\" ");
                }
            }

            embed.setColor(0xffff00);
            if (!complete.isEmpty())
            {
                embed.addField("Successfully monitored:", complete, false);
                embed.setColor(0x00ff00);
            }

            if (!monitored.isEmpty())
            {
                embed.addField("Already being monitored:", monitored, false);
                embed.setColor(0x00ff00);
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
