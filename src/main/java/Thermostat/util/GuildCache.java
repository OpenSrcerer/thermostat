package thermostat.util;

import thermostat.mySQL.DataSource;
import thermostat.util.entities.CachedGuild;
import thermostat.util.entities.Synapse;

import javax.annotation.Nonnull;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * A cache utility class that stores
 * specific information for every Guild.
 */
public class GuildCache {

    /**
     * The HashMap where CachedGuilds are stored.
     * K: Guild ID -> V: Cached Guild Data
     */
    private static final Map<String, CachedGuild> cache = new WeakHashMap<>();

    /**
     * Fill the cache with CachedGuilds.
     * @throws SQLException Error while retrieving information
     * in the database.
     */
    public static void initializeCache() throws SQLException {
        DataSource.execute(conn -> {
            PreparedStatement statement = conn.prepareStatement("SELECT GUILD_ID, GUILD_PREFIX FROM GUILDS");
            ResultSet rs = statement.executeQuery();

            String guildId, guildPrefix;
            while (rs.next()) {
                guildId = rs.getString(1);
                guildPrefix = rs.getString(2);

                CachedGuild guildData = new CachedGuild(guildPrefix);
                cache.put(guildId, guildData);
            }
            return null;
        });
    }

    /**
     * Get the prefix of a Guild.
     * If the Guild has not been cached before, it will be cached prior to returning the prefix.
     * @param guildId ID of Guild.
     * @return Guild's prefix. Null if Guild is not cached.
     */
    @Nonnull
    public static String getPrefix(final String guildId) {
        CachedGuild guild = cache.get(guildId); // Try to retrieve the guild from the cache
        if (guild == null) { // Guild hasn't been cached.
            String prefix = retrievePrefix(guildId); // Retrieve the prefix of the Guild.

            if (prefix == null) { // If the prefix is not set, use the default prefix.
                prefix = Constants.DEFAULT_PREFIX;
            }

            add(guildId, prefix); // Add the Guild to the cache.
            return prefix; // Return the new/default prefix.
        }
        return guild.getPrefix(); // Return the Guild's prefix.
    }

    /**
     * Change the prefix of a Guild in the guildCache to a new one.
     * If the Guild has not been cached before, it will be cached.
     * @param guildId ID of Guild.
     * @param prefix The new prefix to assign.
     */
    public static void assignPrefix(final String guildId, final String prefix) {
        CachedGuild guild = cache.get(guildId);
        if (guild == null) {
            add(guildId, prefix);
            return;
        }
        guild.setPrefix(prefix);
    }

    /**
     * Set the Synapse object for a Guild.
     * @param guildId ID of guild.
     * @return Newly set Synapse.
     */
    @Nonnull
    public static Synapse getSynapse(final String guildId) {
        CachedGuild guild = cache.get(guildId);
        if (guild == null) {
            guild = add(guildId, null);
        }
        return guild.getSynapse(guildId);
    }

    /**
     * Puts a Guild in the GuildCache cache.
     * @param guildId Guild's ID.
     * @return Newly created CachedGuild object.
     */
    @Nonnull
    public static CachedGuild add(final String guildId, final String guildPrefix) {
        CachedGuild guildData = new CachedGuild(guildPrefix);
        cache.put(guildId, guildData);
        return guildData;
    }

    /**
     * Removes a Guild from the cache.
     * @param guildId Guild's ID.
     */
    public static void expungeGuild(final String guildId) {
        cache.remove(guildId);
    }

    /**
     * Retrieves a Prefix for a Guild from the database.
     * @param guildId ID of Guild to lookup prefix for.
     * @return Prefix of said guild.
     */
    private static String retrievePrefix(final String guildId) {
        try {
            return DataSource.execute(conn -> {
                PreparedStatement query = conn.prepareStatement("SELECT GUILD_PREFIX FROM GUILDS WHERE GUILD_ID = ?");
                query.setString(1, guildId);
                ResultSet rs = query.executeQuery();

                if (rs.next()) {
                    return rs.getString(1);
                } else {
                    return null;
                }
            });
        } catch (Exception ex) {
            return null; // Something went wrong with getting the prefix, so just return null!
        }
    }
}
