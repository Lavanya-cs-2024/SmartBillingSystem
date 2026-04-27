
package backend.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection - Manages MySQL database connection
 * 
 * This class handles:
 * - Loading MySQL JDBC driver
 * - Creating connection to stationery_billing database
 * - Closing connections properly
 * 
 * Usage:
 *     Connection conn = DBConnection.getConnection();
 *     // do database operations
 *     DBConnection.closeConnection(conn);
 */
public class DBConnection {
    
    // Database connection parameters
    // ⚠️ IMPORTANT: Update these with your MySQL credentials
    private static final String URL = "jdbc:mysql://localhost:3306/stationery_billing";
    private static final String USERNAME = "root";   // Your MySQL username
    private static final String PASSWORD = "Lavanya*9106*";       // Your MySQL password (empty if none)
    
    /**
     * Establishes and returns connection to MySQL database
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Load MySQL JDBC driver (required for older Java versions)
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Create and return connection
            Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("✅ Database connected successfully!");
            return conn;
            
        } catch (ClassNotFoundException e) {
            System.out.println("❌ MySQL JDBC Driver not found!");
            System.out.println("   Make sure mysql-connector-j-9.7.0.jar is in lib folder");
            System.out.println("   And added to Java classpath");
            e.printStackTrace();
            throw new SQLException("JDBC Driver not found", e);
            
        } catch (SQLException e) {
            System.out.println("❌ Database connection failed!");
            System.out.println("   Check: MySQL is running?");
            System.out.println("   Check: Username/Password correct?");
            System.out.println("   Check: Database 'stationery_billing' exists?");
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Closes database connection safely
     * @param conn Connection to close
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                    System.out.println("✅ Database connection closed.");
                }
            } catch (SQLException e) {
                System.out.println("⚠️ Error closing connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Test method to verify connection works
     * Run this independently to check setup
     */
    public static void testConnection() {
        System.out.println("Testing database connection...");
        try {
            Connection conn = getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Connection test PASSED!");
                System.out.println("   Database: " + conn.getCatalog());
                System.out.println("   URL: " + URL);
                closeConnection(conn);
            }
        } catch (SQLException e) {
            System.out.println("❌ Connection test FAILED!");
            System.out.println("   Error: " + e.getMessage());
        }
    }
    
    /**
     * Main method for quick testing
     */
    public static void main(String[] args) {
        testConnection();
    }
}