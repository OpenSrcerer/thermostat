package thermostat.thermoFunctions.listeners;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.AllowedMentions;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.thermoFunctions.Messages;
import thermostat.thermostat;
import thermostat.Embeds;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class WordFilterEvent {

    private static final Logger lgr = LoggerFactory.getLogger(WordFilterEvent.class);
    private static List<String>
            badWords,
            niceWords;
    private static final Random random = new Random();

    private final TextChannel eventChannel;
    private final User eventAuthor;
    private final List<String> message;

    private final RestAction<Member> retrieveThermostatMember;

    public WordFilterEvent(@NotNull TextChannel eventChannel, @NotNull User eventAuthor, @Nonnull List<String> message) {
        this.eventChannel = eventChannel;
        this.eventAuthor = eventAuthor;
        this.message = message;
        retrieveThermostatMember = eventChannel.getGuild().retrieveMember(thermostat.thermo.getSelfUser());
        this.filter();
    }

    public void filter() {

        boolean messageWasChanged = false;
        for (int index = 0; index < message.size(); ++index) {
            String string = message.get(index);

            if (badWords.stream().anyMatch(string::equalsIgnoreCase)) {
                messageWasChanged = true;
                message.set(index, niceWords.get(random.nextInt(niceWords.size())));
            }
        }

        if (messageWasChanged) {
            Member thermostatMember = eventChannel.getGuild().getMember(thermostat.thermo.getSelfUser());

            if (thermostatMember != null) {
                if (!thermostatMember.hasPermission(Permission.MANAGE_WEBHOOKS)) {
                    Messages.sendMessage(eventChannel, Embeds.simpleInsufficientPerm("MANAGE_WEBHOOKS"));
                    return;
                }

                RestAction<Webhook> getWebhook = getWebhook(eventChannel, eventAuthor);

                if (getWebhook == null) {
                    lgr.debug("Webhook is null, cancelled filter job. Guild: "
                            + eventChannel.getGuild().getId() + " // Channel: " + eventChannel.getId());
                    return;
                }

                getWebhook.queue(webhook -> {
                    WebhookClientBuilder builder = new WebhookClientBuilder(webhook.getId());
                    builder.setAllowedMentions(AllowedMentions.none());
                    WebhookClient client = builder.build();

                    client.send(String.join(" ", message));
                    client.close();
                });
            }
        }
    }

    @Nullable
    public RestAction<Webhook> getWebhook(@NotNull TextChannel eventChannel, @NotNull User eventAuthor) {

        String userAvatarURL = eventAuthor.getAvatarUrl(),
                username = eventAuthor.getName();
        Icon userAvatar;

        try {
            if (userAvatarURL != null) {
                InputStream imageStream = new URL(userAvatarURL).openStream();
                userAvatar = Icon.from(imageStream, Icon.IconType.PNG);
            } else {
                lgr.error("UserAvatarURL is null. Guild: " + eventChannel.getGuild().getName());
                return null;
            }
        } catch (IOException ex) {
            lgr.error("Something went wrong while reading from the Author URL!", ex);
            return null;
        }

        return eventChannel.retrieveWebhooks()
                .and(
                        retrieveThermostatMember,
                        WordFilterEvent::findWebhook
                )
                .flatMap(
                        Objects::isNull,
                        (webhook) -> eventChannel.createWebhook(username)
                )
                .map(
                        (webhook) -> webhook.getManager().setAvatar(userAvatar).setName(username).getWebhook()
                );
    }

    public static Webhook findWebhook(List<Webhook> webhookList, Member thermostatMember) {
        Webhook foundWebhook = null;
        for (Webhook webhook : webhookList) {
            if (webhook.getOwner() == thermostatMember) {
                foundWebhook = webhook;
            }
        }
        return foundWebhook;
    }

    public static void setWordArrays(ArrayList<String> nice, ArrayList<String> prohibited) {
        niceWords = nice;
        badWords = prohibited;
    }
}
