package Thermostat.ThermoFunctions.Commands;

import Thermostat.Embeds;
import Thermostat.MySQL.Connection;
import Thermostat.MySQL.Create;
import Thermostat.MySQL.Delete;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <h1>UnMonitorAll Command</h1>
 * <p>
 * Removes all channels from monitoring.
 */
public class UnMonitorAll extends ListenerAdapter
{
    public void onGuildMessageReceived(GuildMessageReceivedEvent ev)
    {
        // gets given arguments and passes them to a list
        ArrayList<String> args = new ArrayList<>(Arrays.asList(ev.getMessage().getContentRaw().split("\\s+")));

        if (
                args.get(0).equalsIgnoreCase(Thermostat.thermostat.prefix + "unmonitorall") ||
                        args.get(0).equalsIgnoreCase(Thermostat.thermostat.prefix + "unmonall") ||
                        args.get(0).equalsIgnoreCase(Thermostat.thermostat.prefix + "uma")
        ) {
            // checks if event member has permission
            if (!ev.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
                ev.getChannel().sendMessage(Embeds.userNoPermission(ev.getAuthor().getId()).build()).queue();
                return;
            }

            // - prompt block for task
            Message confirmationMessage = ev.getChannel().sendMessage(
                    Embeds.promptEmbed(ev.getAuthor().getId()).build())
                    .complete();
            confirmationMessage.addReaction("✔").queue();

            Runnable runnable = () -> confirmReaction(confirmationMessage.getId(), ev);
            Thread t = new Thread(runnable);
            t.start();
        }
    }

    /**
     * Creates a new thread in order to manage a message prompt.
     * @param msg Bot promt message id.
     * @param ev Event called when the th!unmonitorall command
     *           was called.
     */
    public static void confirmReaction (String msg, GuildMessageReceivedEvent ev)
    {
        // runs the check 20 times (10 seconds, every 500ms)
        for (int index = 0; index < 20; ++index)
        {
            // create list of reactions on the message
            List<MessageReaction> reactions = ev.getChannel().retrieveMessageById(msg).complete().getReactions();
            // iterates through every reaction
            for (MessageReaction it : reactions)
            {
                // gets users in that reaction
                List<User> users = it.retrieveUsers().complete();
                // if the reaction is a heavy check mark and the
                // initiator of the command has clicked it, init
                // removal procedure
                if (
                        it.getReactionEmote().getEmoji().equals("✔") &&
                                users.contains(ev.getAuthor())
                )
                {
                    // connects to database and removes channel
                    Connection conn = null;

                    try {
                        conn = new Connection();
                        // silent guild adder (to not cause conflicts)
                        if (!conn.checkDatabaseForData("SELECT * FROM GUILDS WHERE GUILD_ID = " + ev.getGuild().getId()))
                            Create.Guild(ev.getGuild().getId());
                        // checks db if channel exists
                        if (conn.checkDatabaseForData("SELECT * FROM CHANNELS WHERE GUILD_ID = " + ev.getGuild().getId()))
                        {
                            ResultSet rs = conn.query("SELECT CHANNEL_ID FROM CHANNELS WHERE GUILD_ID = " + ev.getGuild().getId());

                            while (rs.next())
                            {
                                Delete.Channel(ev.getGuild().getId(), rs.getString(1));
                            }

                            ev.getChannel().sendMessage(Embeds.allRemoved().build()).queue();
                            conn.closeConnection();
                            return;
                        }
                        // if not, do not do anything
                        else
                        {
                            ev.getChannel().sendMessage(Embeds.noChannels(ev.getAuthor().getId()).build()).queue();
                            conn.closeConnection();
                            return;
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        ev.getChannel().sendMessage(Embeds.fatalError().build()).queue();
                        conn.closeConnection();
                        return;
                    }
                }
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException ex)
            {
                ex.printStackTrace();
                System.out.println("Main Thread Interrupted.");
                ev.getChannel().sendMessage(Embeds.fatalError().build()).queue();
            }

        }
        ev.getChannel().sendMessage(Embeds.missedPrompt().build()).queue();
    }
}
