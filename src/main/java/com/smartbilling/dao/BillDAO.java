package com.smartbilling.dao;

import com.smartbilling.model.Bill;
import com.smartbilling.model.BillItem;
import com.smartbilling.util.DBConnection;
import java.sql.*;
import java.util.List;

public class BillDAO {
    
    public int createBill(Bill bill, List<BillItem> items) {
        String billSql = "INSERT INTO bills (bill_number, total_amount, status, qr_expiry) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(billSql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, bill.getBillNumber());
            ps.setDouble(2, bill.getTotalAmount());
            ps.setString(3, bill.getStatus());
            ps.setTimestamp(4, bill.getQrExpiry());
            ps.executeUpdate();
            
            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                int billId = generatedKeys.getInt(1);
                
                String itemSql = "INSERT INTO bill_items (bill_id, product_id, product_name, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement itemPs = conn.prepareStatement(itemSql);
                
                for (BillItem item : items) {
                    itemPs.setInt(1, billId);
                    itemPs.setInt(2, item.getProductId());
                    itemPs.setString(3, item.getProductName());
                    itemPs.setInt(4, item.getQuantity());
                    itemPs.setDouble(5, item.getUnitPrice());
                    itemPs.setDouble(6, item.getSubtotal());
                    itemPs.addBatch();
                }
                
                itemPs.executeBatch();
                itemPs.close();
                return billId;
            }
            
        } catch (SQLException e) {
            System.err.println("Error creating bill: " + e.getMessage());
        }
        return -1;
    }
    
    public boolean updateBillStatus(int billId, String status, String reason) {
        String sql;
        
        if (reason.equals("PAID")) {
            sql = "UPDATE bills SET status = 'PAID', payment_date = NOW() WHERE id = ? AND status = 'PENDING'";
        } else if (reason.equals("CANCELLED")) {
            sql = "UPDATE bills SET status = 'FAILED', cancelled_at = NOW() WHERE id = ? AND status = 'PENDING'";
        } else {
            return false;
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
                bill.setCancelledAt(rs.getTimestamp("cancelled_at"));
                bill.setExpiredAt(rs.getTimestamp("expired_at"));
                return bill;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching bill: " + e.getMessage());
        }
        return null;
    }
}