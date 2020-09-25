package thermostat.thermoFunctions.listeners;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.AllowedMentions;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.mySQL.DataSource;
import thermostat.thermoFunctions.Messages;
import thermostat.thermostat;
import thermostat.Embeds;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

/**
 * This class manages Word Filtering Events if enabled by the user.
 * Principle: All "prohibited" words in a message get changed to the "nice" words.
 * Used to filter out slurs.
 */
public class WordFilterEvent {

    private static final Logger lgr = LoggerFactory.getLogger(WordFilterEvent.class);
    private static List<String>
            badWords,
            niceWords;
    private static final Random random = new Random();

    private final TextChannel eventChannel;
    private final Message eventMessage;
    private final List<String> message;

    public WordFilterEvent(@NotNull TextChannel eventChannel, @NotNull Message eventMessage, @Nonnull List<String> message) {
        this.eventChannel = eventChannel;
        this.eventMessage = eventMessage;
        this.message = message;
        this.filter();
    }

    /**
     * Initiate the WFEvent.
     */
    public void filter() {
        Member thermostatMember = eventChannel.getGuild().getMember(thermostat.thermo.getSelfUser());

        // first catch conditions to not initiate event if permissions are lacked

        if (thermostatMember != null) {
            if (!thermostatMember.hasPermission(Permission.MANAGE_WEBHOOKS)) {
                lgr.debug("Thermostat lacks permission MANAGE_WEBHOOKS. Guild: "
                        + eventChannel.getGuild().getId() + " // Channel: " + eventChannel.getId());
                Messages.sendMessage(eventChannel, Embeds.simpleInsufficientPerm("MANAGE_WEBHOOKS"));
                return;
            }

            if (!thermostatMember.hasPermission(Permission.MESSAGE_MANAGE)) {
                lgr.debug("Thermostat lacks permission MANAGE_MESSAGES. Guild: "
                        + eventChannel.getGuild().getId() + " // Channel: " + eventChannel.getId());
                Messages.sendMessage(eventChannel, Embeds.simpleInsufficientPerm("MANAGE_MESSAGES"));
                return;
            }

        } else {
            lgr.debug("Thermostat member is null, cancelled filter job. Guild: "
                    + eventChannel.getGuild().getId() + " // Channel: " + eventChannel.getId());
            return;
        }

        boolean messageWasChanged = false;
        for (int index = 0; index < message.size(); ++index) {
            String string = message.get(index);

            if (badWords.stream().anyMatch(string.toLowerCase()::contains)) {
                messageWasChanged = true;
                message.set(index, niceWords.get(random.nextInt(niceWords.size())));
            }
        }

        if (messageWasChanged) {
            eventMessage.delete()
                    .reason("Inappropriate Language Filter (Thermostat)")
                    .queue();

            String webhookId = getWebhookID(), webhookToken = getWebhookToken();

            if (webhookId.equals("0")) {
                createWebhook(eventChannel, eventMessage.getAuthor())
                .map(unused -> {
                            sendWebhookMessage(getWebhookID(), getWebhookToken());
                            return unused;
                }).queue();
            } else {
                updateWebhook(eventMessage.getAuthor(), webhookId)
                .map(unused -> {
                    sendWebhookMessage(webhookId, webhookToken);
                    return unused;
                }).queue();
            }
        }
    }

    /**
     * Retrieves Webhook URL from DB.
     * @return webhookurl
     */
    public String getWebhookToken() {
        return DataSource.queryString("SELECT WEBHOOK_TOKEN FROM " +
                "CHANNEL_SETTINGS JOIN CHANNELS ON (CHANNELS.CHANNEL_ID = CHANNEL_SETTINGS.CHANNEL_ID) " +
                "WHERE CHANNEL_SETTINGS.CHANNEL_ID = ?", eventChannel.getId());
    }

    /**
     * Retrieves Webhook URL from DB.
     * @return webhookurl
     */
    public String getWebhookID() {
        return DataSource.queryString("SELECT WEBHOOK_ID FROM " +
                "CHANNEL_SETTINGS JOIN CHANNELS ON (CHANNELS.CHANNEL_ID = CHANNEL_SETTINGS.CHANNEL_ID) " +
                "WHERE CHANNEL_SETTINGS.CHANNEL_ID = ?", eventChannel.getId());
    }

    /**
     * Sends compiled message through the webhook provided.
     * @param webhookID Webhook Id
     * @param webhookToken Webhook Token
     */
    public void sendWebhookMessage(@Nonnull String webhookID, String webhookToken) {
        WebhookClientBuilder builder = new WebhookClientBuilder(Long.parseLong(webhookID), webhookToken);
        builder.setAllowedMentions(AllowedMentions.none());
        WebhookClient client = builder.build();

        client.send(String.join(" ", message));
        client.close();
    }


    /**
     * Updates an existing webhook with new parameters.
     * @param eventAuthor User object that carries the new params.
     * @param webhookId Webhook target ID
     * @return RestAction to call when necessary
     */
    public RestAction<Void> updateWebhook(@NotNull User eventAuthor, String webhookId) {

        String username = eventAuthor.getName();
        String userAvatarURL;

        if (eventAuthor.getAvatarUrl() != null)
            userAvatarURL = eventAuthor.getAvatarUrl();
        else
            userAvatarURL = eventAuthor.getDefaultAvatarUrl();

        Icon userAvatar = getUserIcon(userAvatarURL);

        return thermostat.thermo
                .retrieveWebhookById(webhookId)
                .flatMap(
                        Objects::nonNull,
                        webhook -> webhook.getManager().setName(username).setAvatar(userAvatar)
                );
    }

    /**
     * RestAction creator for a webhook.
     * @param eventChannel Channel where the webhook is going to be created.
     * @param eventAuthor User object of the user that the webhook will inherit
     *                    the profile picture from, along with the name.
     * @return RestAction with the created webhook as a parameter
     */
    public RestAction<Webhook> createWebhook(@NotNull TextChannel eventChannel, @NotNull User eventAuthor) {

        String username = eventAuthor.getName();
        String userAvatarURL;

        if (eventAuthor.getAvatarUrl() != null)
            userAvatarURL = eventAuthor.getAvatarUrl();
        else
            userAvatarURL = eventAuthor.getDefaultAvatarUrl();

        Icon userAvatar = getUserIcon(userAvatarURL);

        return eventChannel
                .createWebhook(username)
                .map(
                        webhook -> {
                            webhook.getManager().setAvatar(userAvatar).setName(username)
                                    .queue();
                            try {
                                DataSource.update("UPDATE CHANNEL_SETTINGS JOIN CHANNELS ON " +
                                                "(CHANNELS.CHANNEL_ID = CHANNEL_SETTINGS.CHANNEL_ID) " +
                                                "SET CHANNEL_SETTINGS.WEBHOOK_ID = ?, " +
                                                "CHANNEL_SETTINGS.WEBHOOK_TOKEN = ? " +
                                                "WHERE CHANNEL_SETTINGS.CHANNEL_ID = ?",
                                        Arrays.asList(webhook.getId(), webhook.getToken(), eventChannel.getId()));
                            } catch (SQLException ex) {
                                lgr.error("Something went wrong while setting Webhook Params!", ex);
                            }
                            return webhook;
                        }
                );
    }

    /**
     * Gets the user icon in a JPEG format from the Discord servers.
     * @param avatarURL URL of user avatar image
     * @return User's avatar image in an Icon format
     * ready to be processed by the Webhook Creator
     */
    @Nullable
    @CheckReturnValue
    public Icon getUserIcon(@Nonnull String avatarURL) {
        try {
            InputStream imageStream = new URL(avatarURL + "?size=64").openStream();
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
    public static void setWordArrays(ArrayList<String> nice, ArrayList<String> prohibited) {
        niceWords = nice;
        badWords = prohibited;
    }
}
