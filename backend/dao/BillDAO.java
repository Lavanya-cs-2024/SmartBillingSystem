/**
 * BillDAO.java
 * 
 * PURPOSE:
 * Handles database operations for Bills table.
 * Creates bills, updates status, retrieves bill data.
 * 
 * WHAT HAPPENS WHEN BILL IS CREATED:
 * 1. Inserts into bills table (header)
 * 2. Gets auto-generated bill ID
 * 3. Inserts all items into bill_items table (details)
 * 
 * BILL NUMBER FORMAT:
 * "BILL-" + System.currentTimeMillis() 
 * Example: "BILL-1714567890123" (unique and timestamp-based)
 */

package backend.dao;

import backend.model.Bill;
import backend.model.BillItem;
import backend.util.DBConnection;
import java.sql.*;
import java.util.List;

public class BillDAO {
    
    // ========== CREATE BILL WITH ITEMS ==========
    /**
     * Creates a new bill with all its items
     * @param bill Bill header object
     * @param items List of BillItem objects
     * @return Generated bill ID, or -1 if failed
     * 
     * STEPS:
     * 1. Insert bill header
     * 2. Get generated bill ID
     * 3. Batch insert all bill items (more efficient than individual inserts)
     * 
     * BATCH INSERT: Groups multiple INSERTs into one network call
     * Much faster for inserting many items
     */
    public int createBill(Bill bill, List<BillItem> items) {
        // SQL for bill header
        String billSql = "INSERT INTO bills (bill_number, total_amount, status, qr_expiry) VALUES (?, ?, 'PENDING', ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(billSql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Set values for bill header
            ps.setString(1, bill.getBillNumber());
            ps.setDouble(2, bill.getTotalAmount());
            ps.setTimestamp(3, bill.getQrExpiry());
            
            ps.executeUpdate();
            
            // Get auto-generated bill ID
            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                int billId = generatedKeys.getInt(1);
                
                // SQL for bill items
                String itemSql = "INSERT INTO bill_items (bill_id, product_id, product_name, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement itemPs = conn.prepareStatement(itemSql);
                
                // Add each item to batch
                for (BillItem item : items) {
                    itemPs.setInt(1, billId);
                    itemPs.setInt(2, item.getProductId());
                    itemPs.setString(3, item.getProductName());
                    itemPs.setInt(4, item.getQuantity());
                    itemPs.setDouble(5, item.getUnitPrice());
                    itemPs.setDouble(6, item.getSubtotal());
                    itemPs.addBatch();  // Add to batch instead of executing now
                }
                
                itemPs.executeBatch();  // Execute all inserts at once
                itemPs.close();
                
                return billId;  // Return generated ID
            }
            
        } catch (SQLException e) {
            System.err.println("Error creating bill: " + e.getMessage());
        }
        return -1;
    }
    
    // ========== UPDATE BILL STATUS ==========
    /**
     * Updates bill status to PAID or FAILED
     * @param billId Bill to update
     * @param status New status ("PAID" or "FAILED")
     * @param reason "PAID" or "CANCELLED" (determines which timestamp to set)
     * @return true if update successful
     * 
     * WHY DIFFERENT SQL? Different timestamps for different statuses
     * PAID → sets payment_date
     * CANCELLED → sets cancelled_at
     */
    public boolean updateBillStatus(int billId, String status, String reason) {
        String sql;
        if (reason.equals("PAID")) {
            sql = "UPDATE bills SET status = 'PAID', payment_date = NOW() WHERE id = ? AND status = 'PENDING'";
        } else if (reason.equals("CANCELLED")) {
            sql = "UPDATE bills SET status = 'FAILED', cancelled_at = NOW() WHERE id = ? AND status = 'PENDING'";
        } else {
            return false;  // Invalid reason
        }
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, billId);
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating bill: " + e.getMessage());
            return false;
        }
    }
    
    // ========== GET BILL BY ID ==========
    /**
     * Retrieves bill header by ID
     * @param billId Bill ID to fetch
     * @return Bill object or null if not found
     * 
     * NOTE: This does NOT fetch bill items
     * For full bill with items, you need separate query
     */
    public Bill getBillById(int billId) {
        String sql = "SELECT * FROM bills WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, billId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                Bill bill = new Bill();
                bill.setId(rs.getInt("id"));
                bill.setBillNumber(rs.getString("bill_number"));
                bill.setBillDate(rs.getTimestamp("bill_date"));
                bill.setTotalAmount(rs.getDouble("total_amount"));
                bill.setStatus(rs.getString("status"));
                bill.setPaymentDate(rs.getTimestamp("payment_date"));
                bill.setQrExpiry(rs.getTimestamp("qr_expiry"));
                return bill;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching bill: " + e.getMessage());
        }
        return null;
    }
}