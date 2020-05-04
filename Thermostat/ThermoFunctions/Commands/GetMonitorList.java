package Thermostat.ThermoFunctions.Commands;

import Thermostat.Embeds;
import Thermostat.MySQL.Connection;
import Thermostat.MySQL.Create;
import Thermostat.ThermoFunctions.Messages;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * <h1>GetMonitor Command</h1>
 * <p>
 * Class for the th!getmonitor command call.
 * Retrieves all the currently monitored channels
 * and sends them as a single embed in the channel
 * where the command was called.
 */
public class GetMonitorList extends ListenerAdapter {
    // dynamic embed that gets posted
    private static EmbedBuilder embed = new EmbedBuilder();

    /**
     * JDA Event Listener that grabs any message sent in a Guild.
     * @param ev The called event.
     */
    public void onGuildMessageReceived(GuildMessageReceivedEvent ev) {
        // gets given arguments and passes them to a list
        ArrayList<String> args = new ArrayList<>(Arrays.asList(ev.getMessage().getContentRaw().split("\\s+")));
        String embedString = "";

        if (
                args.get(0).equalsIgnoreCase(Thermostat.thermostat.prefix + "getmonitor") ||
                        args.get(0).equalsIgnoreCase(Thermostat.thermostat.prefix + "getmon") ||
                        args.get(0).equalsIgnoreCase(Thermostat.thermostat.prefix + "gm")
        ) {
            // checks if event member has permission
            if (!ev.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
                Messages.sendMessage(ev.getChannel(), Embeds.userNoPermission(ev.getAuthor().getId()));
                return;
            }

            embed.setTitle("ℹ Channels currently being monitored:");

            // connects to database and creates channel
            Connection conn;
            try {
                conn = new Connection();
            }
            catch (SQLException ex)
            {
                Messages.sendMessage(ev.getChannel(), Embeds.fatalError());
                ex.printStackTrace();
                return;
            }

            try {
                // silent guild adder
                if (!conn.checkDatabaseForData("SELECT * FROM GUILDS WHERE GUILD_ID = " + ev.getGuild().getId()))
                    Create.Guild(ev.getGuild().getId());
                ResultSet rs = conn.query("SELECT CHANNEL_ID FROM CHANNELS WHERE GUILD_ID = " + ev.getGuild().getId());

                if (!rs.next())
                {
                    embed.addField("", "None.", false);
                }
                else
                {
                    do
                    {
                        embedString = embedString.concat("<#" + ev.getGuild().getTextChannelById(rs.getString(1)).getId() + "> ");
                    } while (rs.next());
                }
                rs.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                Messages.sendMessage(ev.getChannel(), Embeds.fatalError());
            }

            conn.closeConnection();

            if (!embedString.isEmpty())
                embed.addField("", embedString, true);

            embed.setColor(0xeb9834);
            embed.addField("", "<@" + ev.getAuthor().getId() + ">", false);
            Messages.sendMessage(ev.getChannel(), embed);
            embed.clear();
        }
    }
}