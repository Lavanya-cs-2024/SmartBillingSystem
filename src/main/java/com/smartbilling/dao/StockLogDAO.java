package com.smartbilling.dao;

import com.smartbilling.util.DBConnection;
import java.sql.*;
import java.sql.Types;

public class StockLogDAO {
    
    public void logStockChange(int productId, Integer billId, int quantityChange, 
                               int stockBefore, int stockAfter, String reason) {
        String sql = "INSERT INTO stock_logs (product_id, bill_id, quantity_change, stock_before, stock_after, reason) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            if (billId != null) {
                ps.setInt(2, billId);
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setInt(3, quantityChange);
            ps.setInt(4, stockBefore);
            ps.setInt(5, stockAfter);
            ps.setString(6, reason);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error logging stock: " + e.getMessage());
        }
    }
}