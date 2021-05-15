package thermostat.util.entities;

import club.minnced.discord.webhook.WebhookClient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Cache object. Struct-like, merely for storage.
 */
public class CachedGuild {
    /**
     * A specific Synapse of a Guild.
     */
    private Synapse synapse;

    /**
     * The global prefix of a Guild.
     */
    private String prefix;

    /**
     * Filtering Rules for this Guild.
     */
    private WordReplacer rule;

    /**
     * Stores Webhook clients for every channel.
     */
    private final Map<String, WebhookClient> webhookClients = new HashMap<>();

    /**
     * Create a new GuildData object for use in the cache.
     * @param prefix Guild's prefix.
     */
    public CachedGuild(final String prefix) {
        this.synapse = null;
        this.prefix = prefix;

        try {
            this.rule = new WordReplacer();
        } catch (SQLException ex) {

        }
    }

    /**
     * Set a new prefix for the Guild.
     * @param prefix Guild's prefix.
     */
    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    /**
     * Get this Guild's cached prefix.
     * @return This Guild's prefix.
     */
    @Nullable
    public String getPrefix() {
        return prefix;
    }

    /**
     * Get this Guild's cached synapse. If the Synapse has not been initialized, it gets so and then returned.
     * @return This Guild's Synapse.
     */
    @Nonnull
    public Synapse getSynapse(final String guildId) {
        if (this.synapse == null) {
            this.synapse = new Synapse(guildId);
        }
        return this.synapse;
    }

    /**
     * @param channelId ID of channel to find the WebhookClient for.
     * @param webhookId ID of the Webhook (in case the client hasn't been created).
     * @param webhookToken Token of the Webhook (in case the client hasn't been created).
     * @return Return the WebhookClient for a channel. Makes one if it is not created.
     */
    @Nonnull
    public WebhookClient getClient(final String channelId, final String webhookId, final String webhookToken) {
        WebhookClient client = webhookClients.get(channelId);
        long webhookIdLong = Long.parseLong(webhookId);

        if (client == null) { // NPE dupe code :( :(
            WebhookClient newClient = WebhookClient.withId(webhookIdLong, webhookToken);
            webhookClients.put(channelId, newClient);
            client = newClient;
        } else if (client.getId() != webhookIdLong) {
            WebhookClient newClient = WebhookClient.withId(webhookIdLong, webhookToken);
            webhookClients.put(channelId, newClient);
            client = newClient;
        }
        return client;
    }
}
