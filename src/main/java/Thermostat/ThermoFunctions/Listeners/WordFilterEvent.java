package thermostat.thermoFunctions.listeners;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.EnumSet;

import static thermostat.thermostat.thermo;

public class WordFilterEvent {

    /*
    private final EnumSet<Permission> neededPermissions = EnumSet.of(
            Permission.MANAGE_WEBHOOKS,
            Permission.
    );*/

    private final Logger lgr = LoggerFactory.getLogger(this.getClass());

    public WordFilterEvent() { }

    public void filter(@NotNull TextChannel eventChannel, @NotNull Message eventMessage) {
        eventChannel.getGuild().retrieveMember(thermo.getSelfUser()).queue(thermostat -> {
            if (thermostat.hasPermission(Permission.MANAGE_WEBHOOKS)) {

                Webhook webhook = getThermoWebhook(eventChannel, thermostat);
                String userAvatarURL = eventMessage.getAuthor().getAvatarUrl(),
                username = eventMessage.getAuthor().getName();
                Icon userAvatar;

                try {
                    if (userAvatarURL != null) {
                        InputStream imageStream = new URL(userAvatarURL).openStream();
                        userAvatar = Icon.from(imageStream, Icon.IconType.PNG);
                    } else { return; }
                } catch (IOException ex) {
                    lgr.error("Something went wrong while reading from the Author URL!", ex);
                    return;
                }

                if (webhook == null) {
                    eventChannel.createWebhook(username).queue(newWebhook -> {
                        newWebhook.getManager().setAvatar(userAvatar).queue();
                    });
                } else {
                    webhook.getManager().setAvatar(userAvatar).setName(username).queue();
                }
            }
        });
    }

    @Nullable
    public static Webhook getThermoWebhook(@NotNull TextChannel eventChannel, @NotNull Member thermoMember) {
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

    public static void failure(Throwable throwable) {

    }
}
