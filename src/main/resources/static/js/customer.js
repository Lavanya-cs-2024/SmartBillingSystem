/**
 * CUSTOMER UI JAVASCRIPT
 * Handles all customer-facing functionality
 */

// ========== GLOBAL VARIABLES ==========
let activeCategoryId = null;
let currentBillId = null;
let currentBillTotal = 0;
let currentBillNumber = null;

// ========== CATEGORY MANAGEMENT ==========

async function loadCategories() {
    try {
        const res = await fetch('/api/customer/categories');
        const cats = await res.json();
        const container = document.getElementById('categoriesList');
        if (!container) return;
        
        container.innerHTML = cats.map(c => `
            <a href="#" class="list-group-item list-group-item-action" data-id="${c.id}">
                <i class="fas fa-folder"></i> ${c.name}
            </a>
        `).join('');
        
        document.querySelectorAll('#categoriesList a').forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                activeCategoryId = parseInt(link.dataset.id);
                loadProducts(activeCategoryId);
            });
        });
        
        if (cats.length > 0) {
            activeCategoryId = cats[0].id;
            loadProducts(activeCategoryId);
        }
    } catch (error) {
        console.error('Error loading categories:', error);
    }
}

async function loadProducts(categoryId) {
    try {
        const res = await fetch(`/api/customer/products/by-category/${categoryId}`);
        const prods = await res.json();
        const area = document.getElementById('productsArea');
        if (!area) return;
        
        if (prods.length === 0) {
            area.innerHTML = '<div class="alert alert-info">No products in this category.</div>';
            return;
        }
        
        area.innerHTML = `<div class="row">` + prods.map(p => `
            <div class="col-md-6 mb-3">
                <div class="card h-100">
                    <div class="card-body">
                        <h6 class="card-title fw-bold">${escapeHtml(p.name)}</h6>
                        <p class="card-text">
                            <span class="text-primary fw-bold fs-5">₹${parseFloat(p.price).toFixed(2)}</span><br>
                            <small class="text-muted" id="stock-display-${p.id}">Stock: ${p.stockQuantity}</small>
                        </p>
                        <div class="input-group">
                            <input type="number" id="qty-${p.id}" class="form-control form-control-sm" value="1" min="1" max="${p.stockQuantity}">
                            <button class="btn btn-sm btn-primary add-to-cart-btn" data-id="${p.id}">
                                <i class="fas fa-cart-plus"></i> Add
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `).join('') + `</div>`;
        
        document.querySelectorAll('.add-to-cart-btn').forEach(btn => {
            btn.addEventListener('click', async () => {
                const productId = btn.dataset.id;
                const qtyInput = document.getElementById(`qty-${productId}`);
                const quantity = qtyInput ? parseInt(qtyInput.value) : 1;
                
                const res = await fetch(`/api/customer/cart/add?productId=${productId}&quantity=${quantity}`, { method: 'POST' });
                const data = await res.json();
                
                if (data.success) {
                    showToast(`${quantity} item(s) added to cart!`, 'success');
                    loadCart();
                } else {
                    showToast(data.message || 'Failed to add item', 'danger');
                }
            });
        });
    } catch (error) {
        console.error('Error loading products:', error);
    }
}

async function refreshStockDisplays() {
    if (!activeCategoryId) return;
    
    try {
        const res = await fetch(`/api/customer/products/by-category/${activeCategoryId}`);
        const prods = await res.json();
        
        prods.forEach(p => {
            const stockSpan = document.getElementById(`stock-display-${p.id}`);
            if (stockSpan) {
                stockSpan.innerHTML = `Stock: ${p.stockQuantity}`;
            }
            const qtyInput = document.getElementById(`qty-${p.id}`);
            if (qtyInput) {
                qtyInput.max = p.stockQuantity;
                if (parseInt(qtyInput.value) > p.stockQuantity) {
                    qtyInput.value = p.stockQuantity;
                }
            }
        });
    } catch (error) {
        console.error('Error refreshing stock displays:', error);
    }
}

async function clearCartAndReset() {
    await fetch('/api/customer/cart/clear', { method: 'DELETE' });
    await refreshStockDisplays();
    await loadCart();
}

async function loadCart() {
    try {
        const res = await fetch('/api/customer/cart');
        const items = await res.json();
        const cartDiv = document.getElementById('cartArea');
        const cartCountSpan = document.getElementById('cartCount');
        const generateBtn = document.getElementById('generateBillBtn');
        
        if (!cartDiv) return;
        
        if (items.length === 0) {
            cartDiv.innerHTML = '<div class="text-center text-muted p-3">Cart is empty</div>';
            if (cartCountSpan) cartCountSpan.textContent = '0';
            if (generateBtn) generateBtn.disabled = true;
            return;
        }
        
        if (generateBtn) generateBtn.disabled = false;
        if (cartCountSpan) cartCountSpan.textContent = items.length;
        
        let total = 0;
        cartDiv.innerHTML = items.map(item => {
            total += item.subtotal;
            return `
                <div class="cart-item mb-2 p-2 border-bottom">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <strong>${escapeHtml(item.productName)}</strong><br>
                            <small>Qty: ${item.quantity} x ₹${parseFloat(item.unitPrice).toFixed(2)}</small>
                        </div>
                        <div class="text-end">
                            <div class="fw-bold">₹${parseFloat(item.subtotal).toFixed(2)}</div>
                            <button class="btn btn-sm btn-outline-danger remove-item mt-1" data-id="${item.productId}">
                                <i class="fas fa-trash"></i> Remove
                            </button>
                        </div>
                    </div>
                </div>
            `;
        }).join('');
        
        cartDiv.innerHTML += `<div class="mt-3 pt-2 border-top fw-bold text-end fs-5">Total: ₹${total.toFixed(2)}</div>`;
        
        document.querySelectorAll('.remove-item').forEach(btn => {
            btn.addEventListener('click', async () => {
                const pid = btn.dataset.id;
                await fetch(`/api/customer/cart/remove/${pid}`, { method: 'DELETE' });
                loadCart();
            });
        });
    } catch (error) {
        console.error('Error loading cart:', error);
    }
}

// ========== BILL GENERATION ==========

document.getElementById('generateBillBtn')?.addEventListener('click', async () => {
    const cartRes = await fetch('/api/customer/cart');
    const cartItems = await cartRes.json();
    
    if (cartItems.length === 0) {
        showToast('Cart is empty! Add items first.', 'warning');
        return;
    }
    
    try {
        const res = await fetch('/api/customer/bill/generate', { method: 'POST' });
        const data = await res.json();
        
        if (data.success) {
            currentBillId = data.billId;
            currentBillTotal = data.total;
            
            const billHTML = `
                <div class="bill-card p-3">
                    <div class="bg-primary text-white p-3 rounded text-center">
                        <h3><i class="fas fa-receipt"></i> TAX INVOICE</h3>
                        <p>Stationery Shop</p>
                        <small>Bill #: ${data.billId} | ${new Date().toLocaleString()}</small>
                    </div>
                    
                    <div class="table-responsive mt-3">
                        <table class="table table-bordered">
                            <thead class="table-light">
                                <tr><th>Item</th><th>Qty</th><th>Price</th><th>Subtotal</th></tr>
                            </thead>
                            <tbody>
                                ${cartItems.map(item => `
                                    <tr>
                                        <td>${escapeHtml(item.productName)}</td>
                                        <td class="text-center">${item.quantity}</td>
                                        <td class="text-end">₹${parseFloat(item.unitPrice).toFixed(2)}</td>
                                        <td class="text-end">₹${parseFloat(item.subtotal).toFixed(2)}</td>
                                    </tr>
                                `).join('')}
                                <tr class="table-active">
                                    <td colspan="3" class="text-end fw-bold">TOTAL:</td>
                                    <td class="text-end fw-bold text-primary fs-5">₹${data.total}</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                    
                    <div class="row mt-4">
                        <div class="col-md-6 text-center">
                            <div class="qr-container p-3 border rounded" style="background: #f8f9fa;">
                                <h6><i class="fas fa-qrcode"></i> Scan to Pay</h6>
                                <div id="qrcode" style="display: flex; justify-content: center; margin: 10px 0;"></div>
                                <p class="small mt-2">UPI ID: stationery@bank</p>
                                <p class="small">Amount: ₹${data.total}</p>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <h6><i class="fas fa-credit-card"></i> Payment Options</h6>
                            <div class="row g-2 mb-3">
                                <div class="col-6">
                                    <div class="payment-method p-3 text-center border rounded" data-method="CASH">
                                        <i class="fas fa-money-bill fa-2x text-success"></i>
                                        <div>Cash</div>
                                        <small>Pay with Cash</small>
                                    </div>
                                </div>
                                <div class="col-6">
                                    <div class="payment-method p-3 text-center border rounded" data-method="UPI">
                                        <i class="fas fa-mobile-alt fa-2x text-primary"></i>
                                        <div>UPI / QR</div>
                                        <small>Scan & Pay</small>
                                    </div>
                                </div>
                            </div>
                            <button class="btn btn-success w-100" id="payNowBtn">
                                <i class="fas fa-rupee-sign"></i> Pay ₹${data.total}
                            </button>
                            <button class="btn btn-secondary w-100 mt-2" id="cancelBillBtn">
                                <i class="fas fa-times"></i> Cancel Bill
                            </button>
                        </div>
                    </div>
                    
                    <div class="text-center mt-3 small text-muted">
                        <p>Thank you for shopping with us!</p>
                    </div>
                </div>
            `;
            
            document.getElementById('billContent').innerHTML = billHTML;
            
            const qrContainer = document.getElementById("qrcode");
            if (qrContainer) {
                qrContainer.innerHTML = "";
                new QRCode(qrContainer, {
                    text: `upi://pay?pa=stationery@bank&pn=StationeryShop&am=${data.total}&cu=INR`,
                    width: 180,
                    height: 180
                });
            }
            
            document.querySelectorAll('.payment-method').forEach(method => {
                method.addEventListener('click', function() {
                    document.querySelectorAll('.payment-method').forEach(m => {
                        m.classList.remove('active', 'border-primary', 'bg-light');
                    });
                    this.classList.add('active', 'border-primary', 'bg-light');
                });
            });
            
            document.querySelector('.payment-method[data-method="CASH"]')?.classList.add('active', 'border-primary', 'bg-light');
            
            document.getElementById('payNowBtn')?.addEventListener('click', processPayment);
            document.getElementById('cancelBillBtn')?.addEventListener('click', cancelBillAndClose);
            
            const billModal = new bootstrap.Modal(document.getElementById('billModal'));
            billModal.show();
            
        } else {
            showToast(data.message || 'Failed to generate bill', 'danger');
        }
    } catch (error) {
        console.error('Error:', error);
        showToast('An error occurred', 'danger');
    }
});

// ========== PAYMENT PROCESSING ==========

async function processPayment() {
    if (!currentBillId) {
        showToast('No active bill found', 'danger');
        return;
    }
    
    const payBtn = document.getElementById('payNowBtn');
    if (payBtn) {
        payBtn.disabled = true;
        payBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Processing...';
    }
    
    showToast('Processing payment...', 'info');
    
    try {
        const res = await fetch(`/api/customer/bill/pay/${currentBillId}`, { method: 'POST' });
        const data = await res.json();
        
        if (data.success) {
            showToast('Payment successful!', 'success');
            // Use billNumber from response, or fallback to bill ID
            const billNumberToShow = data.billNumber || `BILL-${data.billId}`;
            await completePaymentSuccess(data.billId, data.total, billNumberToShow);
        } else {
            showToast('Payment failed: ' + (data.message || 'Unknown error'), 'danger');
            if (payBtn) {
                payBtn.disabled = false;
                payBtn.innerHTML = `<i class="fas fa-rupee-sign"></i> Pay ₹${currentBillTotal}`;
            }
        }
    } catch (error) {
        console.error('Payment error:', error);
        showToast('Payment failed: Network error', 'danger');
        if (payBtn) {
            payBtn.disabled = false;
            payBtn.innerHTML = `<i class="fas fa-rupee-sign"></i> Pay ₹${currentBillTotal}`;
        }
    }
}

// FIXED: Use billNumber from response
async function completePaymentSuccess(billId, billTotal, billNumber) {
    // Close the bill modal
    const billModal = bootstrap.Modal.getInstance(document.getElementById('billModal'));
    if (billModal) billModal.hide();
    
    // Clear cart and refresh UI
    await clearCartAndReset();
    
    // Show success modal WITH the bill number from response
    const successHTML = `
        <div class="success-animation text-center p-4">
            <i class="fas fa-check-circle text-success" style="font-size: 80px; animation: bounce 0.5s;"></i>
            <h2 class="mt-3 text-success">Payment Successful!</h2>
            <p class="lead">Thank you for your purchase!</p>
            <p class="text-muted">Bill #: ${billNumber}</p>
            <p class="text-muted">Amount Paid: ₹${billTotal}</p>
            <div class="mt-4">
                <i class="fas fa-spinner fa-spin"></i> Redirecting...
            </div>
        </div>
    `;
    
    document.getElementById('successContent').innerHTML = successHTML;
    const successModal = new bootstrap.Modal(document.getElementById('successModal'));
    successModal.show();
    
    // Reset global variables
    currentBillId = null;
    currentBillTotal = 0;
    currentBillNumber = null;
    
    setTimeout(() => {
        successModal.hide();
        showToast('Payment completed! Ready for next bill', 'success');
    }, 2000);
}

async function cancelBillAndClose() {
    if (!currentBillId) {
        showToast('No active bill to cancel', 'warning');
        const billModal = bootstrap.Modal.getInstance(document.getElementById('billModal'));
        if (billModal) billModal.hide();
        return;
    }
    
    showToast('Cancelling bill...', 'info');
    
    try {
        await fetch(`/api/customer/bill/cancel/${currentBillId}`, { method: 'POST' });
        await clearCartAndReset();
        
        const billModal = bootstrap.Modal.getInstance(document.getElementById('billModal'));
        if (billModal) billModal.hide();
        
        currentBillId = null;
        currentBillTotal = 0;
        
        showToast('Bill cancelled successfully. Cart cleared.', 'success');
        
    } catch (error) {
        console.error('Error cancelling bill:', error);
        showToast('Error cancelling bill', 'danger');
    }
}

// ========== UTILITY FUNCTIONS ==========

function escapeHtml(text) {
    if (!text) return '';
    return text.replace(/[&<>]/g, function(m) {
        if (m === '&') return '&amp;';
        if (m === '<') return '&lt;';
        if (m === '>') return '&gt;';
        return m;
    });
}

function showToast(message, type) {
    const toast = document.createElement('div');
    toast.className = `alert alert-${type} position-fixed bottom-0 end-0 m-3`;
    toast.style.zIndex = '9999';
    toast.style.minWidth = '250px';
    toast.style.animation = 'slideIn 0.3s';
    toast.style.borderRadius = '8px';
    toast.style.boxShadow = '0 4px 12px rgba(0,0,0,0.15)';
    toast.innerHTML = `<i class="fas fa-${type === 'success' ? 'check-circle' : type === 'danger' ? 'exclamation-circle' : 'info-circle'} me-2"></i> ${message}`;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
}

// ========== CSS ANIMATIONS ==========
const style = document.createElement('style');
style.textContent = `
    @keyframes bounce {
        0%, 100% { transform: scale(1); }
        50% { transform: scale(1.2); }
    }
    @keyframes slideIn {
        from { transform: translateX(100%); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }
    .payment-method {
        cursor: pointer;
        transition: all 0.3s;
    }
    .payment-method:hover {
        transform: scale(1.02);
        box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }
    .payment-method.active {
        border-color: #007bff !important;
        background-color: #f8f9fa !important;
    }
    .qr-container {
        background: #f8f9fa;
        border-radius: 12px;
    }
`;
document.head.appendChild(style);

// ========== INITIALIZE ==========
loadCategories();
loadCart();