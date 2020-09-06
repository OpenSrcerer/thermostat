package thermostat.thermoFunctions.listeners;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.AllowedMentions;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
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

    public void filter() {

        Member thermostatMember = eventChannel.getGuild().getMember(thermostat.thermo.getSelfUser());

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
                Messages.sendMessage(eventChannel, Embeds.simpleInsufficientPerm("MANAGE_WEBHOOKS"));
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

            if (badWords.stream().anyMatch(string::equalsIgnoreCase)) {
                messageWasChanged = true;
                message.set(index, niceWords.get(random.nextInt(niceWords.size())));
            }
        }

        if (messageWasChanged) {

            eventMessage.delete()
                    .reason("Inappropriate Language Filter (Thermostat)")
                    .queue();

            String webhookURL = DataSource.queryString("SELECT WEBHOOK_URL FROM " +
                    "CHANNEL_SETTINGS JOIN CHANNELS ON (CHANNELS.CHANNEL_ID = CHANNEL_SETTINGS.CHANNEL_ID) " +
                    "WHERE CHANNEL_SETTINGS.CHANNEL_ID = ?", eventChannel.getId());

            if (webhookURL != null) {
                if (webhookURL.equals("N/A")) {
                    createWebhook(eventChannel, eventMessage.getAuthor());

                    webhookURL = DataSource.queryString("SELECT WEBHOOK_URL FROM " +
                            "CHANNEL_SETTINGS JOIN CHANNELS ON (CHANNELS.CHANNEL_ID = CHANNEL_SETTINGS.CHANNEL_ID) " +
                            "WHERE CHANNEL_SETTINGS.CHANNEL_ID = ?", eventChannel.getId());

                    if (webhookURL == null) {
                        lgr.debug("Webhook is null, cancelled filter job. Guild: "
                                + eventChannel.getGuild().getId() + " // Channel: " + eventChannel.getId());
                        return;
                    }
                } else {
                    updateWebhook(eventChannel, eventMessage.getAuthor(), webhookURL);
                }
            } else {
                lgr.debug("Webhook is null, cancelled filter job. Guild: "
                        + eventChannel.getGuild().getId() + " // Channel: " + eventChannel.getId());
                return;
            }


            WebhookClientBuilder builder = new WebhookClientBuilder(webhookURL);
            builder.setAllowedMentions(AllowedMentions.none());
            WebhookClient client = builder.build();

            client.send(String.join(" ", message));
            client.close();
        }
    }

    public void updateWebhook(@NotNull TextChannel eventChannel, @NotNull User eventAuthor, String webhookURL) {

        String username = eventAuthor.getName();
        String userAvatarURL;

        if (eventAuthor.getAvatarUrl() != null)
            userAvatarURL = eventAuthor.getAvatarUrl();
        else
            userAvatarURL = eventAuthor.getDefaultAvatarUrl();

        Icon userAvatar = getUserIcon(userAvatarURL);

        eventChannel
                .retrieveWebhooks()
                .submit()
                .thenApply(webhookList -> findWebhook(webhookList, webhookURL))
                .thenApply(webhook -> webhook.getManager().setName(username).setAvatar(userAvatar)
                        .submit()
                );
    }

    @Nullable
    @CheckReturnValue
    public static Webhook findWebhook(@Nonnull List<Webhook> webhookList, @Nonnull String webhookURL) {
        Webhook foundWebhook = null;
        for (Webhook webhook : webhookList) {
            if (webhook.getUrl().equals(webhookURL)) {
                foundWebhook = webhook;
            }
        }
        return foundWebhook;
    }


    public void createWebhook(@NotNull TextChannel eventChannel, @NotNull User eventAuthor) {

        String username = eventAuthor.getName();
        String userAvatarURL;

        if (eventAuthor.getAvatarUrl() != null)
            userAvatarURL = eventAuthor.getAvatarUrl();
        else
            userAvatarURL = eventAuthor.getDefaultAvatarUrl();

        Icon userAvatar = getUserIcon(userAvatarURL);

        if (userAvatar != null) {
            eventChannel
                    .createWebhook(username)
                    .submit()
                    .thenApply(
                            webhook -> {
                                webhook.getManager().setAvatar(userAvatar).setName(username)
                                        .submit();
                                try {
                                    DataSource.update("UPDATE CHANNEL_SETTINGS JOIN CHANNELS ON " +
                                                    "(CHANNELS.CHANNEL_ID = CHANNEL_SETTINGS.CHANNEL_ID) " +
                                                    "SET CHANNEL_SETTINGS.WEBHOOK_URL = ? " +
                                                    "WHERE CHANNEL_SETTINGS.CHANNEL_ID = ?",
                                            Arrays.asList(webhook.getUrl(), eventChannel.getId()));
                                } catch (SQLException ex) {
                                    lgr.error("Something went wrong while setting Webhook ID!", ex);
                                }
                                return webhook;
                            }
                    );
        } else {
            lgr.error("UserAvatarURL is null. Guild: " + eventChannel.getGuild().getName());
        }
    }

    @Nullable
    @CheckReturnValue
    public Icon getUserIcon(@Nonnull String avatarURL) {
        try {
            InputStream imageStream = new URL(avatarURL).openStream();
            return Icon.from(imageStream, Icon.IconType.PNG);
        } catch (IOException ex) {
            return null;
        }
    }

    public static void setWordArrays(ArrayList<String> nice, ArrayList<String> prohibited) {
        niceWords = nice;
        badWords = prohibited;
    }
}
