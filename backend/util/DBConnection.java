
/**
 * DBConnection.java
 * 
 * PURPOSE:
 * Manages database connection using configuration from DBConfig.java
 * 
 * CHANGES MADE:
 * - Removed hardcoded password (now reads from DBConfig)
 * - Silent mode (no connection messages)
 * - Better error handling
 * 
 * TEAM SETUP:
 * 1. Copy DBConfig_TEMPLATE.java to DBConfig.java
 * 2. Update PASSWORD in DBConfig.java
 * 3. Run the application
 */

package backend.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    
    // ========== DATABASE CONFIGURATION ==========
    // Reads from DBConfig.java (each team member has their own)
    private static final String URL = DBConfig.URL;
    private static final String USERNAME = DBConfig.USERNAME;
    private static final String PASSWORD = DBConfig.PASSWORD;
    
    // Silent mode - set to false for debugging
    private static boolean silentMode = true;
    
    /**
     * Enable or disable console messages
     * @param silent true = no messages, false = show debug messages
     */
    public static void setSilentMode(boolean silent) {
        silentMode = silent;
    }
    
    /**
     * Establishes connection to MySQL database
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Create and return connection
            Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            
            if (!silentMode) {
                System.out.println("Database connected successfully!");
            }
            return conn;
            
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found! Make sure JAR is in lib/ folder", e);
        } catch (SQLException e) {
            // Provide helpful error message for common issues
            String errorMsg = e.getMessage();
            if (errorMsg.contains("Access denied")) {
                throw new SQLException("Wrong MySQL password! Check PASSWORD in DBConfig.java", e);
            } else if (errorMsg.contains("Unknown database")) {
                throw new SQLException("Database 'stationery_billing' not found! Run schema.sql first", e);
            } else if (errorMsg.contains("Connection refused")) {
                throw new SQLException("MySQL server not running! Start MySQL service first", e);
            }
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
                    if (!silentMode) {
                        System.out.println("Database connection closed.");
                    }
                }
            } catch (SQLException e) {
                // Silent fail - don't show error to user
            }
        }
    }
    
    /**
     * Tests database connection (for debugging)
     * Shows detailed error messages
     */
    public static void testConnection() {
        System.out.println("\nTesting database connection...");
        System.out.println("URL: " + URL);
        System.out.println("Username: " + USERNAME);
        System.out.println("Password: " + ("".equals(PASSWORD) ? "(empty)" : "********"));
        
        try {
            Connection conn = getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ CONNECTION SUCCESSFUL!");
                System.out.println("   Database: " + conn.getCatalog());
                closeConnection(conn);
            }
        } catch (SQLException e) {
            System.out.println("❌ CONNECTION FAILED!");
            System.out.println("   Error: " + e.getMessage());
            System.out.println("\nTroubleshooting tips:");
            System.out.println("   1. Is MySQL running?");
            System.out.println("   2. Did you create DBConfig.java from template?");
            System.out.println("   3. Is your MySQL password correct in DBConfig.java?");
            System.out.println("   4. Did you run schema.sql to create database?");
        }
    }
    
    /**
     * Main method for testing connection
     * Run: java backend.util.DBConnection
     */
    public static void main(String[] args) {
        testConnection();
    }
}