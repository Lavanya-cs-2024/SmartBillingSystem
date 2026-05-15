package com.smartbilling.controller;

import com.smartbilling.model.BillItem;
import com.smartbilling.model.Product;
import com.smartbilling.dao.ProductDAO;
import com.smartbilling.service.CartService;
import com.smartbilling.service.BillingService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@RestController
@RequestMapping("/api/customer")
@CrossOrigin(origins = "http://localhost:8080")
public class CustomerController {

    @Autowired
    private CartService cartService;

    private ProductDAO productDAO = new ProductDAO();
    private BillingService billingService = new BillingService();

    @GetMapping("/categories")
    public List<Map<String, Object>> getCategories() {
        return List.of(
            Map.of("id", 1, "name", "Writing Instruments"),
            Map.of("id", 2, "name", "Paper Products"),
            Map.of("id", 3, "name", "Office Supplies"),
            Map.of("id", 4, "name", "Art & Craft Supplies"),
            Map.of("id", 5, "name", "Geometry & Tools")
        );
    }

    @GetMapping("/products/by-category/{categoryId}")
    public List<Product> getProductsByCategory(@PathVariable int categoryId) {
        List<Product> allProducts = productDAO.getAllProducts();
        List<Product> filtered = new ArrayList<>();
        for (Product p : allProducts) {
            if (p.getCategoryId() == categoryId) {
                filtered.add(p);
            }
        }
        return filtered;
    }

    @GetMapping("/cart")
    public List<BillItem> getCart() {
        return cartService.getItems();
    }

    @PostMapping("/cart/add")
    public Map<String, Object> addToCart(@RequestParam int productId, @RequestParam int quantity) {
        boolean ok = cartService.addItem(productId, quantity);
        Map<String, Object> res = new HashMap<>();
        res.put("success", ok);
        res.put("cartSize", cartService.size());
        res.put("total", cartService.getTotal());
        if (!ok) {
            res.put("message", "Failed to add item. Check stock.");
        }
        return res;
    }

    @DeleteMapping("/cart/remove/{productId}")
    public Map<String, Object> removeFromCart(@PathVariable int productId) {
        boolean ok = cartService.removeItem(productId);
        Map<String, Object> res = new HashMap<>();
        res.put("success", ok);
        res.put("cartSize", cartService.size());
        res.put("total", cartService.getTotal());
        return res;
    }

    @DeleteMapping("/cart/clear")
    public Map<String, Object> clearCart() {
        cartService.clear();
        return Map.of("success", true, "cartSize", 0, "total", 0.0);
    }

    @PostMapping("/bill/generate")
    public Map<String, Object> generateBill() {
        List<BillItem> cart = cartService.getItems();
        if (cart.isEmpty()) {
            return Map.of("success", false, "message", "Cart is empty");
        }
        double total = cartService.getTotal();
        int billId = billingService.generateBill(cart, total);
        if (billId > 0) {
            return Map.of("success", true, "billId", billId, "total", total);
        } else {
            return Map.of("success", false, "message", "Bill generation failed");
        }
    }

    @PostMapping("/bill/pay/{billId}")
    public Map<String, Object> payBill(@PathVariable int billId) {
        List<BillItem> cart = new ArrayList<>(cartService.getItems());
        boolean ok = billingService.processPayment(billId, cart);
        
        if (ok) {
            // Get the bill details to return bill number
            com.smartbilling.model.Bill bill = billingService.getBillById(billId);
            cartService.clear();
            return Map.of(
                "success", true, 
                "message", "Payment successful!",
                "billId", billId,
                "billNumber", bill != null ? bill.getBillNumber() : "BILL-" + billId,
                "total", cart.stream().mapToDouble(BillItem::getSubtotal).sum()
            );
        } else {
            return Map.of("success", false, "message", "Payment failed");
        }
    }

    @PostMapping("/bill/cancel/{billId}")
    public Map<String, Object> cancelBill(@PathVariable int billId) {
        boolean ok = billingService.cancelBill(billId);
        if (ok) {
            cartService.clear();
            return Map.of("success", true, "message", "Bill cancelled");
        }
        return Map.of("success", false, "message", "Cancel failed");
    }
}