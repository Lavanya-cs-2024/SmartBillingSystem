package backend;

import backend.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Test: Add a new product to Art & Craft Supplies category
 * Category ID for Art & Craft Supplies = 4
 */
public class AddProductTest {
    
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("ADDING NEW PRODUCT TO ART & CRAFT SUPPLIES");
        System.out.println("=".repeat(60));
        
        // Step 1: Show current products in Art & Craft category
        showCurrentArtCraftProducts();
        
        // Step 2: Add new product
        addNewProduct();
        
        // Step 3: Verify the product was added
        showUpdatedArtCraftProducts();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("✅ TEST COMPLETED! Check MySQL Workbench to verify.");
        System.out.println("=".repeat(60));
    }
    
    // Method 1: Display current products in Art & Craft category
    public static void showCurrentArtCraftProducts() {
        System.out.println("\n📌 CURRENT PRODUCTS IN ART & CRAFT SUPPLIES:");
        System.out.println("-".repeat(40));
        
        try {
            Connection conn = DBConnection.getConnection();
            
            String sql = "SELECT id, name, price, stock_quantity FROM products WHERE category_id = 4";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                count++;
                System.out.printf("   ID: %d | Name: %-20s | Price: ₹%.2f | Stock: %d%n",
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getInt("stock_quantity")
                );
            }
            
            if (count == 0) {
                System.out.println("   No products found in this category.");
            } else {
                System.out.println("\n   📊 Total products: " + count);
            }
            
            rs.close();
            ps.close();
            DBConnection.closeConnection(conn);
            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Method 2: Add a new product to Art & Craft category
    public static void addNewProduct() {
        System.out.println("\n📌 ADDING NEW PRODUCT:");
        System.out.println("-".repeat(40));
        
        try {
            Connection conn = DBConnection.getConnection();
            
            // Insert new product
            // category_id = 4 (Art & Craft Supplies)
            String sql = "INSERT INTO products (name, category_id, description, price, stock_quantity, reorder_level) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            ps.setString(1, "Watercolor Brushes");           // Product name
            ps.setInt(2, 4);                                  // Category ID (Art & Craft)
            ps.setString(3, "Set of 5 high-quality watercolor brushes"); // Description
            ps.setDouble(4, 120.00);                          // Price
            ps.setInt(5, 30);                                 // Stock quantity
            ps.setInt(6, 8);                                  // Reorder level
            
            int rowsAffected = ps.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int newId = generatedKeys.getInt(1);
                    System.out.println("✅ PRODUCT ADDED SUCCESSFULLY!");
                    System.out.println("   New Product ID: " + newId);
                    System.out.println("   Product Name: Watercolor Brushes");
                    System.out.println("   Category: Art & Craft Supplies (ID: 4)");
                    System.out.println("   Price: ₹120.00");
                    System.out.println("   Stock: 30");
                    System.out.println("   Description: Set of 5 high-quality watercolor brushes");
                }
                generatedKeys.close();
            } else {
                System.out.println("❌ Failed to add product.");
            }
            
            ps.close();
            DBConnection.closeConnection(conn);
            
        } catch (Exception e) {
            System.out.println("❌ Error adding product: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Method 3: Show updated list after addition
    public static void showUpdatedArtCraftProducts() {
        System.out.println("\n📌 UPDATED PRODUCTS IN ART & CRAFT SUPPLIES:");
        System.out.println("-".repeat(40));
        
        try {
            Connection conn = DBConnection.getConnection();
            
            String sql = "SELECT id, name, price, stock_quantity FROM products WHERE category_id = 4 ORDER BY id";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                count++;
                String marker = (rs.getString("name").equals("Watercolor Brushes")) ? "🆕 NEW → " : "       ";
                System.out.printf("%s ID: %d | Name: %-20s | Price: ₹%.2f | Stock: %d%n",
                    marker,
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getInt("stock_quantity")
                );
            }
            
            System.out.println("\n   📊 Total products in Art & Craft: " + count);
            System.out.println("   (Previously: 6 products, Now: " + count + " products)");
            
            rs.close();
            ps.close();
            DBConnection.closeConnection(conn);
            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}