package thermostat.mySQL;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * Queries DB for a string array.
     *
     * @param Query A string containing a query that will be
     *              executed. Expected two values.
     * @return A String Array that contains the information
     * for the specified query. Returns an empty ResultSet
     * if the provided query did not return any results.
     */
    @Nonnull
    @Deprecated
    public static Map<String, Integer> queryMap(String Query, String argument) throws SQLException {
        Map<String, Integer> resultMap = new LinkedHashMap<>();

        try (
            Connection conn = getConnection();
            PreparedStatement pst = conn.prepareStatement(
                    Query,
                    ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE
            )
        ) {
            pst.setString(1, argument);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                resultMap.put(rs.getString(1), rs.getInt(2));
            }
        }

        return resultMap;
    }

    /**
     * Queries DB for a string array.
     *
     * @param Query A string containing the query that will be
     *              executed.
     * @return A String Array that contains the information
     * for the specified query. Returns an empty ResultSet
     * if the provided query did not return any results.
     */
    @Nullable
    @Deprecated
    public static ArrayList<String> queryStringArray(String Query, String argument) {
        ArrayList<String> resultArray = null;

        try (
                Connection conn = getConnection();
                PreparedStatement pst = conn.prepareStatement(
                        Query,
                        ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_UPDATABLE
                )
        ) {
            if (!argument.isEmpty())
                pst.setString(1, argument);

            ResultSet rs = pst.executeQuery();

            resultArray = new ArrayList<>();
            while (rs.next()) {
                resultArray.add(rs.getString(1));
            }
        } catch (SQLException ex) {
            lgr.error(ex.getMessage(), ex);
        }
        return resultArray;
    }

    @Deprecated
    public static boolean queryBool(String Query, List<String> args) {
        try (
                Connection conn = getConnection();
                PreparedStatement pst = conn.prepareStatement(
                        Query,
                        ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_UPDATABLE
                )
        ) {
            for (int it = 0; it < args.size(); ++it) {
                pst.setString(it + 1, args.get(it));
            }
            ResultSet rs = pst.executeQuery();
            rs.next();

            return rs.getBoolean(1);
        } catch (SQLException ex) {
            lgr.error(ex.getMessage(), ex);
        }
        return false;
    }

    /**
     * Same as above, returns single int value.
     */
    @Deprecated
    public static float querySens(String Query, String argument) {
        float retVal = 0;

        try (
                Connection conn = getConnection();
                PreparedStatement pst = conn.prepareStatement(
                        Query,
                        ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_UPDATABLE
                )
        ) {
            pst.setString(1, argument);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                retVal = rs.getFloat(1);
            } else {
                return retVal;
            }
        } catch (SQLException ex) {
            lgr.error(ex.getMessage(), ex);
        }
        return retVal;
    }

    /**
     * Same as above.
     */
    @Deprecated
    public static int queryInt(String Query, String argument) {
        int retVal = -1;

        try (
                Connection conn = getConnection();
                PreparedStatement pst = conn.prepareStatement(
                        Query,
                        ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_UPDATABLE
                )
        ) {
            pst.setString(1, argument);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                retVal = rs.getInt(1);
            } else {
                return retVal;
            }
        } catch (SQLException ex) {
            lgr.error(ex.getMessage(), ex);
        }
        return retVal;
    }

    /**
     * Same as above.
     */
    @Deprecated
    public static List<Integer> queryInts(String Query, String argument) {
        List<Integer> retVal = new ArrayList<>();

        try (
                Connection conn = getConnection();
                PreparedStatement pst = conn.prepareStatement(
                        Query,
                        ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_UPDATABLE
                )
        ) {
            pst.setString(1, argument);
            ResultSet rs = pst.executeQuery();

            rs.next();
            retVal.add(rs.getInt(1));
            retVal.add(rs.getInt(2));

        } catch (SQLException ex) {
            lgr.error(ex.getMessage(), ex);
        }
        return retVal;
    }

    /**
     * Same as above, returns single String value.
     */
    @Nullable
    @Deprecated
    public static String queryString(String Query, String argument) throws SQLException {
        String retString;

        try (
            Connection conn = getConnection();
            PreparedStatement pst = conn.prepareStatement(
                    Query,
                    ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE
            )
        ) {
            pst.setString(1, argument);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                retString = rs.getString(1);
            } else {
                return null;
            }
        }

        return retString;
    }

    /**
     * @param channelId Channel ID for elements of the Settings command
     *              information package.
     * @return A String Array that contains the information
     * for the Settings command. Returns an empty List
     * if the provided query did not return any results.
     */
    @Nonnull
    @Deprecated
    public static List<Object> getSettingsPackage(String channelId) throws SQLException {
        List<Object> resultArray;

        try (
             Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(
                     "SELECT MIN_SLOW, MAX_SLOW, SENSOFFSET, MONITORED, FILTERED " +
                             "FROM CHANNEL_SETTINGS WHERE CHANNEL_ID = ?",
                     ResultSet.TYPE_SCROLL_SENSITIVE,
                     ResultSet.CONCUR_UPDATABLE
             )
        ) {
            pst.setString(1, channelId);
            ResultSet rs = pst.executeQuery();

            resultArray = new ArrayList<>();
            // Min
            rs.next();
            resultArray.add(rs.getInt(1));
            // Max
            resultArray.add(rs.getInt(2));
            // Sens
            resultArray.add(rs.getFloat(3));
            // Monitored
            resultArray.add(rs.getBoolean(4));
            // Filtered
            resultArray.add(rs.getBoolean(5));
        }

        return resultArray;
    }

    /**
     * Function that performs a data-changing query
     * upon the database.
     *
     * @param Query A string containing the update that will be
     *              executed.
     * @throws SQLException Error while executing update.
     */
    @Deprecated
    public static void update(String Query, String... args) throws SQLException {
        try (
             Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(Query)
        ) {
            for (int it = 0; it < args.length; ++it) {
                pst.setString(it + 1, args[it]);
            }
            pst.executeUpdate();
        }
    }

    @Deprecated
    public static void update(String Query, String argument) throws SQLException {
        try (
             Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(Query)
        ) {
            pst.setString(1, argument);
            pst.executeUpdate();
        }
    }

    /**
     * Looks up whether a certain ResultSet is empty.
     *
     * @param Query A string containing the query that will be
     *              executed.
     * @return True if ResultSet had data on it, false if it was empty.
     */
    @Deprecated
    public static boolean checkDatabaseForData(String Query, String argument) throws SQLException {
        try (
                Connection conn = getConnection();
                PreparedStatement pst = conn.prepareStatement(Query)
        ) {
            pst.setString(1, argument);
            ResultSet rs = pst.executeQuery();

            if (!rs.next())
                return false;
        }
        return true;
    }
}