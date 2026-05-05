/**
 * ProductDAO.java
 * 
 * PURPOSE:
 * DAO = Data Access Object
 * This class handles ALL database operations for Product table.
 * 
 * WHY DAO PATTERN?
 * - Separates database logic from business logic
 * - If database changes, only DAO needs updates
 * - Makes code reusable and testable
 * 
 * WHAT THIS CLASS DOES:
 * - Fetch products from database
 * - Check stock availability
 * - Update stock quantities
 */

package backend.dao;

import backend.model.Product;
import backend.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    
    // ========== GET ALL PRODUCTS ==========
    /**
     * Retrieves all products from database
     * @return List of Product objects (empty list if none)
     * 
     * SQL: SELECT id, name, price, stock_quantity FROM products ORDER BY id
     * 
     * STEPS:
     * 1. Get database connection
     * 2. Create SQL statement
     * 3. Execute query to get ResultSet
     * 4. Loop through ResultSet and create Product objects
     * 5. Add each Product to ArrayList
     * 6. Close resources and return list
     */
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();  // Empty list to fill
        String sql = "SELECT id, name, price, stock_quantity FROM products ORDER BY id";
        
        // Try-with-resources: automatically closes Connection, Statement, ResultSet
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            // Loop through each row in result
            while (rs.next()) {
                Product p = new Product();
                p.setId(rs.getInt("id"));           // Get id column as int
                p.setName(rs.getString("name"));     // Get name column as String
                p.setPrice(rs.getDouble("price"));   // Get price column as double
                p.setStockQuantity(rs.getInt("stock_quantity")); // Get stock
                products.add(p);  // Add to list
            }
        } catch (SQLException e) {
            System.err.println("Error fetching products: " + e.getMessage());
        }
        return products;
    }
    
    // ========== GET PRODUCT BY ID ==========
    /**
     * Retrieves single product by its ID
     * @param id Product ID to search for
     * @return Product object or null if not found
     * 
     * Uses PreparedStatement to prevent SQL injection
     * PreparedStatement pre-compiles SQL with placeholders (?)
     */
    public Product getProductById(int id) {
        String sql = "SELECT id, name, price, stock_quantity FROM products WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, id);  // Replace first ? with id value
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {  // If row exists
                Product p = new Product();
                p.setId(rs.getInt("id"));
                p.setName(rs.getString("name"));
                p.setPrice(rs.getDouble("price"));
                p.setStockQuantity(rs.getInt("stock_quantity"));
                return p;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching product: " + e.getMessage());
        }
        return null;  // Product not found
    }
    
    // ========== REDUCE STOCK (WITH CONNECTION) ==========
    /**
     * Reduces product stock by given quantity
     * @param conn Active database connection (for transactions)
     * @param productId Product to update
     * @param quantity Amount to reduce
     * @return true if successful, false if insufficient stock
     * 
     * NOTE: This method receives a Connection object (not creates its own)
     * This allows it to be part of a larger TRANSACTION
     * WHERE clause "stock_quantity >= ?" ensures we don't go negative
     */
    public boolean reduceStock(Connection conn, int productId, int quantity) throws SQLException {
        String sql = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE id = ? AND stock_quantity >= ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, quantity);
        ps.setInt(2, productId);
        ps.setInt(3, quantity);
        
        int affected = ps.executeUpdate();
        ps.close();
        return affected > 0;  // True if row was updated
    }
    
    // ========== CHECK STOCK AVAILABILITY ==========
    /**
     * Checks if requested quantity is available
     * @param productId Product to check
     * @param requestedQuantity How many needed
     * @return true if enough stock, false otherwise
     */
    public boolean checkStock(int productId, int requestedQuantity) {
        String sql = "SELECT stock_quantity FROM products WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                int available = rs.getInt("stock_quantity");
                return available >= requestedQuantity;
            }
        } catch (SQLException e) {
            System.err.println("Error checking stock: " + e.getMessage());
        }
        return false;
    }
    
    // ========== GET CURRENT STOCK (WITH CONNECTION) ==========
    /**
     * Gets current stock level within an existing transaction
     * @param conn Active database connection
     * @param productId Product to check
     * @return Current stock quantity
     * 
     * Used during payment transaction to log BEFORE value
     */
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