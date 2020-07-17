package Thermostat.ThermoFunctions.Commands;

import Thermostat.Embeds;
import Thermostat.MySQL.Create;
import Thermostat.MySQL.DataSource;
import Thermostat.ThermoFunctions.Messages;
import Thermostat.thermostat;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

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
            // checks if member sending request is a bot
            if (ev.getMember().getUser().isBot()) {
                return;
            }

            // checks if event member has permission
            if (!ev.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
                Messages.sendMessage(ev.getChannel(), Embeds.specifyChannels());
                return;
            }

            if (!ev.getGuild().getSelfMember().hasPermission(ev.getChannel(), Permission.MESSAGE_HISTORY))
            {
                Messages.sendMessage(ev.getChannel(), Embeds.insufficientReact("Read Message History"));
                return;
            }
            else if (!ev.getGuild().getSelfMember().hasPermission(ev.getChannel(), Permission.MESSAGE_ADD_REACTION))
            {
                Messages.sendMessage(ev.getChannel(), Embeds.insufficientReact());
                return;
            }

            // Custom consumer in order to also add reaction
            // and start the monitoring thread
            Consumer<Message> consumer = message -> {
                message.addReaction("☑").queue();
                Runnable runnable = () -> confirmReaction(message.getId(), ev);
                Thread msgThread = new Thread(runnable);
                msgThread.start();
                message.delete().queueAfter(100, TimeUnit.SECONDS);
            };
            ev.getChannel().sendMessage(Embeds.promptEmbed(ev.getAuthor().getAsTag()).build()).queue(consumer);
        }
    }

    /**
     * Creates a new thread in order to manage a message prompt.
     * @param msg Bot prompt message id.
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
                        it.getReactionEmote().getEmoji().equals("☑") &&
                                users.contains(ev.getAuthor())
                )
                {
                    // connects to database and removes channel
                    try {
                        // silent guild adder (to not cause conflicts)
                        if (!DataSource.checkDatabaseForData("SELECT * FROM GUILDS WHERE GUILD_ID = " + ev.getGuild().getId()))
                            Create.Guild(ev.getGuild().getId());
                        // checks db if channel exists
                        if (DataSource.checkDatabaseForData("SELECT * FROM CHANNELS WHERE GUILD_ID = " + ev.getGuild().getId()))
                        {
                            List<String> channelsToUnMonitor = DataSource.query("SELECT CHANNEL_ID FROM CHANNELS WHERE GUILD_ID = " + ev.getGuild().getId());

                            for (String jt : channelsToUnMonitor)
                            {
                                Create.ChannelMonitor(ev.getGuild().getId(), jt, 0);
                            }
                            Messages.sendMessage(ev.getChannel(), Embeds.allRemoved(ev.getAuthor().getAsTag()));
                            return;
                        }
                        // if not, do not do anything
                        else
                        {
                            Messages.sendMessage(ev.getChannel(), Embeds.noChannels());
                            return;
                        }

                    } catch (Exception ex) {
                        Logger lgr = LoggerFactory.getLogger(DataSource.class);
                        lgr.error(ex.getMessage(), ex);
                        Messages.sendMessage(ev.getChannel(), Embeds.fatalError());
                        return;
                    }
                } else {
                    // If the reaction doesn't contain the owner's reaction
                    // In all of the users that have reacted to the prompt message
                    for (User jt : users)
                    {
                        // delete their reactions except thermostat's one
                        if (!jt.getId().equals(thermostat.thermo.getSelfUser().getId()))
                        {
                            it.removeReaction(jt).queue();
                        }
                    }
                }
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException ex)
            {
                Logger lgr = LoggerFactory.getLogger(DataSource.class);
                lgr.error(ex.getMessage(), ex);
                Messages.sendMessage(ev.getChannel(), Embeds.fatalError());
            }

        }
        Messages.sendMessage(ev.getChannel(), Embeds.missedPrompt());
    }
}
