package Thermostat.ThermoFunctions.Commands;

import Thermostat.Embeds;
import Thermostat.MySQL.Create;
import Thermostat.MySQL.DataSource;
import Thermostat.ThermoFunctions.Messages;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;

import static Thermostat.ThermoFunctions.Functions.parseMention;

/**
 * Command that when called, shows an embed
 * with the settings of a specific channel.
 */
public class Settings extends ListenerAdapter
{
    public void onGuildMessageReceived(GuildMessageReceivedEvent ev) {
        // gets given arguments and passes them to a list
        ArrayList<String> args = new ArrayList<>(Arrays.asList(ev.getMessage().getContentRaw().split("\\s+")));
        String embedString = "";

        if (
                args.get(0).equalsIgnoreCase(Thermostat.thermostat.prefix + "settings") ||
                        args.get(0).equalsIgnoreCase(Thermostat.thermostat.prefix + "s")
        ) {
            // checks if member sending request is a bot
            if (ev.getMember().getUser().isBot()) {
                return;
            }

            // checks if event member has permission
            if (!ev.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
                Messages.sendMessage(ev.getChannel(), Embeds.userNoPermission(ev.getAuthor().getId()));
                return;
            }

            if (args.size() == 1) {
                Messages.sendMessage(ev.getChannel(), Embeds.specifyChannel(ev.getAuthor().getId()));
                return;
            }

            args.remove(0);
            args.set(0, parseMention(args.get(0), "#"));

            // if channel doesn't exist, show error msg
            if (args.get(0).isBlank() || ev.getGuild().getTextChannelById(args.get(0)) == null)
            {
                Messages.sendMessage(ev.getChannel(), Embeds.channelNotFound(ev.getAuthor().getId()));
                return;
            }

            // connects to database and creates channel
            try {
                // silent guild adder
                if (!DataSource.checkDatabaseForData("SELECT * FROM GUILDS WHERE GUILD_ID = " + ev.getGuild().getId()))
                    Create.Guild(ev.getGuild().getId());

                {
                    int max = DataSource.queryInt("SELECT MAX_SLOW FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = " + args.get(0));

                    if (max == -1) {
                        Messages.sendMessage(ev.getChannel(), Embeds.noChannels());
                        return;
                    }

                    Messages.sendMessage(ev.getChannel(),
                            Embeds.channelSettings(ev.getGuild().getTextChannelById(args.get(0)).getName(),
                                    max,
                                    DataSource.queryInt("SELECT MIN_SLOW FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = " + args.get(0)),
                                    DataSource.queryBool("SELECT MONITORED FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = " + args.get(0))
                            )
                    );
                }

            } catch (Exception ex) {
                Logger lgr = LoggerFactory.getLogger(DataSource.class);
                lgr.error(ex.getMessage(), ex);
                Messages.sendMessage(ev.getChannel(), Embeds.fatalError());
            }
        }
    }
}
