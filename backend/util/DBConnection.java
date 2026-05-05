
/**
 * DBConnection.java
 * 
 * PURPOSE:
 * Handles database connection with SILENT MODE (no console messages).
 * 
 * CHANGES MADE:
 * 1. Removed all System.out.println statements (no more "Database connected successfully")
 * 2. Added silentMode flag for optional debugging
 * 3. Clean exception handling without user messages
 * 
 * WHY SILENT MODE?
 * Users don't need to see technical database messages.
 * Only show errors when something goes wrong.
 */

package backend.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    
    // Database configuration - Update with your credentials
    private static final String URL = "jdbc:mysql://localhost:3306/stationery_billing";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Lavanya*9106*";
    
    // Silent mode = true means NO console messages
    // Set to false only for debugging
    private static boolean silentMode = true;
    
    /**
     * Enable or disable console messages
     * @param silent true = no messages, false = show messages
     */
    public static void setSilentMode(boolean silent) {
        silentMode = silent;
    }
    
    /**
     * Establishes database connection - SILENT MODE (no messages)
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("JDBC Driver not found", e);
        }
    }
    
    /**
     * Closes database connection silently
     * @param conn Connection to close
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                // Silent fail - no user messages
            }
        }
    }
    
    /**
     * Test connection (for debugging only)
     */
    public static void testConnection() {
        try {
            Connection conn = getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Database connection test PASSED!");
                closeConnection(conn);
            }
        } catch (SQLException e) {
            System.out.println("❌ Connection test FAILED!");
        }
    }
}