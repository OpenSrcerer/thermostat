package Thermostat.ThermoFunctions.Commands;

import Thermostat.Embeds;
import Thermostat.MySQL.Connection;
import Thermostat.MySQL.Create;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static Thermostat.ThermoFunctions.Functions.parseMention;

/**
 * <h1>Monitor Command</h1>
 * <p>
 * Adds channels to the database provided in
 * {@link Connection}, upon user running the
 * command. Extends ListenerAdapter thus must
 * be added as a listener in {@link Thermostat.thermostat}.
 */
public class Monitor extends ListenerAdapter {
    private static EmbedBuilder embed = new EmbedBuilder();

    public void onGuildMessageReceived(GuildMessageReceivedEvent ev) {
        // gets given arguments and passes them to a list
        ArrayList<String> args = new ArrayList<>(Arrays.asList(ev.getMessage().getContentRaw().split("\\s+")));

        if (
                args.get(0).equalsIgnoreCase(Thermostat.thermostat.prefix + "monitor") ||
                args.get(0).equalsIgnoreCase(Thermostat.thermostat.prefix + "mon") ||
                args.get(0).equalsIgnoreCase(Thermostat.thermostat.prefix + "m")
        ) {
            if (args.size() == 1) {
                ev.getChannel().sendMessage(Embeds.specifyChannels(ev.getAuthor().getId()).build()).queue();
                return;
            }

            // catch to remove command initiation with prefix
            args.remove(0);

            // checks if event member has permission
            if (!ev.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
                ev.getChannel().sendMessage(Embeds.userNoPermission(ev.getAuthor().getId()).build()).queue();
                return;
            }

            embed.setTitle("ℹ Command Results:");

            // parses arguments into usable IDs, checks if channels exist
            // realIndex is just for the message.
            int realIndex = 1;
            for (int index = 0; index < args.size(); ++index) {
                // first check, if it's a channel mention then passes id instead
                args.set(index, parseMention(args.get(index), "#"));

                // if string is empty add a 0 to it in order to represent
                // empty channel
                if (args.get(index).isBlank()) {
                    embed.addField("", "Channel #" + realIndex + " is not a valid channel.", false);
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
                        embed.addField("", "Category <#" + args.get(index) + "> does not contain any text channels.", false);
                    }
                    // removes category ID from argument ArrayList
                    args.remove(index);
                    // iterates through every channel and adds its' id to the arg list
                    for (TextChannel it : TextChannels)
                    {
                        args.add(it.getId());
                    }
                    --index;
                }

                // removes element from arguments if it's not a valid channel ID
                else if (ev.getGuild().getTextChannelById(args.get(index)) == null) {
                    embed.addField("", "Text Channel " + args.get(index) + " was not found in this guild.", false);
                    args.remove(index);
                    --index;
                }

                ++realIndex;
            }

            // connects to database and creates channel
            Connection conn;
            try {
                conn = new Connection();
            }
            catch (SQLException ex)
            {
                ev.getChannel().sendMessage(Embeds.fatalError().build()).queue();
                ex.printStackTrace();
                return;
            }

            for (String it : args) {
                try {
                    // silent guild adder
                    if (!conn.checkDatabaseForData("SELECT * FROM GUILDS WHERE GUILD_ID = " + ev.getGuild().getId()))
                        Create.Guild(ev.getGuild().getId());
                    // check db if channel exists
                    if (!conn.checkDatabaseForData("SELECT * FROM CHANNELS WHERE CHANNEL_ID = " + it))
                    {
                        Create.Channel(ev.getGuild().getId(), it);
                        embed.addField("", "<#" + it + "> is now being monitored.\n", false);
                    }
                    else
                    {
                        embed.addField("", "Channel <#" + it + "> is already being monitored.", false);
                    }
                } catch (Exception ex) {
                ex.printStackTrace();
                    embed.addField("", "Channel " + it + " was not found in this guild.\n", false);
                }
            }

            conn.closeConnection();

            embed.setColor(0xeb9834);
            embed.addField("", "<@" + ev.getAuthor().getId() + ">", false);
            ev.getChannel().sendMessage(embed.build()).queue();
            embed.clear();
        }
    }
}
