package Thermostat.ThermoFunctions.Listeners;

import Thermostat.Embeds;
import Thermostat.MySQL.Create;
import Thermostat.MySQL.DataSource;
import Thermostat.ThermoFunctions.Commands.Objects.MenuType;
import Thermostat.ThermoFunctions.Commands.Objects.MonitoredMessage;
import Thermostat.ThermoFunctions.Messages;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static Thermostat.ThermoFunctions.Commands.Objects.MonitoredMessage.monitoredMessages;

public class ReactionAddEvent extends ListenerAdapter
{
    public void onGuildMessageReactionAdd (@NotNull GuildMessageReactionAddEvent ev) {
        for (MonitoredMessage it : monitoredMessages)
        {
            matchInfoReaction(it, ev);
            matchUnmonitorallReaction(it, ev);
        }
    }

    /**
     * Checks if new reactions have been added in
     * a MonitoredMessage and applies an appropriate action.
     * @param monitoredMessage The monitored message.
     * @param ev The event of the reaction being added.
     */
    public void matchInfoReaction(MonitoredMessage monitoredMessage, GuildMessageReactionAddEvent ev)
    {
        // if reaction event is applied on the message
        // that is being monitored for reactions, and
        // the user ID matches the sender of the message
        if (
                ev.getMessageId().equals(monitoredMessage.getMessageId()) &&
                ev.getUserId().equals(monitoredMessage.getMessageOwner())
        ) {
            try {
                switch (ev.getReactionEmote().getEmoji()) {
                    case "🌡":
                        // Monitored Functions Menu
                        if (monitoredMessage.getMenuType() == MenuType.SELECTION) {
                            monitoredMessage.resetDestructionTimer(ev.getChannel());
                            Messages.editMessage(ev.getChannel(), monitoredMessage.getMessageId(), Embeds.getMonitorInfo().build());
                            monitoredMessage.setMenuType(MenuType.MONITOR);
                            try {
                                Messages.clearReactions(ev.getChannel(), monitoredMessage.getMessageId());
                                Messages.addReactions(ev.getChannel(), monitoredMessage.getMessageId(), Arrays.asList("⬆", "❌"));
                            } catch (PermissionException ignored) {}
                        }
                        break;
                    case "ℹ":
                        // Informational Menu
                        if (monitoredMessage.getMenuType() == MenuType.SELECTION) {
                            monitoredMessage.resetDestructionTimer(ev.getChannel());
                            Messages.editMessage(ev.getChannel(), monitoredMessage.getMessageId(), Embeds.getOtherInfo().build());
                            monitoredMessage.setMenuType(MenuType.OTHER);
                            try {
                                Messages.clearReactions(ev.getChannel(), monitoredMessage.getMessageId());
                                Messages.addReactions(ev.getChannel(), monitoredMessage.getMessageId(), Arrays.asList("⬆", "❌"));
                            } catch (PermissionException ignored) {}
                        }
                        break;
                    case "⬆":
                        // Exit Menu Function
                        if (monitoredMessage.getMenuType() != MenuType.SELECTION) {
                            monitoredMessage.resetDestructionTimer(ev.getChannel());
                            Messages.editMessage(ev.getChannel(), monitoredMessage.getMessageId(), Embeds.getInfoSelection().build());
                            monitoredMessage.setMenuType(MenuType.SELECTION);
                            try {
                                Messages.clearReactions(ev.getChannel(), monitoredMessage.getMessageId());
                                Messages.addReactions(ev.getChannel(), monitoredMessage.getMessageId(), Arrays.asList("🌡", "ℹ", "❌"));
                            } catch (PermissionException ignored) {}
                        }
                        break;
                    case "❌":
                        // Close Menu
                        monitoredMessage.invalidate();
                        Messages.deleteMessage(ev.getChannel(), monitoredMessage.getMessageId());
                        break;
                }
            } catch (IllegalStateException ignored) {}
        }
    }

    public void matchUnmonitorallReaction(MonitoredMessage monitoredMessage, GuildMessageReactionAddEvent ev)
    {
        try {
            if (
                    ev.getMessageId().equals(monitoredMessage.getMessageId()) &&
                    ev.getUserId().equals(monitoredMessage.getMessageOwner()) &&
                    monitoredMessage.getMenuType() == MenuType.UNMONITORALL &&
                    ev.getReactionEmote().getEmoji().equals("☑")
            ) {
                try {
                    // silent guild adder (to not cause conflicts)
                    if (!DataSource.checkDatabaseForData("SELECT * FROM GUILDS WHERE GUILD_ID = " + ev.getGuild().getId()))
                        Create.Guild(ev.getGuild().getId());
                    // checks db if channel exists
                    if (DataSource.checkDatabaseForData("SELECT * FROM CHANNELS JOIN CHANNEL_SETTINGS " +
                            "ON (CHANNELS.CHANNEL_ID = CHANNEL_SETTINGS.CHANNEL_ID) WHERE CHANNELS.GUILD_ID = " +
                            ev.getGuild().getId() + " AND CHANNEL_SETTINGS.MONITORED = 1")) {
                        List<String> channelsToUnMonitor = DataSource.query("SELECT CHANNEL_ID FROM CHANNELS WHERE GUILD_ID = " + ev.getGuild().getId());

                        for (String jt : channelsToUnMonitor) {
                            Create.ChannelMonitor(ev.getGuild().getId(), jt, 0);
                        }
                        Messages.sendMessage(ev.getChannel(), Embeds.allRemoved(ev.getUser().getAsTag()));
                    }
                    // if not, do not do anything
                    else {
                        Messages.sendMessage(ev.getChannel(), Embeds.noChannels());
                    }

                } catch (Exception ex) {
                    Logger lgr = LoggerFactory.getLogger(DataSource.class);
                    lgr.error(ex.getMessage(), ex);
                    Messages.sendMessage(ev.getChannel(), Embeds.fatalError());
                }
                monitoredMessage.invalidate();
                Messages.deleteMessage(ev.getChannel(), monitoredMessage.getMessageId());
            }
        } catch (IllegalStateException ignored) {}
    }
}

