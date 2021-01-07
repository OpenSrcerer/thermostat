package thermostat.dispatchers;

import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.collections4.map.ReferenceMap;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.Messages;
import thermostat.commands.CommandTrigger;
import thermostat.entities.ReactionMenu;
import thermostat.enumeration.MenuType;
import thermostat.mySQL.Create;
import thermostat.mySQL.DataSource;
import thermostat.preparedStatements.ErrorEmbeds;
import thermostat.preparedStatements.GenericEmbeds;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

/**
 * Organizes a cache of ReactionMenu and dispatches
 * incoming events until a timer runs out.
 */
public class MenuDispatcher extends ListenerAdapter {
    /**
     * Logger for MenuDispatcher.
     */
    private static final Logger lgr = LoggerFactory.getLogger(MenuDispatcher.class);

    /**
     * ReactionMenu cache that is accessed through the message ID.
     */
    private static final Map<String, ReactionMenu> cache = new ReferenceMap<>();

    /**
     * Removes a ReactionMenu from the cache.
     * @param messageId ID of ReactionMenu.
     * @param message Object that represents Menu.
     */
    public static void addMenu(String messageId, ReactionMenu message) {
        cache.put(messageId, message);
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
     * Tries to retrieve a menu on a Reaction being added to a message.
     * @param event Event where the reaction was added.
     */
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        ReactionMenu menu = getMenu(event.getMessageId());

        if (menu != null) {
            if (menu.getMenuType() == MenuType.UNMONITORALL) {
                matchUnMonitorAllReaction(menu, event);
            } else {
                matchInfoReaction(menu, event);
            }
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
    public void matchInfoReaction(ReactionMenu reactionMenu, GuildMessageReactionAddEvent event) {
        if (!reactionMenu.getOwnerId().equals(event.getUserId())) {
            return;
        }

        String prefix = CommandTrigger.getGuildPrefix(event.getGuild().getId());

        try {
            reactionMenu.resetDestructionTimer(event.getChannel());
            Messages.clearReactions(event.getChannel(), event.getMessageId());

            switch (event.getReactionEmote().getEmoji()) {
                case "🌡":
                    // Monitored Functions Menu
                    if (reactionMenu.getMenuType() == MenuType.SELECTION) {
                        Messages.editMessage(event.getChannel(), event.getMessageId(), GenericEmbeds.getMonitorInfo(prefix).build());
                        reactionMenu.setMenuType(MenuType.MONITOR);
                        Messages.addReactions(event.getChannel(), event.getMessageId(), Arrays.asList("⬆", "❌"));
                    }
                    break;
                case "🔧":
                    // Informational Menu
                    if (reactionMenu.getMenuType() == MenuType.SELECTION) {
                        Messages.editMessage(event.getChannel(), event.getMessageId(), GenericEmbeds.getUtilityInfo(prefix).build());
                        reactionMenu.setMenuType(MenuType.UTILITY);
                        Messages.addReactions(event.getChannel(), event.getMessageId(), Arrays.asList("⬆", "❌"));
                    }
                    break;
                case "ℹ":
                    // Informational Menu
                    if (reactionMenu.getMenuType() == MenuType.SELECTION) {
                        Messages.editMessage(event.getChannel(), event.getMessageId(), GenericEmbeds.getOtherInfo(prefix).build());
                        reactionMenu.setMenuType(MenuType.OTHER);
                        Messages.addReactions(event.getChannel(), event.getMessageId(), Arrays.asList("⬆", "❌"));
                    }
                    break;
                case "⬆":
                    // Exit Menu
                    if (reactionMenu.getMenuType() != MenuType.SELECTION) {
                        Messages.editMessage(event.getChannel(), event.getMessageId(), GenericEmbeds.getInfoSelection().build());
                        reactionMenu.setMenuType(MenuType.SELECTION);
                        Messages.addReactions(event.getChannel(), event.getMessageId(), Arrays.asList("🌡", "🔧", "ℹ", "❌"));
                    }
                    break;
                case "❌":
                    // Close Menu
                    reactionMenu.invalidate();
                    Messages.deleteMessage(event.getChannel(), event.getMessageId());
                    break;
                // This case is unreachable.
                default:
                    break;
            }
        } catch (IllegalStateException ex) {
            lgr.warn("Illegal Reaction State", ex);
        } catch (InsufficientPermissionException ex) {
            lgr.warn("Missing Permissions:", ex);
        }
    }

    public void matchUnMonitorAllReaction(ReactionMenu reactionMenu, GuildMessageReactionAddEvent event) {
        if (
                !reactionMenu.getOwnerId().equals(event.getUserId()) ||
                reactionMenu.getMenuType() != MenuType.UNMONITORALL ||
                !event.getReactionEmote().getEmoji().equals("☑")
        ) {
            return;
        }

        if (

                reactionMenu.getMenuType() == MenuType.UNMONITORALL &&
                event.getReactionEmote().getEmoji().equals("☑")
        ) {
            try {
                DataSource.execute(conn -> {
                        PreparedStatement pst =
                                conn.prepareStatement("SELECT * FROM CHANNELS JOIN CHANNEL_SETTINGS ON " +
                                "(CHANNELS.CHANNEL_ID = CHANNEL_SETTINGS.CHANNEL_ID) " +
                                "WHERE CHANNELS.GUILD_ID = ? AND CHANNEL_SETTINGS.MONITORED = 1");
                        pst.setString(1, event.getGuild().getId());

                        ResultSet rs = pst.executeQuery();

                        while (rs.next()) {
                            Create.Monitor(event.getGuild().getId(), rs.getString(1), 0);
                        }
                        return null;
                    }
                );
                Messages.sendMessage(event.getChannel(), GenericEmbeds.allRemoved(event.getUser().getAsTag(), event.getUser().getAvatarUrl()));
            } catch (SQLException ex) {
                lgr.error(ex.getMessage(), ex);
                Messages.sendMessage(event.getChannel(), ErrorEmbeds.error(ex.getLocalizedMessage()));
            }

            reactionMenu.invalidate();
            Messages.deleteMessage(event.getChannel(), event.getMessageId());
        }
    }
}