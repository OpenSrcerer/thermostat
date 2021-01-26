package thermostat.mySQL;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * A single Hikari data source for Thermostat.
 */
public abstract class DataSource {
    private static final Logger lgr = LoggerFactory.getLogger(DataSource.class);

    // Creates the single HikariDataSource instance
    private static HikariDataSource ds = null;

    public static void initializeDataSource() {
        HikariConfig config = new HikariConfig("/db.properties");
        ds = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        // Preparing for transactions
        /*Connection conn = ds.getConnection();
        conn.setAutoCommit(false);*/
        return ds.getConnection();
    }

    public static void closeDataSource() {
        if (ds != null) {
            ds.close();
        }
    }

    /**
     * Creates database entries for guilds/channels if they do not exist.
     * @param guildId ID of the guild to check.
     * @param channelId Channel of the guild to check.
     */
    public static void syncDatabase(final Connection conn, final String guildId, final String channelId) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("SELECT * FROM GUILDS WHERE GUILD_ID = ?");
        statement.setString(1, guildId);
        if (!statement.executeQuery().next()) {
            PreparedActions.createGuild(conn, guildId);
        }

        statement = conn.prepareStatement("SELECT * FROM CHANNELS WHERE CHANNEL_ID = ?");
        statement.setString(1, channelId);
        if (!statement.executeQuery().next()) {
            PreparedActions.createChannel(conn, guildId, channelId, 0);
        }
    }

    /**
     * Used to wrap database actions.
     * @param <X> Type of result to expect from Database.
     */
    @FunctionalInterface
    public interface DatabaseAction<X> {
        X doInConnection(Connection conn) throws SQLException;
    }

    /**
     * Perform an action on the database.
     * @param action Action to perform.
     * @param <X> Type of result to expect.
     * @return Database's result in given type.
     * @throws SQLException If something went wrong while
     * performing transaction.
     */
    public static <X> X execute(DatabaseAction<X> action) throws SQLException {
        try (Connection conn = getConnection()) {
            return action.doInConnection(conn);
        }
    }

    /**
     * Return an action that performs an update on the database.
     * @param sql SQL Code to run.
     * @param args Arguments to set.
     * @return An action that takes in arguments but returns nothing.
     */
    public static DataSource.DatabaseAction<Void> getAction(String sql, String... args) {
        return conn -> {
            PreparedStatement statement = conn.prepareStatement(sql);
            for (int index = 0; index < args.length; ++index) {
                statement.setString(index + 1, args[index]);
            }
            statement.executeUpdate();
            return null;
        };
    }
}