package thermostat.commands.utility;

import club.minnced.discord.webhook.WebhookClient;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.Thermostat;
import thermostat.commands.Command;
import thermostat.dispatchers.ResponseDispatcher;
import thermostat.embeds.Embeds;
import thermostat.mySQL.DataSource;
import thermostat.util.entities.CommandData;
import thermostat.util.enumeration.CommandType;
import thermostat.util.enumeration.EmbedType;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * This class manages Word Filtering Events if enabled by the user.
 * Principle: All "prohibited" words in a message get changed to the "nice" words.
 * Used to filter out slurs.
 */
@SuppressWarnings("ConstantConditions")
public class WordFilter implements Command {
    private static final Logger lgr = LoggerFactory.getLogger(WordFilter.class);
    private final CommandData data;
    private final List<String> message;

    private static List<String> badWords, niceWords;
    private static final Random random = new Random();

    public WordFilter(@Nonnull GuildMessageReceivedEvent data) {
        this.data = new CommandData(data);
        this.message = new ArrayList<>(Arrays.asList(data.getMessage().getContentRaw().split("\\s+")));

        checkThermoPermissionsAndQueue(this);
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
            try {
                DataSource.execute(conn -> {
                    data.event.getMessage().delete()
                            .reason("Inappropriate Language Filter")
                            .queue();

                    String webhookId, webhookToken;

                    webhookId = getWebhookValue(conn, "ID");
                    webhookToken = getWebhookValue(conn, "TOKEN");

                    if (webhookId.equals("0")) {
                        createWebhook(conn)
                                .map(webhook -> {
                                    try {
                                        sendWebhookMessage(getWebhookValue(conn, "ID"), getWebhookValue(conn, "TOKEN"));
                                        ResponseDispatcher.commandSucceeded(this, null);
                                    } catch (SQLException ex) {
                                        ResponseDispatcher.commandFailed(this, null, ex);
                                    }
                                    return webhook;
                                }).queue();
                    } else {
                        updateWebhook(data.event.getAuthor(), webhookId)
                                .map(webhook -> {
                                    sendWebhookMessage(webhookId, webhookToken);
                                    ResponseDispatcher.commandSucceeded(this, null);
                                    return webhook;

                                    // if something is wrong with the previous webhook, create a new one
                                }).queue(null, throwable -> createWebhook(conn));
                    }
                    return null;
                });
            } catch (SQLException ex) {
                ResponseDispatcher.commandFailed(this, null, ex);
            }
        }
    }

    /**
     * Retrieves a Webhook value from the database.
     * @param conn The connection to use to perform this action.
     * @param value The value of the Webhook to look for.
     * @return A webhook value depending on the argument.
     */
    public String getWebhookValue(final Connection conn, final String value) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("SELECT WEBHOOK_" + value + " FROM " +
                "CHANNEL_SETTINGS JOIN CHANNELS ON (CHANNELS.CHANNEL_ID = CHANNEL_SETTINGS.CHANNEL_ID) " +
                "WHERE CHANNEL_SETTINGS.CHANNEL_ID = ?");
        statement.setString(1, data.event.getChannel().getId());
        ResultSet rs = statement.executeQuery();
        rs.next();
        return rs.getString(1);
    }

    /**
     * Sends compiled message through the webhook provided.
     * @param webhookID Webhook Id
     * @param webhookToken Webhook Token
     */
    public void sendWebhookMessage(@Nonnull String webhookID, String webhookToken) {
        WebhookClient client = WebhookClient.withId(Long.parseLong(webhookID), webhookToken);
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
    public RestAction<Webhook> createWebhook(final Connection conn) {
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
                                PreparedStatement statement = conn.prepareStatement("UPDATE CHANNEL_SETTINGS JOIN CHANNELS ON " +
                                                "(CHANNELS.CHANNEL_ID = CHANNEL_SETTINGS.CHANNEL_ID) " +
                                                "SET CHANNEL_SETTINGS.WEBHOOK_ID = ?, " +
                                                "CHANNEL_SETTINGS.WEBHOOK_TOKEN = ? " +
                                                "WHERE CHANNEL_SETTINGS.CHANNEL_ID = ?");

                                statement.setString(1, webhook.getId());
                                statement.setString(2, webhook.getToken());
                                statement.setString(3, data.event.getChannel().getId());

                                statement.executeUpdate();
                            } catch (SQLException ex) {
                                ResponseDispatcher.commandFailed(this,
                                        Embeds.getEmbed(EmbedType.ERR, data, ex.getMessage()),
                                        ex);
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
    public CommandType getType() {
        return CommandType.WORDFILTEREVENT;
    }

    @Override
    public Logger getLogger() {
        return lgr;
    }

    @Override
    public CommandData getData() {
        return data;
    }
}
