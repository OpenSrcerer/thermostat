package thermostat.commands.utility;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.AllowedMentions;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.mySQL.DataSource;
import thermostat.preparedStatements.ErrorEmbeds;
import thermostat.util.Functions;
import thermostat.commands.Command;
import thermostat.enumeration.CommandType;
import thermostat.Thermostat;

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
@SuppressWarnings("ConstantConditions")
public class WordFilterCommand implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(WordFilterCommand.class);

    private static List<String> badWords, niceWords;
    private static final Random random = new Random();

    private final GuildMessageReceivedEvent data;
    private final List<String> message;
    private final long commandId;

    public WordFilterCommand(@Nonnull GuildMessageReceivedEvent data) {
        this.data = data;
        this.message = new ArrayList<>(Arrays.asList(data.getMessage().getContentRaw().split("\\s+")));
        this.commandId = Functions.getCommandId();

        if (validateEvent(data)) {
            checkThermoPermissionsAndQueue(this);
        }
    }

    /**
     * Initiate the WFEvent.
     */
    @Override
    public void run() {
        boolean messageWasChanged = false;
        for (int index = 0; index < message.size(); ++index) {
            String string = message.get(index);

            if (badWords.stream().anyMatch(string.toLowerCase()::contains)) {
                messageWasChanged = true;
                message.set(index, niceWords.get(random.nextInt(niceWords.size())));
            }
        }

        if (messageWasChanged) {
            data.getMessage().delete()
                    .reason("Inappropriate Language Filter (Thermostat)")
                    .queue();


            String webhookId, webhookToken;
            try {
                webhookId = getWebhookID();
                webhookToken = getWebhookToken();
            } catch (SQLException ex) {
                ResponseDispatcher.commandFailed(this, null, ex);
                return;
            }

            if (webhookId.equals("0")) {
                createWebhook()
                    .map(webhook -> {
                        try {
                            sendWebhookMessage(getWebhookID(), getWebhookToken());
                            ResponseDispatcher.commandSucceeded(this, null);
                        } catch (SQLException ex) {
                            ResponseDispatcher.commandFailed(this, null, ex);
                        }
                        return webhook;
                }).queue();
            } else {
                updateWebhook(data.getAuthor(), webhookId)
                .map(webhook -> {
                    sendWebhookMessage(webhookId, webhookToken);
                    ResponseDispatcher.commandSucceeded(this, null);
                    return webhook;
                }).queue();
            }
        }
    }

    /**
     * Retrieves Webhook URL from DB.
     * @return webhookurl
     */
    public String getWebhookToken() throws SQLException {
        return DataSource.queryString("SELECT WEBHOOK_TOKEN FROM " +
                "CHANNEL_SETTINGS JOIN CHANNELS ON (CHANNELS.CHANNEL_ID = CHANNEL_SETTINGS.CHANNEL_ID) " +
                "WHERE CHANNEL_SETTINGS.CHANNEL_ID = ?", data.getChannel().getId());
    }

    /**
     * Retrieves Webhook URL from DB.
     * @return webhookurl
     */
    public String getWebhookID() throws SQLException {
        return DataSource.queryString("SELECT WEBHOOK_ID FROM " +
                "CHANNEL_SETTINGS JOIN CHANNELS ON (CHANNELS.CHANNEL_ID = CHANNEL_SETTINGS.CHANNEL_ID) " +
                "WHERE CHANNEL_SETTINGS.CHANNEL_ID = ?", data.getChannel().getId());
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
    public RestAction<Void> updateWebhook(@Nonnull User eventAuthor, String webhookId) {

        String username = eventAuthor.getName();
        String userAvatarURL;

        if (eventAuthor.getAvatarUrl() != null)
            userAvatarURL = eventAuthor.getAvatarUrl();
        else
            userAvatarURL = eventAuthor.getDefaultAvatarUrl();

        Icon userAvatar = getUserIcon(userAvatarURL);

        return Thermostat.thermo
                .retrieveWebhookById(webhookId)
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

        User user = data.getMember().getUser();
        String username = user.getName();
        String userAvatarURL;

        if (user.getAvatarUrl() != null)
            userAvatarURL = user.getAvatarUrl();
        else
            userAvatarURL = user.getDefaultAvatarUrl();

        Icon userAvatar = getUserIcon(userAvatarURL);

        return data.getChannel()
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
                                        webhook.getId(), webhook.getToken(), data.getChannel().getId());
                            } catch (SQLException ex) {
                                ResponseDispatcher.commandFailed(this,
                                        ErrorEmbeds.error(ex.getLocalizedMessage(), Functions.getCommandId()),
                                        ex);
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

    @Override
    public GuildMessageReceivedEvent getEvent() {
        return data;
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
    public long getId() {
        return commandId;
    }
}
