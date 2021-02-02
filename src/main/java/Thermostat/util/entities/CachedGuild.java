package thermostat.util.entities;

import javax.annotation.Nonnull;

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
     * Create a new GuildData object for use in the cache.
     * @param prefix Guild's prefix.
     */
    public CachedGuild(final String guildId, final String prefix) {
        this.synapse = new Synapse(guildId);
        this.prefix = prefix;
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
    public String getPrefix() {
        return prefix;
    }

    /**
     * Get this Guild's cached synapse.
     * @return This Guild's synapse. Null if synapse is not set.
     */
    @Nonnull
    public Synapse getSynapse() {
        return synapse;
    }
}
