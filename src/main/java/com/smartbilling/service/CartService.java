package com.smartbilling.service;

import com.smartbilling.model.BillItem;
import com.smartbilling.dao.ProductDAO;
import java.util.ArrayList;
import java.util.List;

public class CartService {
    
    private List<BillItem> items = new ArrayList<>();
    private transient ProductDAO productDAO = new ProductDAO();

    public List<BillItem> getItems() { 
        return items; 
    }
    
    public int size() {
        return items.size();
    }
    
    public boolean isEmpty() {
        return items.isEmpty();
    }
    
    public double getTotal() {
        return items.stream().mapToDouble(BillItem::getSubtotal).sum();
    }

    public boolean addItem(int productId, int quantity) {
        var product = productDAO.getProductById(productId);
        if (product == null) return false;
        if (!productDAO.checkStock(productId, quantity)) return false;

        for (BillItem item : items) {
            if (item.getProductId() == productId) {
                int newQuantity = item.getQuantity() + quantity;
                if (!productDAO.checkStock(productId, newQuantity)) return false;
                item.setQuantity(newQuantity);
                item.setSubtotal(newQuantity * item.getUnitPrice());
                return true;
            }
        }
        items.add(new BillItem(productId, product.getName(), quantity, product.getPrice()));
        return true;
    }
    
    public boolean updateQuantity(int productId, int newQuantity) {
        if (newQuantity <= 0) return removeItem(productId);
        var product = productDAO.getProductById(productId);
        if (product == null) return false;
        if (!productDAO.checkStock(productId, newQuantity)) return false;

        for (BillItem item : items) {
            if (item.getProductId() == productId) {
                item.setQuantity(newQuantity);
                item.setSubtotal(newQuantity * item.getUnitPrice());
                return true;
            }
        }
        return false;
    }
    
    public boolean removeItem(int productId) {
        return items.removeIf(item -> item.getProductId() == productId);
    }
    
    public void clear() { 
        items.clear(); 
    }
}