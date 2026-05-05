/**
 * Product.java
 * 
 * PURPOSE:
 * This is a Model/POJO (Plain Old Java Object) class.
 * It represents a Product from the 'products' database table.
 * 
 * WHAT IS A MODEL CLASS?
 * - Blueprint for creating Product objects
 * - Each Product object represents ONE row from products table
 * - Contains data + getters/setters
 * 
 * RELATIONSHIP WITH DATABASE:
 * Product object ←→ products table row
 * id ←→ id column
 * name ←→ name column
 * price ←→ price column
 * stockQuantity ←→ stock_quantity column
 */

package backend.model;

public class Product {
    
    // ========== FIELDS (Instance Variables) ==========
    // These match columns in products table
    
    private int id;              // Unique product ID (Primary Key)
    private String name;         // Product name (e.g., "Pen", "Notebook")
    private int categoryId;      // Foreign key to categories table
    private String description;  // Product description
    private double price;        // Selling price in Rupees
    private int stockQuantity;   // Available stock count
    private int reorderLevel;    // Alert when stock falls below this
    
    // ========== CONSTRUCTORS ==========
    
    /**
     * Default constructor (no arguments)
     * Creates empty Product object
     * Used when we'll set values using setters
     */
    public Product() {}
    
    /**
     * Parameterized constructor (with arguments)
     * Creates Product object with basic info
     * Used when we have all data ready
     * 
     * @param id Product ID from database
     * @param name Product name
     * @param price Product price
     * @param stockQuantity Available stock
     */
    public Product(int id, String name, double price, int stockQuantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }
    
    // ========== GETTERS (Accessor Methods) ==========
    // Each getter returns the value of a field
    // Naming convention: get + FieldName (capitalized)
    
    public int getId() { 
        return id; 
    }
    
    public String getName() { 
        return name; 
    }
    
    public int getCategoryId() { 
        return categoryId; 
    }
    
    public String getDescription() { 
        return description; 
    }
    
    public double getPrice() { 
        return price; 
    }
    
    public int getStockQuantity() { 
        return stockQuantity; 
    }
    
    public int getReorderLevel() { 
        return reorderLevel; 
    }
    
    // ========== SETTERS (Mutator Methods) ==========
    // Each setter updates the value of a field
    // Naming convention: set + FieldName (capitalized)
    // 'this' keyword refers to current object's field
    
    public void setId(int id) { 
        this.id = id; 
    }
    
    public void setName(String name) { 
        this.name = name; 
    }
    
    public void setCategoryId(int categoryId) { 
        this.categoryId = categoryId; 
    }
    
    public void setDescription(String description) { 
        this.description = description; 
    }
    
    public void setPrice(double price) { 
        this.price = price; 
    }
    
    public void setStockQuantity(int stockQuantity) { 
        this.stockQuantity = stockQuantity; 
    }
    
    public void setReorderLevel(int reorderLevel) { 
        this.reorderLevel = reorderLevel; 
    }
    
    // ========== toString() METHOD ==========
    /**
     * Returns string representation of Product
     * Automatically called when printing Product object
     * 
     * %-3d → 3 characters width, left-aligned for integer
     * %-25s → 25 characters width, left-aligned for string
     * ₹%-8.2f → Rupee symbol, 8 width, 2 decimal places
     * 
     * Example output: "1   Pen                       ₹15.00    Stock: 200"
     */
    @Override
    public String toString() {
        return String.format("%-3d %-25s ₹%-8.2f Stock: %d", 
                            id, name, price, stockQuantity);
    }
}