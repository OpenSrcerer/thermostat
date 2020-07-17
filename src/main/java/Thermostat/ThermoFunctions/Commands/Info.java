package Thermostat.ThermoFunctions.Commands;

import Thermostat.Embeds;
import Thermostat.thermostat;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * <h1>Info</h1>
 * <p>
 * Class that manages the th!info command. Sends
 * an Info embed when th!info is called.
 */
public class Info extends ListenerAdapter
{
    public static ScheduledFuture<?> msgFuture;

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
                message.addReaction("🌡").queue();
                message.addReaction("ℹ").queueAfter(100, TimeUnit.MILLISECONDS);
                message.addReaction("❌").queueAfter(200, TimeUnit.MILLISECONDS);

                Runnable runnable = () -> messageThread(message.getId(), ev);

                Thread msgThread = new Thread(runnable);
                msgThread.start();
                msgFuture = message.delete().queueAfter(100, TimeUnit.SECONDS);
            };
            ev.getChannel().sendMessage(Embeds.getInfoSelection().build()).queue(consumer);
        }
    }


    /**
     * Thread function that's used as a runnable to
     * monitor the Main Information Menu.
     * @param msgId The message ID of the info msg.
     * @param ev The event that represents the guild.
     */
    public static void messageThread(String msgId, GuildMessageReceivedEvent ev) {
        Consumer<Message>
                mainMenuConsumer = message -> {
            message.clearReactions().queue();
            message.editMessage(Embeds.getInfoSelection().build()).queue();
            message.addReaction("🌡").queue();
            message.addReaction("ℹ").queueAfter(100, TimeUnit.MILLISECONDS);
            message.addReaction("❌").queueAfter(200, TimeUnit.MILLISECONDS);
            msgFuture.cancel(true);
            msgFuture = message.delete().queueAfter(100, TimeUnit.SECONDS);
        },
                monitorInformationConsumer = message -> {
            message.clearReactions().queue();
            message.editMessage(Embeds.getMonitorInfo().build()).queue();
            message.addReaction("⬆").queue();
            message.addReaction("❌").queueAfter(100, TimeUnit.MILLISECONDS);
            msgFuture.cancel(true);
            msgFuture = message.delete().queueAfter(100, TimeUnit.SECONDS);
        },
                otherInformationConsumer = message -> {
            message.clearReactions().queue();
            message.editMessage(Embeds.getOtherInfo().build()).queue();
            message.addReaction("⬆").queue();
            message.addReaction("❌").queueAfter(100, TimeUnit.MILLISECONDS);
            msgFuture.cancel(true);
            msgFuture = message.delete().queueAfter(100, TimeUnit.SECONDS);
        },
                deleteMenuConsumer = message -> {
            msgFuture.cancel(true);
            message.delete().queue();
        };

        try {
            while (true) {
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
                        ev.getChannel().retrieveMessageById(msgId).queue(monitorInformationConsumer);
                    } else if (
                            it.getReactionEmote().getEmoji().equals("ℹ") &&
                                    users.contains(ev.getAuthor())
                    ) {
                        ev.getChannel().retrieveMessageById(msgId).queue(otherInformationConsumer);
                    } else if (
                            it.getReactionEmote().getEmoji().equals("⬆") &&
                                    users.contains(ev.getAuthor())
                    ) {
                        ev.getChannel().retrieveMessageById(msgId).queue(mainMenuConsumer);
                    } else if (
                            it.getReactionEmote().getEmoji().equals("❌") &&
                                    users.contains(ev.getAuthor())
                    ) {
                        ev.getChannel().retrieveMessageById(msgId).queue(deleteMenuConsumer);
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
        } catch (ErrorResponseException | InterruptedException ignored) {
        }
    }
}
