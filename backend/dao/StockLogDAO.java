/**
 * StockLogDAO.java
 * 
 * PURPOSE:
 * Handles stock audit logging with FIXED lock timeout issue.
 * 
 * ISSUE FIXED:
 * "Lock wait timeout exceeded" error occurred because:
 * - Multiple transactions were trying to write to stock_logs simultaneously
 * - Previous transaction hadn't committed yet
 * 
 * SOLUTION:
 * 1. Added retry mechanism (3 attempts)
 * 2. Added small delay between retries
 * 3. Each attempt uses fresh connection
 * 4. Better exception handling
 */

package backend.dao;

import backend.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class StockLogDAO {
    
    // Maximum number of retry attempts for lock timeout
    private static final int MAX_RETRIES = 3;
    // Delay between retries in milliseconds
    private static final int RETRY_DELAY_MS = 500;
    
    /**
     * Logs stock change with retry mechanism for lock timeout
     * 
     * @param productId Product that changed
     * @param billId Related bill (null if not from sale)
     * @param quantityChange Amount changed (negative for sale)
     * @param stockBefore Stock level before change
     * @param stockAfter Stock level after change
     * @param reason Why change happened (SALE, RESTOCK, etc.)
     * 
     * RETRY LOGIC:
     * - Attempt 1: Try immediately
     * - If lock timeout, wait 500ms and try again
     * - Max 3 attempts before giving up
     */
    public void logStockChange(int productId, Integer billId, int quantityChange, 
                               int stockBefore, int stockAfter, String reason) {
        
        String sql = "INSERT INTO stock_logs (product_id, bill_id, quantity_change, stock_before, stock_after, reason) VALUES (?, ?, ?, ?, ?, ?)";
        
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                
                // Set product ID
                ps.setInt(1, productId);
                
                // Handle bill_id (could be null for restock)
                if (billId != null) {
                    ps.setInt(2, billId);
                } else {
                    ps.setNull(2, Types.INTEGER);
                }
                
                // Set other values
                ps.setInt(3, quantityChange);
                ps.setInt(4, stockBefore);
                ps.setInt(5, stockAfter);
                ps.setString(6, reason);
                
                ps.executeUpdate();
                return; // Success - exit method
                
            } catch (SQLException e) {
                // Check if this is a lock timeout error
                if (e.getMessage().contains("Lock wait timeout") && attempt < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS); // Wait before retry
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    // Other error or max retries reached - log but don't crash
                    System.err.println("Error logging stock change (attempt " + attempt + "): " + e.getMessage());
                    return;
                }
            }
        }
    }
}