package thermostat.util;

import thermostat.mySQL.DataSource;
import thermostat.util.entities.CachedGuild;
import thermostat.util.entities.Synapse;

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
     * The HashMap where GuildCacheData objects are stored.
     * K: Guild ID -> V: GuildCacheData
     */
    private static final Map<String, CachedGuild> cache = new WeakHashMap<>();

    /**
     * Fill the cache with GuildCacheData objects. These
     * objects are initially given non-lazy loaded values.
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

        /* TO BE REMOVED
        Runnable monitorAction = () -> {
            Synapse synapse;
            for (Map.Entry<String, CachedGuild> guild : cache.entrySet()) {
                synapse = guild.getValue().getSynapse();
                if (synapse != null) {
                    if (guild.getValue().getSynapse().getState() == SynapseState.ACTIVE) {
                        new SynapseMonitor(guild.getValue().getSynapse());
                    }
                }
            }
        };

        Thermostat.executor.scheduleAtFixedRate(monitorAction, 0, 8, TimeUnit.SECONDS);*/
    }

    /**
     * Get the prefix of a Guild.
     * @param guildId ID of Guild.
     * @return Guild's prefix. Null if Guild is not cached.
     */
    public static String getPrefix(String guildId) {
        CachedGuild guild = cache.get(guildId);
        if (guild == null) {
            return null;
        }
        return guild.getPrefix();
    }

    /**
     * Get the synapse of a Guild.
     * @param guildId ID of Guild.
     * @return Guild's Synapse. Null if Guild is not cached.
     */
    public static Synapse getSynapse(String guildId) {
        CachedGuild guild = cache.get(guildId);
        if (guild == null) {
            guild = cacheGuild(guildId, null);
        }
        return guild.getSynapse();
    }

    /**
     * Set the Synapse object for a Guild.
     * @param guildId ID of guild.
     * @return Newly set synapse.
     */
    public static Synapse setSynapse(String guildId) throws RuntimeException {
        CachedGuild guild = cache.get(guildId);
        if (guild != null) {
            return guild.setSynapse(guildId);
        }
        throw new RuntimeException("Guild was not found in cache.");
    }

    /**
     * Caches a Guild.
     * @param guildId Guild's ID.
     */
    public static CachedGuild cacheGuild(String guildId, String guildPrefix) {
        CachedGuild guildData = new CachedGuild(guildPrefix);
        cache.put(guildId, guildData);
        return guildData;
    }

    /**
     * Removes a Guild from the cache.
     * @param guildId Guild's ID.
     */
    public static void expungeGuild(String guildId) {
        cache.remove(guildId);
    }
}
