package Thermostat.MySQL;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Class that creates a single Hikari
 * data source for our Thermostat application.
 */
public class DataSource {

    private static HikariDataSource ds;

    static {
        String configFile = "/db.properties";
        HikariConfig config = new HikariConfig(configFile);
        ds = new HikariDataSource(config);
    }

    public DataSource() {}

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    /**
     * Function that performs a data-grabbing query
     * upon the database linked.
     * @param Query A string containing the query that will be
     *              executed.
     * @return A ResultSet that contains the information
     * for the specified query. Returns an empty ResultSet
     * if the provided query did not return any results.
     */
    public static ArrayList<String> query (String Query)
    {
        ArrayList<String> resultArray = null;

        try (
                Connection conn = DataSource.getConnection();
                PreparedStatement pst = conn.prepareStatement(
                        Query,
                        ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_UPDATABLE
                );
                ResultSet rs = pst.executeQuery()
        ) {
            resultArray = new ArrayList<>();
            while (rs.next())
            {
                resultArray.add(rs.getString(1));
            }
        } catch (SQLException ex) {
            Logger lgr = LoggerFactory.getLogger(ds.getClass());
            lgr.error(ex.getMessage(), ex);
        }
        return resultArray;
    }

    /**
     * Same as above, returns single boolean value.
     */
    public static boolean queryBool (String Query)
    {
        try (
                Connection conn = DataSource.getConnection();
                PreparedStatement pst = conn.prepareStatement(
                        Query,
                        ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_UPDATABLE
                );
                ResultSet rs = pst.executeQuery()
        ) {
            rs.next();

            return rs.getBoolean(1);
        } catch (SQLException ex) {
            Logger lgr = LoggerFactory.getLogger(ds.getClass());
            lgr.error(ex.getMessage(), ex);
        }
        return false;
    }

    /**
     * Same as above, returns single int value.
     */
    public static float querySens (String Query)
    {
        float retVal = 0;

        try (
                Connection conn = DataSource.getConnection();
                PreparedStatement pst = conn.prepareStatement(
                        Query,
                        ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_UPDATABLE
                );
                ResultSet rs = pst.executeQuery()
        ) {
            if (rs.next())
            {
                retVal = rs.getFloat(1);
            } else {
                return retVal;
            }
        } catch (SQLException ex) {
            Logger lgr = LoggerFactory.getLogger(ds.getClass());
            lgr.error(ex.getMessage(), ex);
        }
        return retVal;
    }

    /**
     * Same as above, returns single int value.
     */
    public static int queryInt (String Query)
    {
        int retVal = -1;

        try (
                Connection conn = DataSource.getConnection();
                PreparedStatement pst = conn.prepareStatement(
                        Query,
                        ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_UPDATABLE
                );
                ResultSet rs = pst.executeQuery()
        ) {
            if (rs.next())
            {
                retVal = rs.getInt(1);
            } else {
                return retVal;
            }
        } catch (SQLException ex) {
            Logger lgr = LoggerFactory.getLogger(ds.getClass());
            lgr.error(ex.getMessage(), ex);
        }
        return retVal;
    }

    /**
     * Same as above, returns single String value.
     */
    public static String queryString (String Query)
    {
        String retString = null;

        try (
                Connection conn = DataSource.getConnection();
                PreparedStatement pst = conn.prepareStatement(
                        Query,
                        ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_UPDATABLE
                );
                ResultSet rs = pst.executeQuery()
        ) {
            if (rs.next())
            {
                retString = rs.getString(1);
            } else {
                return null;
            }
        } catch (SQLException ex) {
            Logger lgr = LoggerFactory.getLogger(ds.getClass());
            lgr.error(ex.getMessage(), ex);
        }
        return retString;
    }

    /**
     * Function that performs a data-changing query
     * upon the database.
     * @param Query A string containing the update that will be
     *              executed.
     * @throws SQLException Error while executing update.
     */
    public static void update (String Query) throws SQLException
    {
        Connection conn = DataSource.getConnection();
        PreparedStatement pst = conn.prepareStatement(Query);
        pst.executeUpdate();

        pst.close();
        conn.close();
    }

    /**
     * Looks up whether a certain ResultSet is empty.
     * @param Query A string containing the query that will be
     *              executed.
     * @return True if ResultSet had data on it, false if it was empty.
     */
    public static boolean checkDatabaseForData (String Query) throws SQLException
    {

        try (
                Connection conn = DataSource.getConnection();
                PreparedStatement pst = conn.prepareStatement(Query);
                ResultSet rs = pst.executeQuery()
        ) {
            if (!rs.next())
                return false;
        }
        return true;
    }
}