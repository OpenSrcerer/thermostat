package thermostat.util.entities;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import thermostat.Thermostat;
import thermostat.commands.Command;
import thermostat.dispatchers.MenuDispatcher;
import thermostat.embeds.Embeds;
import thermostat.util.RestActions;
import thermostat.util.enumeration.EmbedType;
import thermostat.util.enumeration.MenuType;

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
     * This menu's Discord ID.
     */
    private final String messageId;

    /**
     * Command that initialized this menu.
     */
    private final Command command;

    /**
     * When this menu will stop being cached.
     */
    private ScheduledFuture<Void> decachingTimer;

    /**
     * Build a new menu.
     * @param menu Type of menu.
     * @param messageId Message ID of this menu.
     * @param command The Command that created this ReactionMenu.
     */
    public ReactionMenu(@Nonnull final MenuType menu, @Nonnull final String messageId, @Nonnull final Command command) {
        this.menu = menu;
        this.messageId = messageId;
        this.command = command;

        rescheduleTimer(command.getData().event.getChannel());
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
    public void rescheduleTimer(final TextChannel channel) {
        this.decachingTimer = Thermostat.SCHEDULED_EXECUTOR.schedule(() -> {
            channel.retrieveMessageById(messageId).flatMap(Message::delete).queue(null, null);
            MenuDispatcher.removeMenu(messageId);

            switch (this.getMenuType()) {
                case MONITORALL, FILTERALL -> RestActions.sendMessage(channel, Embeds.getEmbed(EmbedType.MISSED_PROMPT)).queue();
                default -> throw new RuntimeException("Unknown menu type");
            }
            return null;
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
     * Get this menu's message ID.
     * @return Message ID of menu.
     */
    @Nonnull
    public String getMessageId() {
        return messageId;
    }

    /**
     * Get the owner of the menu.
     * @return ID of menu's owner.
     */
    @Nonnull
    public String getOwnerId() {
        return command.getData().event.getAuthor().getId();
    }

    /**
     * Get the owner of the menu.
     * @return ID of menu's owner.
     */
    @Nonnull
    public Command getCommand() {
        return command;
    }

    /**
     * Cancels the destruction timer for this menu and
     * restarts the countdown.
     * @param channel Channel to delete the menu from.
     */
    public void resetDestructionTimer(final TextChannel channel) {
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