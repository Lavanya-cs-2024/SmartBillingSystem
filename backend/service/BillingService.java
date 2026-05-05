/**
 * BillingService.java
 * 
 * PURPOSE:
 * Core business logic for the Smart Billing System.
 * Handles cart management, bill generation, payment processing, and stock management.
 * 
 * FEATURES:
 * 1. Shopping Cart Management (add, remove, update quantity, view)
 * 2. Bill Generation with QR (3-minute expiry)
 * 3. Payment Processing with Database Transactions
 * 4. Stock Management (reduces only after successful payment)
 * 5. Audit Trail (stock_logs for accountability)
 * 6. Category-based Product Display with navigation
 * 7. Optimized transaction handling
 * 8. Silent Database Operations
 */

package backend.service;

import backend.dao.BillDAO;
import backend.dao.ProductDAO;
import backend.dao.StockLogDAO;
import backend.model.Bill;
import backend.model.BillItem;
import backend.model.Product;
import backend.util.DBConnection;
import java.sql.*;
import java.util.*;

public class BillingService {
    
    private ProductDAO productDAO;
    private BillDAO billDAO;
    private StockLogDAO stockLogDAO;
    private List<BillItem> cart;
    private int currentBillId;
    private static final int MAX_PAYMENT_RETRIES = 2;
    
    // Store categories and products for navigation
    private List<Category> categories;
    
    public static class Category {
        public int id;
        public String name;
        public List<Product> products;
        
        public Category(int id, String name) {
            this.id = id;
            this.name = name;
            this.products = new ArrayList<>();
        }
    }
    
    public BillingService() {
        productDAO = new ProductDAO();
        billDAO = new BillDAO();
        stockLogDAO = new StockLogDAO();
        cart = new ArrayList<>();
        currentBillId = -1;
        loadCategoriesAndProducts();
    }
    
    /**
     * Load all categories and their products from database
     */
    private void loadCategoriesAndProducts() {
        categories = new ArrayList<>();
        String sql = "SELECT c.id AS cat_id, c.name AS cat_name, " +
                     "p.id AS prod_id, p.name AS prod_name, " +
                     "p.price, p.stock_quantity " +
                     "FROM categories c " +
                     "LEFT JOIN products p ON c.id = p.category_id " +
                     "ORDER BY c.id, p.id";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            Category currentCategory = null;
            
            while (rs.next()) {
                int catId = rs.getInt("cat_id");
                String catName = rs.getString("cat_name");
                
                if (currentCategory == null || currentCategory.id != catId) {
                    currentCategory = new Category(catId, catName);
                    categories.add(currentCategory);
                }
                
                int prodId = rs.getInt("prod_id");
                if (prodId != 0) {
                    Product p = new Product();
                    p.setId(prodId);
                    p.setName(rs.getString("prod_name"));
                    p.setPrice(rs.getDouble("price"));
                    p.setStockQuantity(rs.getInt("stock_quantity"));
                    currentCategory.products.add(p);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error loading categories: " + e.getMessage());
        }
    }
    
    /**
     * Display all category names (without products)
     */
    public void displayCategories() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("     PRODUCT CATEGORIES");
        System.out.println("=".repeat(60));
        int num = 1;
        for (Category cat : categories) {
            System.out.println(num + ". " + cat.name);
            num++;
        }
        System.out.println(num + ". Back to Main Menu");
        System.out.println("=".repeat(60));
    }
    
    /**
     * Display products for a specific category
     */
    public void displayProductsByCategory(int categoryIndex) {
        if (categoryIndex < 1 || categoryIndex > categories.size()) {
            System.out.println("Invalid category selection!");
            return;
        }
        
        Category cat = categories.get(categoryIndex - 1);
        
        System.out.println("\n" + "=".repeat(65));
        System.out.println("     " + cat.name.toUpperCase());
        System.out.println("=".repeat(65));
        System.out.printf("%-6s %-35s %-10s %-8s%n", "ID", "Product Name", "Price", "Stock");
        System.out.println("-".repeat(65));
        
        for (Product p : cat.products) {
            System.out.printf("%-6d %-35s Rs.%-9.2f %-8d%n",
                p.getId(), p.getName(), p.getPrice(), p.getStockQuantity());
        }
        System.out.println("=".repeat(65));
        System.out.println("\nOptions:");
        System.out.println("1. Add product to cart");
        System.out.println("2. View categories (choose another category)");
        System.out.println("3. Back to Main Menu");
        System.out.println("-".repeat(40));
    }
    
    /**
     * Add product to cart from current category view
     */
    public boolean addToCartFromCategory(int productId, int quantity) {
        return addToCart(productId, quantity);
    }
    
    public boolean addToCart(int productId, int quantity) {
        if (quantity <= 0) {
            System.out.println("Quantity must be greater than 0!");
            return false;
        }
        
        Product product = productDAO.getProductById(productId);
        if (product == null) {
            System.out.println("Product not found!");
            return false;
        }
        
        if (!productDAO.checkStock(productId, quantity)) {
            System.out.println("Insufficient stock! Only " + product.getStockQuantity() + " available.");
            return false;
        }
        
        for (BillItem item : cart) {
            if (item.getProductId() == productId) {
                int newQty = item.getQuantity() + quantity;
                if (!productDAO.checkStock(productId, newQty)) {
                    System.out.println("Total quantity exceeds stock!");
                    return false;
                }
                item.setQuantity(newQty);
                item.setSubtotal(newQty * item.getUnitPrice());
                System.out.println("Updated " + product.getName() + " quantity to " + newQty);
                return true;
            }
        }
        
        cart.add(new BillItem(productId, product.getName(), quantity, product.getPrice()));
        System.out.println("\n✅ Added " + quantity + " x " + product.getName() + " to cart");
        return true;
    }
    
    public boolean removeFromCart(int productId) {
        for (int i = 0; i < cart.size(); i++) {
            if (cart.get(i).getProductId() == productId) {
                String productName = cart.get(i).getProductName();
                cart.remove(i);
                System.out.println("Removed " + productName + " from cart");
                return true;
            }
        }
        System.out.println("Product not found in cart!");
        return false;
    }
    
    public boolean updateCartQuantity(int productId, int newQuantity) {
        if (newQuantity <= 0) {
            return removeFromCart(productId);
        }
        
        for (BillItem item : cart) {
            if (item.getProductId() == productId) {
                Product product = productDAO.getProductById(productId);
                if (product == null) {
                    System.out.println("Product not found!");
                    return false;
                }
                
                if (product.getStockQuantity() < newQuantity) {
                    System.out.println("Insufficient stock! Only " + product.getStockQuantity() + " available.");
                    return false;
                }
                
                item.setQuantity(newQuantity);
                item.setSubtotal(newQuantity * item.getUnitPrice());
                System.out.println("Updated " + item.getProductName() + " quantity to " + newQuantity);
                return true;
            }
        }
        System.out.println("Product not found in cart!");
        return false;
    }
    
    public void viewCart() {
        if (cart.isEmpty()) {
            System.out.println("\nCart is empty!");
            return;
        }
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                      YOUR CART");
        System.out.println("-".repeat(80));
        System.out.printf("%-10s %-30s %-8s %-12s %-12s%n", 
            "Product ID", "Product", "Qty", "Price", "Subtotal");
        System.out.println("-".repeat(80));
        
        double total = 0;
        for (BillItem item : cart) {
            System.out.printf("%-10d %-30s %-8d Rs.%-11.2f Rs.%-11.2f%n",
                item.getProductId(),
                item.getProductName(), 
                item.getQuantity(), 
                item.getUnitPrice(), 
                item.getSubtotal());
            total += item.getSubtotal();
        }
        System.out.println("-".repeat(80));
        System.out.printf("%-70s Rs.%-11.2f%n", "TOTAL:", total);
        System.out.println("=".repeat(80));
    }
    
    public List<BillItem> getCartItems() {
        return cart;
    }
    
    public void clearCart() {
        cart.clear();
        currentBillId = -1;
        System.out.println("Cart cleared!");
    }
    
    public boolean isCartEmpty() {
        return cart.isEmpty();
    }
    
    public boolean hasActiveBill() {
        return currentBillId > 0;
    }
    
    public int getCartSize() {
        return cart.size();
    }
    
    public int getCategoriesCount() {
        return categories.size();
    }
    
    public int generateBill() {
        if (cart.isEmpty()) {
            System.out.println("Cart is empty! Add items first.");
            return -1;
        }
        
        double total = cart.stream().mapToDouble(BillItem::getSubtotal).sum();
        String billNumber = "BILL-" + System.currentTimeMillis();
        Timestamp qrExpiry = new Timestamp(System.currentTimeMillis() + (3 * 60 * 1000));
        
        Bill bill = new Bill(billNumber, total, "PENDING");
        bill.setQrExpiry(qrExpiry);
        
        currentBillId = billDAO.createBill(bill, cart);
        
        if (currentBillId > 0) {
            System.out.println("\n" + "=".repeat(65));
            System.out.println("BILL GENERATED SUCCESSFULLY!");
            System.out.println("-".repeat(65));
            System.out.println("   Bill Number: " + billNumber);
            System.out.println("   Bill ID: " + currentBillId);
            System.out.println("   Total Amount: Rs." + total);
            System.out.println("   QR Valid Until: " + qrExpiry);
            System.out.println("-".repeat(65));
            System.out.println("QR CODE FOR PAYMENT (3 min expiry)");
            System.out.println("   UPI ID: stationery@bank");
            System.out.println("   Amount: Rs." + total);
            System.out.println("=".repeat(65));
            System.out.println("\nAfter scanning QR, select option 5 to complete payment");
            
            return currentBillId;
        }
        
        return -1;
    }
    
    public boolean processPayment() {
        if (currentBillId <= 0) {
            System.out.println("No active bill! Generate a bill first.");
            return false;
        }
        
        if (cart.isEmpty()) {
            System.out.println("No items in this bill!");
            return false;
        }
        
        Connection conn = null;
        int retryCount = 0;
        
        while (retryCount < MAX_PAYMENT_RETRIES) {
            try {
                conn = DBConnection.getConnection();
                conn.setAutoCommit(false);
                
                boolean success = billDAO.updateBillStatus(currentBillId, "PAID", "PAID");
                if (!success) {
                    conn.rollback();
                    retryCount++;
                    if (retryCount < MAX_PAYMENT_RETRIES) {
                        Thread.sleep(100);
                        continue;
                    } else {
                        System.out.println("Payment failed: Bill already processed.");
                        return false;
                    }
                }
                
                for (BillItem item : cart) {
                    int currentStock = productDAO.getCurrentStock(conn, item.getProductId());
                    
                    boolean stockReduced = productDAO.reduceStock(conn, item.getProductId(), item.getQuantity());
                    if (!stockReduced) {
                        conn.rollback();
                        retryCount++;
                        if (retryCount < MAX_PAYMENT_RETRIES) {
                            Thread.sleep(100);
                            continue;
                        } else {
                            System.out.println("Payment failed: Stock update error.");
                            return false;
                        }
                    }
                    
                    int stockAfter = currentStock - item.getQuantity();
                    insertStockLogDirect(conn, item.getProductId(), currentBillId, 
                        -item.getQuantity(), currentStock, stockAfter, "SALE");
                }
                
                conn.commit();
                
                System.out.println("\n" + "=".repeat(50));
                System.out.println("PAYMENT SUCCESSFUL!");
                System.out.println("=".repeat(50));
                System.out.println("   Bill ID: " + currentBillId);
                System.out.println("   Stock has been updated.");
                System.out.println("=".repeat(50));
                
                generateInvoice();
                
                cart.clear();
                currentBillId = -1;
                
                return true;
                
            } catch (SQLException e) {
                if (conn != null) {
                    try { conn.rollback(); } catch (SQLException ex) {}
                }
                retryCount++;
                if (retryCount >= MAX_PAYMENT_RETRIES) {
                    System.out.println("Payment failed: " + e.getMessage());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            } finally {
                if (conn != null) {
                    try { conn.close(); } catch (SQLException e) {}
                }
            }
        }
        return false;
    }
    
    private void insertStockLogDirect(Connection conn, int productId, int billId, 
                                       int quantityChange, int stockBefore, 
                                       int stockAfter, String reason) throws SQLException {
        String sql = "INSERT INTO stock_logs (product_id, bill_id, quantity_change, stock_before, stock_after, reason) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setInt(2, billId);
            ps.setInt(3, quantityChange);
            ps.setInt(4, stockBefore);
            ps.setInt(5, stockAfter);
            ps.setString(6, reason);
            ps.executeUpdate();
        }
    }
    
    public boolean cancelBill() {
        if (currentBillId <= 0) {
            System.out.println("No active bill to cancel!");
            return false;
        }
        
        boolean cancelled = billDAO.updateBillStatus(currentBillId, "FAILED", "CANCELLED");
        if (cancelled) {
            System.out.println("\n" + "=".repeat(40));
            System.out.println("BILL CANCELLED!");
            System.out.println("=".repeat(40));
            System.out.println("   Bill ID: " + currentBillId);
            System.out.println("   No stock was deducted.");
            System.out.println("=".repeat(40));
            
            cart.clear();
            currentBillId = -1;
            return true;
        } else {
            System.out.println("Failed to cancel bill.");
            return false;
        }
    }
    
    private void generateInvoice() {
        System.out.println("\n" + "=".repeat(65));
        System.out.println("                     INVOICE");
        System.out.println("=".repeat(65));
        System.out.printf("%-30s %-8s %-12s %-12s%n", "Product", "Qty", "Price", "Subtotal");
        System.out.println("-".repeat(65));
        
        for (BillItem item : cart) {
            System.out.printf("%-30s %-8d Rs.%-11.2f Rs.%-11.2f%n",
                item.getProductName(), item.getQuantity(), 
                item.getUnitPrice(), item.getSubtotal());
        }
        System.out.println("-".repeat(65));
        
        double total = cart.stream().mapToDouble(BillItem::getSubtotal).sum();
        System.out.printf("%-64s Rs.%-11.2f%n", "TOTAL:", total);
        System.out.println("=".repeat(65));
        System.out.println("   Status: PAID");
        System.out.println("   Thank you for shopping with us!");
        System.out.println("   Visit Again!");
        System.out.println("=".repeat(65));
    }
}