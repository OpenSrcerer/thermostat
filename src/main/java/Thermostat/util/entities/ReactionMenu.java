package thermostat.util.entities;

import net.dv8tion.jda.api.entities.TextChannel;
import thermostat.Messages;
import thermostat.Thermostat;
import thermostat.dispatchers.MenuDispatcher;
import thermostat.util.enumeration.MenuType;
import thermostat.Embeds.GenericEmbeds;

import javax.annotation.Nonnull;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Represents a Menu-like message in Discord that
 * needs to receive user input through reactions.
 */
public class ReactionMenu {
    /**
     * Holds the current type of menu.
     * Can switch since the user cycles between
     * varying types of menus.
     */
    private MenuType menu;

    /**
     * The original initiator of the mentu.
     */
    private final String ownerId;

    /**
     * This menu's Discord ID.
     */
    private final String messageId;

    /**
     * When this menu will stop being cached.
     */
    private ScheduledFuture<?> decachingTimer;

    /**
     * Build a new menu.
     * @param menu Type of menu.
     * @param ownerId Initiator of this menu.
     */
    public ReactionMenu(@Nonnull MenuType menu, @Nonnull String ownerId, @Nonnull String messageId, @Nonnull TextChannel channel) {
        this.menu = menu;
        this.ownerId = ownerId;
        this.messageId = messageId;

        rescheduleTimer(channel);
        MenuDispatcher.addMenu(messageId, this);
    }

    /**
     * Set the menu type to something else.
     * @param menu New type of menu.
     */
    public void setMenuType(MenuType menu) {
        this.menu = menu;
    }

    /**
     * Create a new destruction timer for this menu.
     */
    public void rescheduleTimer(TextChannel channel) {
        this.decachingTimer = Thermostat.executor.schedule(() -> {
            Messages.deleteMessage(channel, messageId);
            MenuDispatcher.removeMenu(messageId);

            if (this.getMenuType() == MenuType.UNMONITORALL) {
                Messages.sendMessage(channel, GenericEmbeds.missedPrompt());
            }
        }, 200, TimeUnit.SECONDS);
    }

    /**
     * Get the menu's type.
     * @return Type of menu.
     */
    @Nonnull
    public MenuType getMenuType() {
        return menu;
    }

    /**
     * Get the owner of the menu.
     * @return ID of menu's owner.
     */
    @Nonnull
    public String getOwnerId() {
        return ownerId;
    }

    /**
     * Cancels the destruction timer for this menu and
     * restarts the countdown.
     * @param channel Channel to delete the menu from.
     */
    public void resetDestructionTimer(TextChannel channel) {
        if (decachingTimer != null) {
            this.decachingTimer.cancel(true);
        }

        rescheduleTimer(channel);
    }

    /**
     * Cancel the remove-from-cache operation for this menu.
     */
    public void invalidate() {
        this.decachingTimer.cancel(true);
    }
}