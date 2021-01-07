package thermostat.entities;

import net.dv8tion.jda.api.entities.TextChannel;
import thermostat.enumeration.MenuType;
import thermostat.preparedStatements.GenericEmbeds;
import thermostat.Messages;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Represents a Menu-like message in Discord that
 * needs to receive user input through reactions.
 */
public class MonitoredMessage {
    private final String messageId;
    private final String messageOwner;
    private MenuType menu;
    private ScheduledFuture<?> destructionTimer = null;

    public MonitoredMessage(String messageId, String messageOwner, MenuType menu) {
        this.messageId = messageId;
        this.messageOwner = messageOwner;
        this.menu = menu;
    }

    public void setMenuType(MenuType menu) {
        this.menu = menu;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getMessageOwner() {
        return messageOwner;
    }

    public MenuType getMenuType() {
        return menu;
    }

    public void resetDestructionTimer(TextChannel channel) {
        if (destructionTimer != null) {
            this.destructionTimer.cancel(true);
        }
        this.destructionTimer = executorService.schedule(() -> {
            Messages.deleteMessage(channel, this.messageId);
            monitoredMessages.remove(this);
            if (this.getMenuType() == MenuType.UNMONITORALL) {
                Messages.sendMessage(channel, GenericEmbeds.missedPrompt());
            }
        }, 100, TimeUnit.SECONDS);
    }

    public void invalidate() {
        this.destructionTimer.cancel(true);
    }
}