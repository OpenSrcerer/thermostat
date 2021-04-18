package thermostat.commands.internal;

import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import okhttp3.internal.annotations.EverythingIsNonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.Thermostat;
import thermostat.commands.InternalCommand;
import thermostat.dispatchers.CommandDispatcher;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.embeds.Embeds;
import thermostat.embeds.ThermoEmbed;
import thermostat.mySQL.PreparedActions;
import thermostat.util.GuildCache;
import thermostat.util.entities.CommandContext;
import thermostat.util.enumeration.CommandType;
import thermostat.util.enumeration.EmbedType;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This class manages Word Filtering Events if enabled by the user.
 * Principle: All "prohibited" words in a message get changed to the "nice" words.
 * Used to filter out slurs.
 */
public class WordFilter implements InternalCommand {

    /**
     * Logger for this class.
     */
    private static final Logger lgr = LoggerFactory.getLogger(WordFilter.class);

    /**
     * Data for this command.
     */
    private final CommandContext data;

    /**
     * Message to filter.
     */
    private final List<String> message;

    /**
     * List of bad and replacement words.
     */
    private static List<String> badWords, niceWords;

    public WordFilter(@Nonnull final GuildMessageReceivedEvent data) {
        this.data = new CommandContext(data);
        this.message = new ArrayList<>(Arrays.asList(data.getMessage().getContentRaw().split("\\s+")));

        CommandDispatcher.checkThermoPermissionsAndQueue(this);
    }

    /**
     * Initiate the WordFilterEvent.
     */
    @Override
    public void run() {
        if (checkMessageForProfanity()) {
            data.event.getMessage().delete()
                    .reason("Inappropriate Language Filter")
                    .queue();
            try {
                webhookAction(); // Proceeding carefully with handling this raw connection.
            } catch (SQLException ex) {
                ResponseDispatcher.commandFailed(this, Embeds.getEmbed(EmbedType.ERR, "Could not filter message. Cause:\n" + ex.getMessage()));
            }
        }
    }

    private void webhookAction() throws SQLException {
        // webhookId, webhookToken
        final String[] webhookIdentity = PreparedActions.getWebhookValue(data.event.getChannel().getId());

        if (webhookIdentity[0].equals("0")) {
            createWebhook()
                    .map(webhook -> {
                        sendWebhookMessage(webhook.getId(), webhook.getToken());
                        ResponseDispatcher.commandSucceeded(this, (ThermoEmbed) null);
                        return webhook;
                    })
                    .onErrorMap(t -> {
                        ResponseDispatcher.commandFailed(this, Embeds.getEmbed(EmbedType.ERR, data, t.getMessage()), t);
                        return null;
                    }).queue();
        } else {
            updateWebhook(data.event.getAuthor(), webhookIdentity[0])
                    .map(webhook -> {
                        sendWebhookMessage(webhookIdentity[0], webhookIdentity[1]);
                        ResponseDispatcher.commandSucceeded(this, (ThermoEmbed) null);
                        return webhook;
                    })
                    // if something is wrong with the previous webhook, create a new one
                    .onErrorMap(t -> {
                        try {
                            PreparedActions.deleteWebhook(data.event.getChannel().getId());
                        } catch (SQLException ignored) {
                        }
                        return null;
                    }).queue();
        }
    }

    private boolean checkMessageForProfanity() {
        boolean messageWasChanged = false;
        for (int index = 0; index < message.size(); ++index) {
            String string = message.get(index);

            if (badWords.stream().anyMatch(string.toLowerCase()::contains)) {
                messageWasChanged = true;
                message.set(index, niceWords.get(ThreadLocalRandom.current().nextInt(niceWords.size())));
            }
        }
        return messageWasChanged;
    }

    /**
     * Sends compiled message through the webhook provided.
     */
    @EverythingIsNonNull
    public void sendWebhookMessage(final String webhookId, final String webhookToken) {
        GuildCache.getClient(data.event.getGuild().getId(), data.event.getChannel().getId(),
                webhookId, webhookToken).send(String.join(" ", message));
    }

    /**
     * Updates an existing webhook with new parameters.
     * @param eventAuthor User object that carries the new params.
     * @param webhookId Webhook target ID
     * @return RestAction to call when necessary
     */
    @EverythingIsNonNull
    public RestAction<Void> updateWebhook(final User eventAuthor, final String webhookId) {
        String username = eventAuthor.getName();
        String userAvatarURL;

        if (eventAuthor.getAvatarUrl() != null)
            userAvatarURL = eventAuthor.getAvatarUrl();
        else userAvatarURL = eventAuthor.getDefaultAvatarUrl();
        Icon userAvatar = getUserIcon(userAvatarURL);

        return Thermostat.thermo.retrieveWebhookById(webhookId)
                .flatMap(
                        Objects::nonNull,
                        webhook -> webhook.getManager().setName(username).setAvatar(userAvatar)
                );
    }

    /**
     * RestAction creator for a webhook.
     * @return RestAction with the created webhook as a parameter
     */
    public RestAction<Webhook> createWebhook() {
        User user = data.event.getMember().getUser();
        String username = user.getName();
        String userAvatarURL;

        if (user.getAvatarUrl() != null)
            userAvatarURL = user.getAvatarUrl();
        else
            userAvatarURL = user.getDefaultAvatarUrl();
        Icon userAvatar = getUserIcon(userAvatarURL);

        return data.event.getChannel()
                .createWebhook(username)
                .map(
                        webhook -> {
                            webhook.getManager().setAvatar(userAvatar).setName(username).queue();
                            try {
                                PreparedActions.createWebhook(webhook.getId(), webhook.getToken(), data.event.getChannel().getId());
                            } catch (SQLException ex) {
                                ResponseDispatcher.commandFailed(this, Embeds.getEmbed(EmbedType.ERR, data, ex.getMessage()), ex);
                            }
                            return webhook;
                        }
                );
    }

    /**
     * Gets the user icon in a low-fidelity JPEG format from the Discord servers.
     * @param avatarURL URL of user avatar image
     * @return User's avatar image in an Icon format
     * ready to be processed by the Webhook Creator
     */
    @Nullable
    @CheckReturnValue
    public Icon getUserIcon(@Nonnull final String avatarURL) {
        try (InputStream imageStream = new URL(avatarURL + "?size=64").openStream()) {
            return Icon.from(imageStream, Icon.IconType.JPEG);
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * Set function for word lists upon initialization.
     * @param nice Words that will replace the prohibited words.
     * @param prohibited Prohibited words that will get removed.
     */
    @EverythingIsNonNull
    public static void setWordArrays(final ArrayList<String> nice, final ArrayList<String> prohibited) {
        niceWords = nice;
        badWords = prohibited;
    }

    @Override
    public CommandType getType() {
        return CommandType.WORDFILTEREVENT;
    }

    @Override
    public Logger getLogger() {
        return lgr;
    }

    @Override
    public CommandContext getData() {
        return data;
    }
}
