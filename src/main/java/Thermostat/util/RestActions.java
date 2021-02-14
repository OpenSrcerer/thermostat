package thermostat.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.Contract;

import javax.annotation.CheckReturnValue;
import java.util.List;

/**
 * A handy Message sender with static functions.
 */
public final class RestActions {
    /**
     * Sends an embed to a designated channel.
     * @param channel Channel to send the embed in.
     * @param embed   The embed to send.
     */
    @CheckReturnValue
    @Contract("null, null -> fail; null, _ -> fail; _, null -> fail")
    public static RestAction<Message> sendMessage(final TextChannel channel, final EmbedBuilder embed) {
        return channel.sendMessage(embed.build());
    }

    /**
     * Edits a Message's content to the provided MessageEmbed.
     * @param embed New embed to place in the message.
     */
    @CheckReturnValue
    @Contract("null, null -> fail; null, _ -> fail; _, null -> fail")
    public static RestAction<Message> editMessage(final Message message, final EmbedBuilder embed) {
        return message.editMessage(embed.build());
    }

    /**
     * Adds a list of reactions to a given message.
     * @param unicode The unicode emoji to add as a reaction.
     */
    @CheckReturnValue
    @Contract("null, null -> fail; null, _ -> null")
    public static RestAction<Void> addReactions(final Message message, final List<String> unicode) {
        RestAction<Void> action = null;
        for (final String it : unicode) {
            if (action == null) {
                action = message.addReaction(it);
            }
            action = action.and(message.addReaction(it));
        }
        return action;
    }
}