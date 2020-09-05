package thermostat.thermoFunctions.listeners;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.AllowedMentions;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static thermostat.thermostat.thermo;

public class WordFilterEvent {

    private static final Logger lgr = LoggerFactory.getLogger(WordFilterEvent.class);
    private static List<String>
            badWords,
            niceWords;
    private static final Random random = new Random();

    public WordFilterEvent() { }

    public void filter(@NotNull TextChannel eventChannel, @NotNull User eventAuthor, List<String> message) {

        boolean messageWasChanged = false;
        for (int index = 0; index < message.size(); ++index) {
            String string = message.get(index);

            if (badWords.stream().anyMatch(string::equalsIgnoreCase)) {
                messageWasChanged = true;
                message.set(index, niceWords.get(random.nextInt(niceWords.size())));
            }
        }

        if (messageWasChanged) {
            String webhookURL = getWebhookURL(eventChannel, eventAuthor);

            if (webhookURL != null) {
                WebhookClientBuilder builder = new WebhookClientBuilder(webhookURL);
                builder.setAllowedMentions(AllowedMentions.none());
                WebhookClient client = builder.build();

                client.send(String.join(" ", message));
                client.close();
            } else {
                lgr.debug("Webhook URL is null, cancelled filter job. Guild: "
                        + eventChannel.getGuild().getId() + " // Channel: " + eventChannel.getId());
            }
        }
    }

    @Nullable
    public String getWebhookURL(@NotNull TextChannel eventChannel, @NotNull User eventAuthor) {
        var retWebhook = new Object() { String webhookURL = null; };

        eventChannel.getGuild().retrieveMember(thermo.getSelfUser()).queue(thermostat -> {
            if (thermostat.hasPermission(Permission.MANAGE_WEBHOOKS)) {

                Webhook webhook = getThermoWebhook(eventChannel, thermostat);
                String userAvatarURL = eventAuthor.getAvatarUrl(),
                        username = eventAuthor.getName();
                Icon userAvatar;

                try {
                    if (userAvatarURL != null) {
                        InputStream imageStream = new URL(userAvatarURL).openStream();
                        userAvatar = Icon.from(imageStream, Icon.IconType.PNG);
                    } else { lgr.error("userAvatarURL is null. Guild: " + eventChannel.getGuild().getName()); return;}
                } catch (IOException ex) {
                    lgr.error("Something went wrong while reading from the Author URL!", ex);
                    return;
                }

                if (webhook == null) {
                    eventChannel.createWebhook(username).queue(newWebhook -> {
                        newWebhook.getManager().setAvatar(userAvatar).queue();
                        retWebhook.webhookURL = newWebhook.getUrl();
                    });
                } else {
                    webhook.getManager().setAvatar(userAvatar).setName(username).queue();
                    retWebhook.webhookURL = webhook.getUrl();
                }
            }
        });

        return retWebhook.webhookURL;
    }

    @Nullable
    public Webhook getThermoWebhook(@NotNull TextChannel eventChannel, @NotNull Member thermoMember) {
        var wrapper = new Object() { Webhook webhook = null; };

        eventChannel.retrieveWebhooks().queue(webhooks -> {
            webhooks.forEach(webhook -> {
                if (webhook.getOwner() == thermoMember) {
                    wrapper.webhook = webhook;
                }
            });
        });

        return wrapper.webhook;
    }

    public static void setWordArrays(ArrayList<String> nice, ArrayList<String> prohibited) {
        niceWords = nice;
        badWords = prohibited;
    }
}
