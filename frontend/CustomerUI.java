/**
 * CustomerUI.java
 * 
 * PURPOSE:
 * Customer interface with CATEGORY NAVIGATION.
 * 
 * FEATURES:
 * - Select category first, then products
 * - Stay in same category after adding product
 * - View cart with management options
 * - Bill/payment options appear ONLY after cart has items
 */

package frontend;

import backend.service.BillingService;
import backend.model.BillItem;
import java.util.Scanner;
import java.util.List;

public class CustomerUI {
    
    private BillingService billingService;
    private Scanner scanner;
    private boolean inCategoryView;
    private int currentCategoryIndex;
    
    public CustomerUI() {
        billingService = new BillingService();
        scanner = new Scanner(System.in);
        inCategoryView = false;
        currentCategoryIndex = -1;
    }
    
    public void start() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("     WELCOME TO STATIONERY SHOP");
        System.out.println("=".repeat(60));
        
        while (true) {
            // Check if cart has items to show bill/payment options
            boolean hasItems = !billingService.isCartEmpty();
            
            System.out.println("\n" + "-".repeat(60));
            System.out.println("MAIN MENU");
            System.out.println("-".repeat(60));
            System.out.println("1. View Categories");
            System.out.println("2. View Cart");
            
            // Only show these options if cart has at least one item
            if (hasItems) {
                System.out.println("3. Generate Bill & QR");
                System.out.println("4. Make Payment");
                System.out.println("5. Cancel Current Bill");
                System.out.println("6. Clear Cart");
                System.out.println("7. Exit");
            } else {
                System.out.println("3. Exit");
            }
            System.out.println("-".repeat(60));
            System.out.print("Enter your choice: ");
            
            int choice;
            try {
                choice = scanner.nextInt();
            } catch (Exception e) {
                System.out.println("Invalid input! Please enter a number.");
                scanner.nextLine();
                continue;
            }
            
            if (!hasItems) {
                // Cart is empty - only options 1, 2, 3 available
                switch (choice) {
                    case 1:
                        showCategoryMenu();
                        break;
                    case 2:
                        viewCartWithMenu();
                        break;
                    case 3:
                        System.out.println("\nThank you for shopping with us!");
                        System.out.println("   Have a great day!");
                        return;
                    default:
                        System.out.println("Invalid choice! Please try again.");
                }
            } else {
                // Cart has items - all options available
                switch (choice) {
                    case 1:
                        showCategoryMenu();
                        break;
                    case 2:
                        viewCartWithMenu();
                        break;
                    case 3:
                        billingService.generateBill();
                        break;
                    case 4:
                        if (billingService.hasActiveBill()) {
                            processPayment();
                        } else {
                            System.out.println("No active bill! Generate a bill first.");
                        }
                        break;
                    case 5:
                        if (billingService.hasActiveBill()) {
                            billingService.cancelBill();
                        } else {
                            System.out.println("No active bill to cancel!");
                        }
                        break;
                    case 6:
                        billingService.clearCart();
                        break;
                    case 7:
                        System.out.println("\nThank you for shopping with us!");
                        System.out.println("   Have a great day!");
                        return;
                    default:
                        System.out.println("Invalid choice! Please try again.");
                }
            }
        }
    }
    
    /**
     * Show category menu - user selects a category
     */
    private void showCategoryMenu() {
        billingService.displayCategories();
        int totalCategories = billingService.getCategoriesCount();
        System.out.print("Select category (1-" + totalCategories + "): ");
        int catChoice = scanner.nextInt();
        
        if (catChoice == totalCategories + 1) {
            // Back to main menu
            return;
        }
        
        if (catChoice >= 1 && catChoice <= totalCategories) {
            currentCategoryIndex = catChoice;
            showProductsInCategory();
        } else {
            System.out.println("Invalid category selection!");
        }
    }
    
    /**
     * Show products in selected category with options
     */
    private void showProductsInCategory() {
        while (true) {
            billingService.displayProductsByCategory(currentCategoryIndex);
            
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            
            if (choice == 1) {
                // Add product to cart
                System.out.print("Enter Product ID: ");
                int productId = scanner.nextInt();
                System.out.print("Enter Quantity: ");
                int quantity = scanner.nextInt();
                billingService.addToCart(productId, quantity);
                // Stay in same category after adding
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
                scanner.nextLine();
                // Continue loop - show same category again
            } else if (choice == 2) {
                // Go back to categories menu
                showCategoryMenu();
                return;
            } else if (choice == 3) {
                // Back to main menu
                return;
            } else {
                System.out.println("Invalid choice!");
            }
        }
    }
    
    private void viewCartWithMenu() {
        List<BillItem> cart = billingService.getCartItems();
        
        if (cart.isEmpty()) {
            System.out.println("\nCart is empty!");
            return;
        }
        
        displayCartContents();
        
        while (true) {
            System.out.println("\n" + "-".repeat(40));
            System.out.println("CART MANAGEMENT");
            System.out.println("-".repeat(40));
            System.out.println("1. Update Item Quantity");
            System.out.println("2. Remove Item from Cart");
            System.out.println("3. Back to Main Menu");
            System.out.println("-".repeat(40));
            System.out.print("Enter your choice: ");
            
            int choice;
            try {
                choice = scanner.nextInt();
            } catch (Exception e) {
                System.out.println("Invalid input!");
                scanner.nextLine();
                continue;
            }
            
            switch (choice) {
                case 1:
                    updateCartQuantity();
                    if (!billingService.getCartItems().isEmpty()) {
                        displayCartContents();
                    } else {
                        System.out.println("\nCart is now empty. Returning to main menu.");
                        return;
                    }
                    break;
                case 2:
                    removeFromCart();
                    if (billingService.getCartItems().isEmpty()) {
                        System.out.println("\nCart is now empty. Returning to main menu.");
                        return;
                    }
                    displayCartContents();
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid choice! Please try again.");
            }
        }
    }
    
    private void displayCartContents() {
        List<BillItem> cart = billingService.getCartItems();
        
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
    
    private void updateCartQuantity() {
        List<BillItem> cart = billingService.getCartItems();
        
        if (cart.isEmpty()) {
            System.out.println("Cart is empty!");
            return;
        }
        
        System.out.println("\n" + "-".repeat(60));
        System.out.println("CURRENT CART ITEMS");
        System.out.println("-".repeat(60));
        System.out.printf("%-12s %-30s %-8s%n", "Product ID", "Product Name", "Current Qty");
        System.out.println("-".repeat(60));
        
        for (BillItem item : cart) {
            System.out.printf("%-12d %-30s %-8d%n",
                item.getProductId(),
                item.getProductName(),
                item.getQuantity());
        }
        System.out.println("-".repeat(60));
        
        System.out.print("\nEnter Product ID to update: ");
        int productId = scanner.nextInt();
        
        boolean found = false;
        for (BillItem item : cart) {
            if (item.getProductId() == productId) {
                found = true;
                break;
            }
        }
        
        if (!found) {
            System.out.println("Product not found in cart!");
            return;
        }
        
        System.out.print("Enter new quantity: ");
        int newQuantity = scanner.nextInt();
        
        if (newQuantity <= 0) {
            System.out.println("Quantity must be greater than 0. Use Remove option to delete item.");
            return;
        }
        
        if (billingService.updateCartQuantity(productId, newQuantity)) {
            System.out.println("Quantity updated successfully!");
        }
    }
    
    private void removeFromCart() {
        List<BillItem> cart = billingService.getCartItems();
        
        if (cart.isEmpty()) {
            System.out.println("Cart is empty!");
            return;
        }
        
        System.out.println("\n" + "-".repeat(60));
        System.out.println("CURRENT CART ITEMS");
        System.out.println("-".repeat(60));
        System.out.printf("%-12s %-30s %-8s%n", "Product ID", "Product Name", "Qty");
        System.out.println("-".repeat(60));
        
        for (BillItem item : cart) {
            System.out.printf("%-12d %-30s %-8d%n",
                item.getProductId(),
                item.getProductName(),
                item.getQuantity());
        }
        System.out.println("-".repeat(60));
        
        System.out.print("\nEnter Product ID to remove from cart: ");
        int productId = scanner.nextInt();
        
        if (billingService.removeFromCart(productId)) {
            System.out.println("Item removed from cart successfully!");
        } else {
            System.out.println("Product not found in cart!");
        }
    }
    
    private void processPayment() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("     PAYMENT OPTIONS");
        System.out.println("=".repeat(60));
        System.out.println("1. Mark as PAID (after QR scan)");
        System.out.println("2. Cancel Bill");
        System.out.println("-".repeat(60));
        System.out.print("Enter choice: ");
        
        int choice = scanner.nextInt();
        
        if (choice == 1) {
            System.out.print("\nProcessing payment");
            for (int i = 0; i < 3; i++) {
                try {
                    Thread.sleep(300);
                    System.out.print(".");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            System.out.println();
            
            boolean success = billingService.processPayment();
            
            if (success) {
                System.out.println("\nPayment completed successfully!");
            }
        } else if (choice == 2) {
            billingService.cancelBill();
        } else {
            System.out.println("Invalid choice!");
        }
    }
}