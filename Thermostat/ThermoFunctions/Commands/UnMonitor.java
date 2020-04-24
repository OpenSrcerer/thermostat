package Thermostat.ThermoFunctions.Commands;

import Thermostat.MySQL.Connection;
import Thermostat.MySQL.Delete;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static Thermostat.ThermoFunctions.Functions.parseMention;


/**
 * <h1>UnMonitor Command</h1>
 * <p>
 * Removes channels from the database provided in
 * {@link Connection}, upon user running the
 * command. Extends ListenerAdapter thus must
 * be added as a listener in {@link Thermostat.thermostat}.
 */
public class UnMonitor extends ListenerAdapter
{
    public void onGuildMessageReceived(GuildMessageReceivedEvent ev)
    {
        // gets given arguments and passes them to a list
        ArrayList<String> args = new ArrayList<>(Arrays.asList(ev.getMessage().getContentRaw().split("\\s+")));

        if (
                args.get(0).equalsIgnoreCase(Thermostat.thermostat.prefix + "unmonitor") ||
                args.get(0).equalsIgnoreCase(Thermostat.thermostat.prefix + "unmon") ||
                args.get(0).equalsIgnoreCase(Thermostat.thermostat.prefix + "um")
        ) {
            if (args.size() == 1) {
                ev.getChannel().sendMessage("<@" + ev.getAuthor().getId() + "> Please specify the channels to unmonitor.").queue();
                return;
            }

            // catch to remove command initation with prefix
            args.remove(0);

            // checks if event member has permission
            if (!ev.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
                ev.getChannel().sendMessage("<@" + ev.getAuthor().getId() + "> \nYou must have the `MANAGE_CHANNEL` permission in order to use this command.").queue();
                return;
            }

            String finalMessage = "<@" + ev.getAuthor().getId() + ">\n";

            // parses arguments into usable IDs, checks if channels exist
            // realindex - > for msg
            int realIndex = 1;
            for (int index = 0; index < args.size(); ++index) {
                // first check, if it's a channel mention then passes id instead
                args.set(index, parseMention(args.get(index), "#"));

                // if string is empty add a 0 to it in order to represent
                if (args.get(index).isBlank()) {
                    finalMessage = finalMessage.concat("Channel #" + realIndex + " is not a valid channel.\n");
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
                        finalMessage = finalMessage.concat("Category <#" + args.get(index) + "> does not contain any text channels.");
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
                    finalMessage = finalMessage.concat("Channel " + args.get(index) + " was not found in this guild.\n");
                    args.remove(index);
                    --index;
                }
                ++realIndex;
            }

            // connects to database and removes channel
            Connection conn = new Connection();

            for (String it : args) {
                try {
                    // checks db if channel exists
                    if (conn.checkDatabaseForData("SELECT * FROM CHANNELS WHERE CHANNEL_ID = " + it))
                    {
                        Delete.Channel(ev.getGuild().getId(), it);
                        finalMessage = finalMessage.concat("<#" + it + "> was successfully removed from the monitoring database.\n");
                    }
                    // if not, do not do anything
                    else
                        finalMessage = finalMessage.concat("Channel <#" + it + "> cannot be removed from monitoring if it is not being monitored.\n");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    finalMessage = finalMessage.concat("Channel " + it + " could not be removed.\n");
                }
            }

            conn.closeConnection();
            ev.getChannel().sendMessage(finalMessage).queue();
        }
    }
}
