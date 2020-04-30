package Thermostat.MySQL;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * <h1>Connection</h1>
 * <p>The main driver class for the MySQL
 * database that this bot works with.
 * Includes functions that create a connection
 * when needed, look up data from queries
 * or perform table changing operations.
 */

public class Connection
{
    private java.sql.Connection conn = null;

    /**
     * Constructor for the connection. Simply
     * initiates a connection to the provided database.
     * Login information is kept hardcoded due to safety
     * reasons.
     */
    public Connection() throws SQLException
    {
        conn = 
        //            DriverManager.getConnection(
        //            "jdbc:mysql://servername:port/db_name"
        //            "DB_USERNAME"
        //            "DB_PASSWORD"
        );
    }

    /**
     * Function that performs a data-grabbing query
     * upon the database linked to the instance of
     * Connection.
     * @param Query A string containing the query that will be
     *              executed.
     * @return A ResultSet that contains the information
     * for the specified query. Returns an empty ResultSet
     * if the provided query did not return any results.
     */
    public ResultSet query (String Query)
    {
        ResultSet rs = null;
        try
        {
            Statement stmt;
            stmt = conn.createStatement();
            rs = stmt.executeQuery(Query);
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
            if (ex.toString().startsWith("java.sql.SQLException: null,  message from server: \"Host 'localhost' is not allowed"))
            {
                try
                {
                    System.out.println("Exception was thrown!");
                    Thread.sleep(1000);
                    query(Query);
                    System.out.println("Handled.");
                } catch (InterruptedException xx)
                {
                    xx.printStackTrace();
                }
            }
        }
        return rs;
    }

    /**
     * Function that performs a data-changing query
     * upon the database linked to the instance of
     * Connection, like ALTER TABLE.
     * @param Query A string containing the query that will be
     *              executed.
     * @throws SQLException
     */
    public void update (String Query) throws SQLException
    {
        Statement stmt;
        stmt = conn.createStatement();
        stmt.executeUpdate(Query);
    }

    /**
     * Looks up whether a certain ResultSet is empty.
     * @param Query A string containing the query that will be
     *              executed.
     * @return True if ResultSet had data on it, false if it was empty.
     * @throws SQLException
     */
    public boolean checkDatabaseForData (String Query)
    {
        try
        {
            ResultSet rs = query(Query);
            rs.beforeFirst();

            if (!rs.next())
                return false;
        }
        catch (SQLException ex)
        {
            System.out.println(ex + "(CheckDBData)");
        }

        return true;
    }

    /**
     * Closes the connection with the database
     * created in {@link Connection}.
     */
    public void closeConnection()
    {
        try
        {
            conn.close();
        }
        catch (Exception ex)
        {
            System.out.println(ex + "(Could not close conn)");
        }
    }
}
