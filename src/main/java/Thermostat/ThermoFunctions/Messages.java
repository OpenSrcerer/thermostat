package Thermostat.ThermoFunctions;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

/**
 * Deals with InsufficientPermissionExceptions thrown
 * by a lack of MESSAGE_WRITE permission, when sending
 * messages.
 */
public class Messages
{
    /**
     * Sends an embed to a designated channel with error catching.
     * @param channel Channel to send the embed in.
     * @param eb The embed to send.
     */
    public static void sendMessage(TextChannel channel, EmbedBuilder eb)
    {
        if (channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE))
        {
            try {
                channel.sendMessage(eb.build()).queue();
            } catch (InsufficientPermissionException ex) {
                sendMessage(channel, "Please add the \"Embed Links\" permission to the bot in order to get command results!");
            }
        }
    }

    /**
     * Sends a text message to a designated channel with error catching.
     * @param channel Channel to send the message in.
     * @param msg The message to send.
     */
    public static void sendMessage(TextChannel channel, String msg)
    {
        if (channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE))
        {
            try {
                channel.sendMessage(msg).queue();
            } catch (InsufficientPermissionException ex) {
                System.out.println(channel.getGuild().getName() + " - Permission error when trying to send message.");
            }
        }
    }
}
