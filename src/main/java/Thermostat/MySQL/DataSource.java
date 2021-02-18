package thermostat.mySQL;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.dv8tion.jda.api.entities.Guild;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A single Hikari data source for Thermostat.
 */
public abstract class DataSource {
    // Creates the single HikariDataSource instance
    private static HikariDataSource ds = null;

    public static void initializeDataSource() {
        HikariConfig config = new HikariConfig("/db.properties");
        ds = new HikariDataSource(config);
    }

    private static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public static void closeDataSource() {
        if (ds != null) {
            ds.close();
        }
    }

    /**
     * Creates database entries for guilds/channels if they do not exist.
     * @param conn Connection to perform this operation on.
     * @param guild Guild to check.
     */
    public static void syncDatabase(final Connection conn, final Guild guild) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("SELECT * FROM GUILDS WHERE GUILD_ID = ?");
        statement.setString(1, guild.getId());
        if (!statement.executeQuery().next()) {
            PreparedActions.createGuild(conn, guild.getId());
        }

        Set<String> channelIds = new HashSet<>();
        guild.getChannels().forEach(e -> channelIds.add(e.getId()));

        PreparedActions.createChannels(conn, guild.getId(), channelIds, 0);
    }

    /**
     * Used to wrap database actions that return a value.
     * @param <X> Type of result to expect from Database.
     */
    @FunctionalInterface
    public interface DatabaseAction<X> {
        X doInConnection(final Connection conn) throws SQLException;
    }

    /**
     * Query the database and return a value.
     * @param action Action to perform.
     * @param <X> Type of result to expect.
     * @return Database's result in given type.
     * @throws SQLException If something went wrong while
     * performing transaction.
     */
    public static <X> X demand(DatabaseAction<X> action) throws SQLException {
        Objects.requireNonNull(action);
        try (final Connection conn = getConnection()) {
            return action.doInConnection(conn);
        }
    }
}