package thermostat.dispatchers;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.collections4.map.ReferenceMap;
import org.jetbrains.annotations.NotNull;
import thermostat.commands.Command;
import thermostat.embeds.Embeds;
import thermostat.mySQL.DataSource;
import thermostat.mySQL.PreparedActions;
import thermostat.util.GuildCache;
import thermostat.util.entities.InsufficientPermissionsException;
import thermostat.util.entities.ReactionMenu;
import thermostat.util.enumeration.DBActionType;
import thermostat.util.enumeration.EmbedType;
import thermostat.util.enumeration.MenuType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

import static thermostat.util.RestActions.*;

/**
 * Organizes a cache of ReactionMenu and dispatches
 * incoming events until a timer runs out.
 */
public class MenuDispatcher extends ListenerAdapter {

    /**
     * ReactionMenu cache that is accessed through the message ID.
     */
    private static final Map<String, ReactionMenu> cache = new ReferenceMap<>();

    /**
     * Adds a new ReactionMenu to the cache.
     * @param type Type of ReactionMenu.
     * @param messageId Message ID of ReactionMenu.
     * @param command The Command that created the ReactionMenu.
     */
    public static void addMenu(final MenuType type, final String messageId, final Command command) {
        cache.put(messageId, new ReactionMenu(type, messageId, command));
    }

    /**
     * Retrieves a ReactionMenu from the cache.
     * @param messageId ID of ReactionMenu.
     */
    @Nullable
    private static ReactionMenu getMenu(String messageId) {
        return cache.get(messageId);
    }

    /**
     * Removes a ReactionMenu from the cache.
     * @param messageId ID of ReactionMenu.
     */
    public static void removeMenu(String messageId) {
        cache.remove(messageId);
    }

    /**
     * Invalidate a menu and then remove it from the cache.
     * @param messageId Id of menu's message.
     */
    private static void expungeMenu(String messageId) {
        ReactionMenu menu = getMenu(messageId);

        if (menu != null) {
            menu.invalidate();
            cache.remove(messageId);
        }
    }

    /**
     * Tries to retrieve a menu on a Reaction being added to a message.
     * @param event Event where the reaction was added.
     */
    public void onGuildMessageReactionAdd(@NotNull final GuildMessageReactionAddEvent event) {
        ReactionMenu menu = getMenu(event.getMessageId());

        try {
            if (menu != null) {
                switch (menu.getMenuType()) {
                    case MONITORALL -> matchFMReaction(menu, event, DBActionType.MONITOR, 1);
                    case UNMONITORALL -> matchFMReaction(menu, event, DBActionType.MONITOR, 0);
                    case FILTERALL -> matchFMReaction(menu, event, DBActionType.FILTER, 1);
                    case UNFILTERALL -> matchFMReaction(menu, event, DBActionType.FILTER, 0);
                    case SELECTION, MONITOR, UTILITY, OTHER -> matchInfoReaction(menu, event);
                    default -> expungeMenu(event.getMessageId());
                }
            }
        } catch (InsufficientPermissionsException ex) {
            sendMessage(event.getChannel(),
                    Embeds.getEmbed(EmbedType.ERR_PERMISSION_THERMO, ex.getPermissionSet())).queue();
            expungeMenu(event.getMessageId());
        } catch (Exception ex) {
            sendMessage(event.getChannel(),
                    Embeds.getEmbed(EmbedType.ERR, "Something went wrong. Please try again.")).queue();
            ex.printStackTrace();
        }
    }

    /**
     * Removes cached menus if they are deleted.
     * @param event Deletion event.
     */
    public void onGuildMessageDelete(@Nonnull GuildMessageDeleteEvent event) {
        ReactionMenu menu = cache.get(event.getMessageId());

        if (menu != null) {
            menu.invalidate();
            cache.remove(event.getMessageId());
        }
    }

    /**
     * Checks if new reactions have been added in
     * a MonitoredMessage and applies an appropriate action.
     * @param reactionMenu The monitored message.
     * @param event The event of the reaction being added.
     */
    public void matchInfoReaction(final ReactionMenu reactionMenu, final GuildMessageReactionAddEvent event)
            throws InsufficientPermissionsException, IllegalStateException {
        if (!reactionMenu.getOwnerId().equals(event.getUserId())) {
            return;
        }

        switch (event.getReactionEmote().getEmoji()) {
            // Monitoring Submenu
            case "🌡" -> updateMenu(reactionMenu, event, EmbedType.MONITOR_INFO, reactionMenu.getMenuType(), MenuType.MONITOR);
            // Utility Submenu
            case "🔧" -> updateMenu(reactionMenu, event, EmbedType.UTILITY_INFO, reactionMenu.getMenuType(), MenuType.UTILITY);
            // Informational Submenu
            case "ℹ" -> updateMenu(reactionMenu, event, EmbedType.OTHER_INFO, reactionMenu.getMenuType(), MenuType.OTHER);
            // Go back to main menu
            case "🔼" -> updateMenu(reactionMenu, event, EmbedType.SELECTION, reactionMenu.getMenuType(), MenuType.SELECTION);
            // Close Menu
            case "❌" -> {
                removeMenu(reactionMenu.getMessageId());
                event.getChannel().retrieveMessageById(reactionMenu.getMessageId()).flatMap(Message::delete).queue();
            }
            // Remove invalid reaction
            default -> event.getReaction().removeReaction().queue();
        }
    }

    public static void updateMenu(final ReactionMenu reactionMenu, final GuildMessageReactionAddEvent event,
                                  final EmbedType type, final MenuType currType, final MenuType newType)
    {
        reactionMenu.setMenuType(newType);
        reactionMenu.resetDestructionTimer(event.getChannel());

        if (currType == MenuType.SELECTION) {
            final String prefix = GuildCache.getPrefix(event.getGuild().getId());

            // Action to update the menu to a submenu
            event.getChannel().retrieveMessageById(reactionMenu.getMessageId())
                    .flatMap(message ->
                            message.clearReactions()
                            .and(editMessage(message, Embeds.getEmbed(type, prefix)))
                            .and(addReactions(message, Arrays.asList("🔼", "❌")))
                    ).queue();
        } else {
            // Go back to the main menu
            event.getChannel().retrieveMessageById(reactionMenu.getMessageId())
                    .flatMap(message ->
                            message.clearReactions()
                            .and(editMessage(message, Embeds.getEmbed(type)))
                            .and(addReactions(message, Arrays.asList("🌡", "🔧", "ℹ", "❌")))
                    ).queue();
        }
    }

    /**
     * Checks if a given reaction matches a GuildMessageReactionAddEvent, then runs a Filter or Monitor action
     * on the database.
     * @param reactionMenu ReactionMenu to compare values with.
     * @param event Reaction event data.
     * @param type Type of action to perform (Filter/Monitor).
     * @throws SQLException If action could not be performed.
     * @throws InsufficientPermissionsException Thermostat lacks permissions to add reactions.
     */
    public void matchFMReaction(final ReactionMenu reactionMenu, final GuildMessageReactionAddEvent event,
                                final DBActionType type, final int actionValue)
            throws SQLException, InsufficientPermissionsException
    {
        if (!reactionMenu.getOwnerId().equals(event.getUserId()) || !event.getReactionEmote().getEmoji().equals("☑")) {
            return;
        }

        if (event.getReactionEmote().getEmoji().equals("☑")) {

            if (actionValue == 0) {
                DataSource.demand(PreparedActions.discardChannels(event, type));
            } else if (actionValue == 1) {
                DataSource.demand(PreparedActions.acquireChannels(event, type));
            } else {
                throw new IllegalArgumentException("Action Value must be 0 or 1");
            }

            expungeMenu(reactionMenu.getMessageId());

            event.getChannel().retrieveMessageById(event.getMessageId())
                    .flatMap(message -> editMessage(message, Embeds.getEmbed(EmbedType.ACTION_SUCCESSFUL,
                            reactionMenu.getCommand().getData()))).queue();
        }
    }


}
