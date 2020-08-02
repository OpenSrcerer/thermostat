package Thermostat.ThermoFunctions.Commands;

import Thermostat.Embeds;
import Thermostat.MySQL.DataSource;
import Thermostat.ThermoFunctions.Messages;
import Thermostat.thermostat;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

public class Prefix extends ListenerAdapter {
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent ev) {
        ArrayList<String> args = new ArrayList<>(Arrays.asList(ev.getMessage().getContentRaw().split("\\s+")));

        if (ev.getMember() != null) {
            // checks if member sending request is a bot
            if (ev.getMember().getUser().isBot()) {
                return;
            }
        } else {
            return;
        }

        // gets guild prefix from database. if it doesn't have one, use default
        String prefix = DataSource.queryString("SELECT GUILD_PREFIX FROM GUILDS WHERE GUILD_ID = " + ev.getGuild().getId());
        if (prefix == null) { prefix = thermostat.prefix; }

        if (
                args.get(0).equalsIgnoreCase(prefix + "prefix") ||
                args.get(0).equalsIgnoreCase(prefix + "p")
        ) {
            args.remove(0);

            try {
                prefixAction(args, ev.getChannel(), ev.getAuthor().getAsTag(), ev.getGuild().getId(), prefix);
            } catch (SQLException ex) {
                Messages.sendMessage(ev.getChannel(), Embeds.fatalError("Try setting the prefix again."));
            }
            return;
        }

        if (args.size() == 1) { return; }

        if (
                (args.get(0).equalsIgnoreCase("<@!" + thermostat.thermo.getSelfUser().getId() + ">") && args.get(1).equalsIgnoreCase("prefix")) ||
                (args.get(0).equalsIgnoreCase("<@!" + thermostat.thermo.getSelfUser().getId() + ">") && args.get(1).equalsIgnoreCase("p"))
        ) {
            args.remove(0);
            args.remove(0);

            try {
                prefixAction(args, ev.getChannel(), ev.getAuthor().getAsTag(), ev.getGuild().getId(), prefix);
            } catch (SQLException ex) {
                Messages.sendMessage(ev.getChannel(), Embeds.fatalError("Try setting the prefix again."));
            }
        }
    }

    /**
     * Code to run when the command is called.
     * @param args User input.
     * @param channel Channel where command was called.
     * @param author Author of event.
     * @param guildId ID of event guild.
     * @param currentPrefix Current prefix of thermostat.
     * @throws SQLException If some error went wrong with the DB conn.
     */
    public void prefixAction(ArrayList<String> args, TextChannel channel, String author, String guildId, String currentPrefix) throws SQLException {
        if (args.size() > 1 && args.get(0).equalsIgnoreCase("set")) {
            if (Pattern.matches("[!-~]*", args.get(1)) && args.get(1).length() <= 10) {
                Messages.sendMessage(channel, Embeds.setPrefix(author, args.get(1)));
                DataSource.update("UPDATE GUILDS SET GUILD_PREFIX = '" + args.get(1) + "' WHERE GUILD_ID = " + guildId);
            } else {
                Messages.sendMessage(channel, Embeds.incorrectPrefix());
            }
        } else if (args.size() == 1 && args.get(0).equalsIgnoreCase("set")) {
            Messages.sendMessage(channel, Embeds.insertPrefix());
        } else if (args.size() >= 1 && args.get(0).equalsIgnoreCase("reset")) {
            DataSource.update("UPDATE GUILDS SET GUILD_PREFIX = NULL WHERE GUILD_ID = " + guildId);
            Messages.sendMessage(channel, Embeds.resetPrefix());
        } else {
            Messages.sendMessage(channel, Embeds.getPrefix(author, currentPrefix));
        }
    }
}
