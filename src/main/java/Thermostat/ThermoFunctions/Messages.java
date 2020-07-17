package Thermostat.ThermoFunctions;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Deals with InsufficientPermissionExceptions thrown
 * by a lack of MESSAGE_WRITE permission, when sending
 * messages. Also controls timed message deletion to
 * prevent spam.
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
            // send message and delete after 100 seconds
            try {
                Consumer<Message> consumer = message -> message.delete().queueAfter(100, TimeUnit.SECONDS);
                channel.sendMessage(eb.build()).queue(consumer);
            } catch (InsufficientPermissionException ex) {
                sendMessage(channel, "Please add the \"**Embed Links**\" permission to the bot in order to get command results!");
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
                channel.sendMessage(msg).queueAfter(100, TimeUnit.SECONDS);
            } catch (InsufficientPermissionException ex) {
                System.out.println(channel.getGuild().getName() + " - Permission error when trying to send message.");
            }
        }
    }
}
