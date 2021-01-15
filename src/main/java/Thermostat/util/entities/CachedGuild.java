package thermostat.util.entities;

import javax.annotation.Nullable;

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
    public CachedGuild(String prefix) {
        this.synapse = null;
        this.prefix = prefix;
    }

    /**
     * Set a new prefix for the Guild.
     * @param prefix Guild's prefix.
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Set the Synapse object for a Guild.
     * @param guildId Guild's ID.
     * @return Newly set synapse.
     */
    public Synapse setSynapse(String guildId) {
        this.synapse = new Synapse(guildId);
        return this.synapse;
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
    @Nullable
    public Synapse getSynapse() {
        return synapse;
    }
}
