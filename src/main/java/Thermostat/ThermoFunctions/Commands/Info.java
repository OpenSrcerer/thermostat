package Thermostat.ThermoFunctions.Commands;

import Thermostat.Embeds;
import Thermostat.ThermoFunctions.Messages;
import Thermostat.thermostat;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * <h1>Info</h1>
 * <p>
 * Class that manages the th!info command. Sends
 * an Info embed when th!info is called.
 */
public class Info extends ListenerAdapter
{
    private static ScheduledFuture<?> msgFuture;

    public void onGuildMessageReceived(GuildMessageReceivedEvent ev) {
        ArrayList<String> args = new ArrayList<>(Arrays.asList(ev.getMessage().getContentRaw().split("\\s+")));

        if (
                args.get(0).equalsIgnoreCase(Thermostat.thermostat.prefix + "info") ||
                        args.get(0).equalsIgnoreCase(Thermostat.thermostat.prefix + "i") ||
                        args.get(0).equalsIgnoreCase(Thermostat.thermostat.prefix + "help") ||
                        args.get(0).equalsIgnoreCase(Thermostat.thermostat.prefix + "h")
        ) {
            // checks if member sending request is a bot
            if (ev.getMember().getUser().isBot()) {
                return;
            }

            Consumer<Message> consumer = message -> {
                try {
                    Messages.addReaction(message, "🌡");
                    Messages.addReaction(message, "ℹ", 100, TimeUnit.MILLISECONDS);
                    Messages.addReaction(message, "❌", 200, TimeUnit.MILLISECONDS);
                } catch (PermissionException ex) {
                    return;
                }

                Runnable runnable = () -> messageThread(message.getId(), ev);

                Thread msgThread = new Thread(runnable);
                msgThread.start();
                msgFuture = Messages.deleteMessage(message, 100, TimeUnit.SECONDS);
            };
            Messages.sendMessage(ev.getChannel(), Embeds.getInfoSelection(), consumer);
        }
    }


    /**
     * Thread function that's used as a runnable to
     * monitor the Main Information Menu.
     * @param msgId The message ID of the info msg.
     * @param ev The event that represents the guild.
     */
    public static void messageThread(String msgId, GuildMessageReceivedEvent ev)
    {
        Consumer<Message>
                mainMenuConsumer = message -> {
            Messages.clearReactions(message);
            Messages.editMessage(message, Embeds.getInfoSelection().build());
            try {
                Messages.addReaction(message, "🌡");
                Messages.addReaction(message, "ℹ", 100, TimeUnit.MILLISECONDS);
                Messages.addReaction(message, "❌", 200, TimeUnit.MILLISECONDS);
            } catch (PermissionException ex) {
                return;
            }
            msgFuture.cancel(true);
            msgFuture = Messages.deleteMessage(message, 100, TimeUnit.SECONDS);
        },
                monitorInformationConsumer = message -> {
            try {
            Messages.clearReactions(message);
            Messages.editMessage(message, Embeds.getMonitorInfo().build());
            Messages.addReaction(message, "⬆");
            Messages.addReaction(message, "❌", 100, TimeUnit.MILLISECONDS);
            } catch (PermissionException ex) {
                return;
            }
            msgFuture.cancel(true);
            msgFuture = Messages.deleteMessage(message, 100, TimeUnit.SECONDS);
        },
                otherInformationConsumer = message -> {
            Messages.clearReactions(message);
            Messages.editMessage(message, Embeds.getOtherInfo().build());
            try {
            Messages.addReaction(message, "⬆");
            Messages.addReaction(message, "❌", 100, TimeUnit.MILLISECONDS);
            } catch (PermissionException ex) {
                return;
            }
            msgFuture.cancel(true);
            msgFuture = Messages.deleteMessage(message, 100, TimeUnit.SECONDS);
        },
                deleteMenuConsumer = message -> {
            msgFuture.cancel(true);
            Messages.deleteMessage(message);
        };

        // Loop that keeps going until the message gets deleted.
        // (Or until an error is thrown.)
        try {
            while (Messages.checkRetrieveMessageById(ev.getChannel(), msgId)) {

                // create list of reactions on the message
                List<MessageReaction> reactions = ev.getChannel().retrieveMessageById(msgId).complete().getReactions();

                // iterates through every reaction
                for (MessageReaction it : reactions) {
                    // gets users in that reaction
                    List<User> users = it.retrieveUsers().complete();
                    // if the reaction is a heavy check mark and the
                    // initiator of the command has clicked it, init
                    // removal procedure
                    if (
                            it.getReactionEmote().getEmoji().equals("🌡") &&
                                    users.contains(ev.getAuthor())
                    ) {
                        Messages.retrieveMessageById(ev.getChannel(), msgId, monitorInformationConsumer);
                    } else if (
                            it.getReactionEmote().getEmoji().equals("ℹ") &&
                                    users.contains(ev.getAuthor())
                    ) {
                        Messages.retrieveMessageById(ev.getChannel(), msgId, otherInformationConsumer);
                    } else if (
                            it.getReactionEmote().getEmoji().equals("⬆") &&
                                    users.contains(ev.getAuthor())
                    ) {
                        Messages.retrieveMessageById(ev.getChannel(), msgId, mainMenuConsumer);
                    } else if (
                            it.getReactionEmote().getEmoji().equals("❌") &&
                                    users.contains(ev.getAuthor())
                    ) {
                        Messages.retrieveMessageById(ev.getChannel(), msgId, deleteMenuConsumer);
                    } else {
                        // If the reaction doesn't contain the owner's reaction
                        // In all of the users that have reacted to the prompt message
                        for (User jt : users) {
                            // delete their reactions except thermostat's one
                            if (!jt.getId().equals(thermostat.thermo.getSelfUser().getId())) {
                                it.removeReaction(jt).queue();
                            }
                        }
                    }
                }
                Thread.sleep(500);
            }
            // if message is deleted, permission errors, etc.
        } catch (Exception ignored) {
        }
    }
}
