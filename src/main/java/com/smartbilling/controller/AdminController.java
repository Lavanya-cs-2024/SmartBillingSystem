package com.smartbilling.controller;

import com.smartbilling.model.Product;
import com.smartbilling.model.Bill;
import com.smartbilling.dao.ProductDAO;
import com.smartbilling.dao.BillDAO;
import com.smartbilling.util.DBConnection;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:8080")
public class AdminController {

    private ProductDAO productDAO = new ProductDAO();
    private BillDAO billDAO = new BillDAO();

    @GetMapping("/products")
    public List<Product> getAllProducts() {
        return productDAO.getAllProducts();
    }

    @PostMapping("/products")
    public Map<String, Boolean> addProduct(@RequestBody Product product) {
        String sql = "INSERT INTO products (name, category_id, description, price, stock_quantity, reorder_level) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getName());
            ps.setInt(2, product.getCategoryId());
            ps.setString(3, product.getDescription() != null ? product.getDescription() : "");
            ps.setDouble(4, product.getPrice());
            ps.setInt(5, product.getStockQuantity());
            ps.setInt(6, product.getReorderLevel() > 0 ? product.getReorderLevel() : 10);
            ps.executeUpdate();
            return Map.of("success", true);
        } catch (SQLException e) {
            return Map.of("success", false);
        }
    }

    @PutMapping("/products/{id}/price")
    public Map<String, Boolean> updatePrice(@PathVariable int id, @RequestParam double price) {
        String sql = "UPDATE products SET price = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, price);
            ps.setInt(2, id);
            return Map.of("success", ps.executeUpdate() > 0);
        } catch (SQLException e) {
            return Map.of("success", false);
        }
    }

    @PutMapping("/products/{id}/stock")
    public Map<String, Boolean> updateStock(@PathVariable int id, @RequestParam int stock) {
        String sql = "UPDATE products SET stock_quantity = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stock);
            ps.setInt(2, id);
            return Map.of("success", ps.executeUpdate() > 0);
        } catch (SQLException e) {
            return Map.of("success", false);
        }
    }

    @DeleteMapping("/products/{id}")
    public Map<String, Boolean> deleteProduct(@PathVariable int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return Map.of("success", ps.executeUpdate() > 0);
        } catch (SQLException e) {
            return Map.of("success", false);
        }
    }

    @GetMapping("/bills")
    public List<Map<String, Object>> getAllBills() {
        List<Map<String, Object>> bills = new ArrayList<>();
        String sql = "SELECT id, bill_number, bill_date, total_amount, status FROM bills ORDER BY id DESC LIMIT 50";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> b = new HashMap<>();
                b.put("id", rs.getInt("id"));
                b.put("billNumber", rs.getString("bill_number"));
                b.put("date", rs.getTimestamp("bill_date").toString());
                b.put("total", rs.getDouble("total_amount"));
                b.put("status", rs.getString("status"));
                bills.add(b);
            }
        } catch (SQLException e) {}
        return bills;
    }

    // ========== BILL STATUS ENDPOINT (For Payment Polling) ==========
    // Add this to AdminController.java if not already present
    @GetMapping("/bill/status/{billId}")
    public Map<String, Object> getBillStatus(@PathVariable int billId) {
        Map<String, Object> status = new HashMap<>();
        String sql = "SELECT id, bill_number, total_amount, status, bill_date, payment_date FROM bills WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, billId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                status.put("success", true);
                status.put("id", rs.getInt("id"));
                status.put("billNumber", rs.getString("bill_number"));
                status.put("total", rs.getDouble("total_amount"));
                status.put("status", rs.getString("status"));
                status.put("billDate", rs.getTimestamp("bill_date"));
                status.put("paymentDate", rs.getTimestamp("payment_date"));
            } else {
                status.put("success", false);
                status.put("message", "Bill not found");
            }
            rs.close();
        } catch (SQLException e) {
            status.put("success", false);
            status.put("message", e.getMessage());
            e.printStackTrace();
        }
        return status;
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        String sql = "SELECT status, COUNT(*) as count, COALESCE(SUM(total_amount), 0) as total FROM bills GROUP BY status";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String status = rs.getString("status");
                stats.put(status + "_count", rs.getInt("count"));
                stats.put(status + "_amount", rs.getDouble("total"));
            }
        } catch (SQLException e) {}
        return stats;
    }

    @GetMapping("/lowstock")
    public List<Map<String, Object>> getLowStockProducts() {
        String sql = "SELECT id, name, stock_quantity, reorder_level FROM products WHERE stock_quantity < reorder_level";
        List<Map<String, Object>> low = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> p = new HashMap<>();
                p.put("id", rs.getInt("id"));
                p.put("name", rs.getString("name"));
                p.put("stockQuantity", rs.getInt("stock_quantity"));
                p.put("reorderLevel", rs.getInt("reorder_level"));
                low.add(p);
            }
        } catch (SQLException e) {}
        return low;
    }
}