package com.smartbilling.service;

import com.smartbilling.dao.BillDAO;
import com.smartbilling.dao.ProductDAO;
import com.smartbilling.dao.StockLogDAO;
import com.smartbilling.model.Bill;
import com.smartbilling.model.BillItem;
import com.smartbilling.util.DBConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class BillingService {
    
    private BillDAO billDAO = new BillDAO();
    private ProductDAO productDAO = new ProductDAO();
    private StockLogDAO stockLogDAO = new StockLogDAO();

    public int generateBill(List<BillItem> cart, double total) {
        if (cart.isEmpty()) return -1;
        
        String billNumber = "BILL-" + System.currentTimeMillis();
        Timestamp expiry = new Timestamp(System.currentTimeMillis() + 3 * 60 * 1000);
        
        Bill bill = new Bill(billNumber, total, "PENDING");
        bill.setQrExpiry(expiry);
        
        return billDAO.createBill(bill, cart);
    }

    public boolean processPayment(int billId, List<BillItem> cart) {
        Connection conn = null;
        
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            boolean billUpdated = billDAO.updateBillStatus(billId, "PAID", "PAID");
            if (!billUpdated) {
                conn.rollback();
                return false;
            }
            
            boolean stockReduced = productDAO.reduceStockBatch(conn, cart);
            if (!stockReduced) {
                conn.rollback();
                return false;
            }
            
            for (BillItem item : cart) {
                int before = productDAO.getCurrentStock(conn, item.getProductId());
                int after = before - item.getQuantity();
                stockLogDAO.logStockChange(
                    item.getProductId(), billId, -item.getQuantity(), 
                    before, after, "SALE"
                );
            }
            
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) {}
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) {}
            }
        }
    }

    public boolean cancelBill(int billId) {
        return billDAO.updateBillStatus(billId, "FAILED", "CANCELLED");
    }
    
    // ADD THIS METHOD - To get bill details after payment
    public Bill getBillById(int billId) {
        return billDAO.getBillById(billId);
    }
}