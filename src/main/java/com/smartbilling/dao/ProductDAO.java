package com.smartbilling.dao;

import com.smartbilling.model.Product;
import com.smartbilling.model.BillItem;
import com.smartbilling.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    
    private List<Product> productCache = null;
    private long cacheTimestamp = 0;
    private static final long CACHE_DURATION = 30000;
    
    public List<Product> getAllProducts() {
        if (productCache != null && (System.currentTimeMillis() - cacheTimestamp) < CACHE_DURATION) {
            return productCache;
        }
        
        List<Product> products = new ArrayList<>();
        String sql = "SELECT id, name, category_id, price, stock_quantity, reorder_level FROM products ORDER BY id";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Product p = new Product();
                p.setId(rs.getInt("id"));
                p.setName(rs.getString("name"));
                p.setCategoryId(rs.getInt("category_id"));
                p.setPrice(rs.getDouble("price"));
                p.setStockQuantity(rs.getInt("stock_quantity"));
                p.setReorderLevel(rs.getInt("reorder_level"));
                products.add(p);
            }
            
            productCache = products;
            cacheTimestamp = System.currentTimeMillis();
            
        } catch (SQLException e) {
            System.err.println("Error fetching products: " + e.getMessage());
        }
        return products;
    }
    
    public Product getProductById(int id) {
        String sql = "SELECT id, name, category_id, price, stock_quantity, reorder_level FROM products WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Product p = new Product();
                p.setId(rs.getInt("id"));
                p.setName(rs.getString("name"));
                p.setCategoryId(rs.getInt("category_id"));
                p.setPrice(rs.getDouble("price"));
                p.setStockQuantity(rs.getInt("stock_quantity"));
                p.setReorderLevel(rs.getInt("reorder_level"));
                return p;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching product: " + e.getMessage());
        }
        return null;
    }
    
    public boolean reduceStock(Connection conn, int productId, int quantity) throws SQLException {
        String sql = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE id = ? AND stock_quantity >= ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, quantity);
        ps.setInt(2, productId);
        ps.setInt(3, quantity);
        int affected = ps.executeUpdate();
        ps.close();
        return affected > 0;
    }
    
    public boolean reduceStockBatch(Connection conn, List<BillItem> items) throws SQLException {
        String sql = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE id = ? AND stock_quantity >= ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        
        for (BillItem item : items) {
            ps.setInt(1, item.getQuantity());
            ps.setInt(2, item.getProductId());
            ps.setInt(3, item.getQuantity());
            ps.addBatch();
        }
        
        int[] results = ps.executeBatch();
        ps.close();
        
        for (int result : results) {
            if (result == 0) return false;
        }
        return true;
    }
    
    public boolean checkStock(int productId, int requestedQuantity) {
        String sql = "SELECT stock_quantity FROM products WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("stock_quantity") >= requestedQuantity;
            }
        } catch (SQLException e) {
            System.err.println("Error checking stock: " + e.getMessage());
        }
        return false;
    }
    
    public int getCurrentStock(Connection conn, int productId) throws SQLException {
        String sql = "SELECT stock_quantity FROM products WHERE id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, productId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            int stock = rs.getInt("stock_quantity");
            rs.close();
            ps.close();
            return stock;
        }
        rs.close();
        ps.close();
        return 0;
    }
}