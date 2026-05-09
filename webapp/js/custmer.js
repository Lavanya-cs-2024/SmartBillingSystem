// API Base URL
const API_BASE = 'http://localhost:8080/api';

// Global state
let products = [];
let cart = [];
let currentCategory = 1;

// DOM Elements
const categoriesDiv = document.getElementById('categories');
const productsDiv = document.getElementById('products');
const cartItemsDiv = document.getElementById('cart-items');
const cartCountSpan = document.getElementById('cart-count');
const cartTotalSpan = document.getElementById('cart-total');
const cartTotalAmountSpan = document.getElementById('cart-total-amount');

// Page Load
document.addEventListener('DOMContentLoaded', () => {
    loadCategories();
    loadProducts();
    loadCart();
    
    // Event Listeners
    document.getElementById('generate-bill').addEventListener('click', generateBill);
    document.getElementById('pay-bill').addEventListener('click', payBill);
    document.getElementById('cancel-bill').addEventListener('click', cancelBill);
    document.getElementById('clear-cart').addEventListener('click', clearCart);
});

// Load Categories
async function loadCategories() {
    try {
        const response = await fetch(`${API_BASE}/categories`);
        const categories = await response.json();
        
        categoriesDiv.innerHTML = categories.map(cat => `
            <button class="category-btn ${cat.id === currentCategory ? 'active' : ''}" 
                    onclick="selectCategory(${cat.id})">
                📁 ${cat.name}
            </button>
        `).join('');
    } catch (error) {
        console.error('Error loading categories:', error);
        showToast('Failed to load categories', 'error');
    }
}

// Select Category
function selectCategory(categoryId) {
    currentCategory = categoryId;
    loadCategories();
    loadProducts();
}

// Load Products
async function loadProducts() {
    try {
        const response = await fetch(`${API_BASE}/products`);
        const allProducts = await response.json();
        
        // Filter by category (using your category logic)
        // For now, show all products. In production, filter by category ID
        products = allProducts;
        
        displayProducts(products);
    } catch (error) {
        console.error('Error loading products:', error);
        showToast('Failed to load products', 'error');
    }
}

// Display Products
function displayProducts(productsToShow) {
    if (productsToShow.length === 0) {
        productsDiv.innerHTML = '<div class="loading">No products found</div>';
        return;
    }
    
    productsDiv.innerHTML = productsToShow.map(product => `
        <div class="product-card">
            <h4>${product.name}</h4>
            <div class="product-price">₹${product.price.toFixed(2)}</div>
            <div class="product-stock ${product.stockQuantity < 10 ? 'stock-low' : ''}">
                Stock: ${product.stockQuantity}
            </div>
            <button class="add-btn" 
                    onclick="addToCart(${product.id}, ${product.price}, '${product.name}', ${product.stockQuantity})"
                    ${product.stockQuantity === 0 ? 'disabled' : ''}>
                ${product.stockQuantity === 0 ? 'Out of Stock' : 'Add to Cart 🛒'}
            </button>
        </div>
    `).join('');
}

// Add to Cart
async function addToCart(productId, price, name, stock) {
    try {
        const response = await fetch(`${API_BASE}/cart/add`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ productId, quantity: 1 })
        });
        const result = await response.json();
        
        if (result.success) {
            await loadCart();
            showToast(`${name} added to cart!`, 'success');
        } else {
            showToast('Failed to add to cart', 'error');
        }
    } catch (error) {
        console.error('Error adding to cart:', error);
        showToast('Error adding to cart', 'error');
    }
}

// Load Cart
async function loadCart() {
    try {
        const response = await fetch(`${API_BASE}/cart`);
        cart = await response.json();
        
        updateCartDisplay();
    } catch (error) {
        console.error('Error loading cart:', error);
    }
}

// Update Cart Display
function updateCartDisplay() {
    const cartData = cart;
    const items = cartData.items || [];
    const total = cartData.total || 0;
    const count = cartData.count || 0;
    
    cartCountSpan.textContent = count;
    cartTotalSpan.textContent = `₹${total.toFixed(2)}`;
    cartTotalAmountSpan.textContent = `₹${total.toFixed(2)}`;
    
    if (items.length === 0) {
        cartItemsDiv.innerHTML = '<div style="text-align:center;color:#999;">Cart is empty</div>';
        return;
    }
    
    cartItemsDiv.innerHTML = items.map(item => `
        <div class="cart-item">
            <div class="cart-item-info">
                <h4>${item.productName}</h4>
                <div class="cart-item-price">₹${item.price} each</div>
            </div>
            <div class="cart-item-actions">
                <div class="cart-item-qty">Qty: ${item.quantity}</div>
                <button class="remove-item" onclick="removeFromCart(${item.productId})">Remove</button>
            </div>
        </div>
    `).join('');
}

// Remove from Cart
async function removeFromCart(productId) {
    // For simplicity, we'll reload cart after "removing"
    // In production, implement a remove API endpoint
    showToast('Use "Clear Cart" or terminal UI for remove functionality', 'info');
}

// Generate Bill
async function generateBill() {
    try {
        const response = await fetch(`${API_BASE}/bill/generate`, {
            method: 'POST'
        });
        const result = await response.json();
        
        if (result.success) {
            showToast(`Bill Generated! Bill ID: ${result.billId}`, 'success');
            showToast('QR Code: Scan using UPI app (3 min expiry)', 'info');
        } else {
            showToast(result.message || 'Failed to generate bill', 'error');
        }
    } catch (error) {
        console.error('Error generating bill:', error);
        showToast('Failed to generate bill', 'error');
    }
}

// Pay Bill
async function payBill() {
    try {
        const response = await fetch(`${API_BASE}/bill/pay`, {
            method: 'POST'
        });
        const result = await response.json();
        
        if (result.success) {
            showToast('✅ Payment Successful!', 'success');
            await loadCart();
        } else {
            showToast(result.message || 'Payment failed', 'error');
        }
    } catch (error) {
        console.error('Error processing payment:', error);
        showToast('Payment failed', 'error');
    }
}

// Cancel Bill
async function cancelBill() {
    try {
        const response = await fetch(`${API_BASE}/bill/cancel`, {
            method: 'POST'
        });
        const result = await response.json();
        
        if (result.success) {
            showToast('Bill cancelled', 'info');
            await loadCart();
        } else {
            showToast('Failed to cancel bill', 'error');
        }
    } catch (error) {
        console.error('Error cancelling bill:', error);
        showToast('Failed to cancel bill', 'error');
    }
}

// Clear Cart
async function clearCart() {
    // For simplicity, clear locally and reload
    showToast('Use terminal UI to manage cart items', 'info');
}

// Show Toast Message
function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.style.background = type === 'success' ? '#27ae60' : type === 'error' ? '#e74c3c' : '#333';
    toast.textContent = message;
    document.body.appendChild(toast);
    
    setTimeout(() => {
        toast.remove();
    }, 3000);
}