/**
 * BillItem.java
 * 
 * PURPOSE:
 * Model class representing an ITEM inside a bill from 'bill_items' table.
 * This is the DETAIL/LINE-ITEM level of a transaction.
 * 
 * WHY PRICE SNAPSHOT?
 * We store product_name and unit_price at bill time.
 * If product price changes later, old bills still show correct price.
 * This is called a "snapshot" - crucial for accounting.
 * 
 * RELATIONSHIPS:
 * Many BillItems → One Bill (many items per bill)
 * Many BillItems → One Product (same product can appear in many bills)
 */

package backend.model;

public class BillItem {
    
    // ========== FIELDS (Match bill_items table columns) ==========
    
    private int id;              // Unique item ID (Primary Key)
    private int billId;          // Foreign key to bills table
    private int productId;       // Foreign key to products table
    private String productName;  // SNAPSHOT: Product name at sale time
    private int quantity;        // Number of units purchased
    private double unitPrice;    // SNAPSHOT: Price per unit at sale time
    private double subtotal;     // quantity × unitPrice (pre-calculated)
    
    // ========== CONSTRUCTORS ==========
    
    /**
     * Default constructor - empty BillItem object
     */
    public BillItem() {}
    
    /**
     * Constructor for adding item to cart
     * 
     * @param productId ID from products table
     * @param productName Snapshot of product name
     * @param quantity How many units
     * @param unitPrice Snapshot of price
     * 
     * NOTE: subtotal is automatically calculated as quantity × unitPrice
     */
    public BillItem(int productId, String productName, int quantity, double unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = quantity * unitPrice;  // Calculate immediately
    }
    
    // ========== GETTERS ==========
    
    public int getId() { 
        return id; 
    }
    
    public int getBillId() { 
        return billId; 
    }
    
    public int getProductId() { 
        return productId; 
    }
    
    public String getProductName() { 
        return productName; 
    }
    
    public int getQuantity() { 
        return quantity; 
    }
    
    public double getUnitPrice() { 
        return unitPrice; 
    }
    
    public double getSubtotal() { 
        return subtotal; 
    }
    
    // ========== SETTERS ==========
    
    public void setId(int id) { 
        this.id = id; 
    }
    
    public void setBillId(int billId) { 
        this.billId = billId; 
    }
    
    public void setProductId(int productId) { 
        this.productId = productId; 
    }
    
    public void setProductName(String productName) { 
        this.productName = productName; 
    }
    
    public void setQuantity(int quantity) { 
        this.quantity = quantity; 
        // Recalculate subtotal when quantity changes
        this.subtotal = this.quantity * this.unitPrice;
    }
    
    public void setUnitPrice(double unitPrice) { 
        this.unitPrice = unitPrice; 
        // Recalculate subtotal when price changes
        this.subtotal = this.quantity * this.unitPrice;
    }
    
    public void setSubtotal(double subtotal) { 
        this.subtotal = subtotal; 
    }
}