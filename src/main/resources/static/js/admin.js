// Global chart instances
let statusChart = null;
let revenueChart = null;

// ========== DASHBOARD ==========
async function loadDashboard() {
    try {
        const statsRes = await fetch('/api/admin/stats');
        const stats = await statsRes.json();
        
        const billsRes = await fetch('/api/admin/bills');
        const bills = await billsRes.json();
        
        const productsRes = await fetch('/api/admin/products');
        const products = await productsRes.json();
        
        const lowStockRes = await fetch('/api/admin/lowstock');
        const lowStock = await lowStockRes.json();
        
        const totalRevenue = stats.PAID_amount || 0;
        const totalBills = (stats.PAID_count || 0) + (stats.FAILED_count || 0) + (stats.PENDING_count || 0) + (stats.EXPIRED_count || 0);
        const totalProducts = products.length;
        const lowStockCount = lowStock.length;
        
        const dashboardHTML = `
            <div class="row mb-4 animate-fade">
                <div class="col-md-3 mb-3">
                    <div class="stat-card" style="background: linear-gradient(135deg, #667eea, #764ba2);">
                        <i class="fas fa-chart-line"></i>
                        <div class="stat-number">₹${totalRevenue.toFixed(2)}</div>
                        <div class="stat-label">Total Revenue</div>
                    </div>
                </div>
                <div class="col-md-3 mb-3">
                    <div class="stat-card" style="background: linear-gradient(135deg, #28a745, #20c997);">
                        <i class="fas fa-receipt"></i>
                        <div class="stat-number">${totalBills}</div>
                        <div class="stat-label">Total Bills</div>
                    </div>
                </div>
                <div class="col-md-3 mb-3">
                    <div class="stat-card" style="background: linear-gradient(135deg, #17a2b8, #0dcaf0);">
                        <i class="fas fa-boxes"></i>
                        <div class="stat-number">${totalProducts}</div>
                        <div class="stat-label">Products</div>
                    </div>
                </div>
                <div class="col-md-3 mb-3">
                    <div class="stat-card" style="background: linear-gradient(135deg, #fd7e14, #ffc107);">
                        <i class="fas fa-exclamation-triangle"></i>
                        <div class="stat-number">${lowStockCount}</div>
                        <div class="stat-label">Low Stock Items</div>
                    </div>
                </div>
            </div>
            
            <div class="row mb-4">
                <div class="col-md-6 mb-3">
                    <div class="chart-card">
                        <div class="chart-card-header">
                            <i class="fas fa-chart-pie"></i> Payment Status Distribution
                        </div>
                        <div class="chart-card-body">
                            <canvas id="paymentStatusChart"></canvas>
                        </div>
                        <div class="chart-card-footer">
                            <i class="fas fa-chart-simple"></i> Total Bills: <strong>${totalBills}</strong>
                        </div>
                    </div>
                </div>
                <div class="col-md-6 mb-3">
                    <div class="chart-card">
                        <div class="chart-card-header">
                            <i class="fas fa-chart-bar"></i> Revenue by Status
                        </div>
                        <div class="chart-card-body">
                            <canvas id="revenueChart"></canvas>
                        </div>
                        <div class="chart-card-footer">
                            <i class="fas fa-rupee-sign"></i> Total Revenue: <strong>₹${totalRevenue.toFixed(2)}</strong>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="row mb-4">
                <div class="col-md-3 mb-3">
                    <div class="card text-center border-success shadow-sm">
                        <div class="card-body">
                            <i class="fas fa-check-circle text-success fa-2x"></i>
                            <h4 class="mt-2 text-success">${stats.PAID_count || 0}</h4>
                            <p class="text-muted small mb-0">Successful</p>
                            <strong>₹${(stats.PAID_amount || 0).toFixed(2)}</strong>
                        </div>
                    </div>
                </div>
                <div class="col-md-3 mb-3">
                    <div class="card text-center border-danger shadow-sm">
                        <div class="card-body">
                            <i class="fas fa-times-circle text-danger fa-2x"></i>
                            <h4 class="mt-2 text-danger">${stats.FAILED_count || 0}</h4>
                            <p class="text-muted small mb-0">Failed</p>
                            <strong>₹${(stats.FAILED_amount || 0).toFixed(2)}</strong>
                        </div>
                    </div>
                </div>
                <div class="col-md-3 mb-3">
                    <div class="card text-center border-warning shadow-sm">
                        <div class="card-body">
                            <i class="fas fa-clock text-warning fa-2x"></i>
                            <h4 class="mt-2 text-warning">${stats.PENDING_count || 0}</h4>
                            <p class="text-muted small mb-0">Pending</p>
                            <strong>₹${(stats.PENDING_amount || 0).toFixed(2)}</strong>
                        </div>
                    </div>
                </div>
                <div class="col-md-3 mb-3">
                    <div class="card text-center border-secondary shadow-sm">
                        <div class="card-body">
                            <i class="fas fa-hourglass-end text-secondary fa-2x"></i>
                            <h4 class="mt-2 text-secondary">${stats.EXPIRED_count || 0}</h4>
                            <p class="text-muted small mb-0">Expired</p>
                            <strong>₹${(stats.EXPIRED_amount || 0).toFixed(2)}</strong>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="chart-card">
                <div class="chart-card-header">
                    <i class="fas fa-history"></i> Recent Transactions
                </div>
                <div class="table-responsive">
                    <table class="table table-hover mb-0" style="width: 100%;">
                        <thead style="background: #f8f9fa;">
                            <tr>
                                <th style="width: 10%;">ID</th>
                                <th style="width: 30%;">Bill Number</th>
                                <th style="width: 25%;">Date</th>
                                <th style="width: 15%;">Amount</th>
                                <th style="width: 20%;">Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${bills.slice(0, 8).map(bill => `
                                <tr>
                                    <td><strong>#${bill.id}</strong></td>
                                    <td><code>${bill.billNumber}</code></td>
                                    <td>${new Date(bill.date).toLocaleDateString()}</td>
                                    <td class="fw-bold text-primary">₹${parseFloat(bill.total).toFixed(2)}</td>
                                    <td>${getStatusBadge(bill.status)}</td>
                                </tr>
                            `).join('')}
                            ${bills.length === 0 ? '<tr><td colspan="5" class="text-center">No bills found</td></tr>' : ''}
                        </tbody>
                    </table>
                </div>
            </div>
        `;
        
        document.getElementById('tabContent').innerHTML = dashboardHTML;
        
        setTimeout(() => {
            createPaymentChart(stats, totalBills);
            createRevenueChart(stats);
        }, 100);
        
    } catch (error) {
        console.error('Error:', error);
        document.getElementById('tabContent').innerHTML = '<div class="alert alert-danger">Error loading dashboard</div>';
    }
}

function createPaymentChart(stats, totalBills) {
    const ctx = document.getElementById('paymentStatusChart');
    if (!ctx) return;
    if (statusChart) statusChart.destroy();
    
    statusChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['Paid', 'Failed', 'Pending', 'Expired'],
            datasets: [{
                data: [stats.PAID_count || 0, stats.FAILED_count || 0, stats.PENDING_count || 0, stats.EXPIRED_count || 0],
                backgroundColor: ['#28a745', '#dc3545', '#ffc107', '#6c757d'],
                borderWidth: 0,
                cutout: '55%'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: { position: 'bottom', labels: { font: { size: 10 }, boxWidth: 10 } },
                tooltip: { callbacks: { label: (ctx) => `${ctx.label}: ${ctx.raw} (${((ctx.raw/totalBills)*100).toFixed(1)}%)` } }
            }
        }
    });
}

function createRevenueChart(stats) {
    const ctx = document.getElementById('revenueChart');
    if (!ctx) return;
    if (revenueChart) revenueChart.destroy();
    
    revenueChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: ['Paid', 'Failed', 'Pending', 'Expired'],
            datasets: [{
                label: '₹',
                data: [stats.PAID_amount || 0, stats.FAILED_amount || 0, stats.PENDING_amount || 0, stats.EXPIRED_amount || 0],
                backgroundColor: ['#28a745', '#dc3545', '#ffc107', '#6c757d'],
                borderRadius: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: { legend: { display: false } },
            scales: { y: { beginAtZero: true, ticks: { callback: (v) => '₹' + v } } }
        }
    });
}

function getStatusBadge(status) {
    const badges = {
        'PAID': '<span class="status-paid"><i class="fas fa-check-circle"></i> PAID</span>',
        'PENDING': '<span class="status-pending"><i class="fas fa-clock"></i> PENDING</span>',
        'FAILED': '<span class="status-failed"><i class="fas fa-times-circle"></i> FAILED</span>',
        'EXPIRED': '<span class="status-expired"><i class="fas fa-hourglass-end"></i> EXPIRED</span>'
    };
    return badges[status] || `<span class="badge bg-secondary">${status}</span>`;
}

// ========== PRODUCTS MANAGEMENT (FIXED TABLE ALIGNMENT) ==========
async function loadProducts() {
    try {
        const res = await fetch('/api/admin/products');
        const prods = await res.json();
        
        let html = `
            <div class="filter-section">
                <div class="row align-items-center">
                    <div class="col-md-4 mb-2 mb-md-0">
                        <div class="input-group">
                            <span class="input-group-text"><i class="fas fa-search"></i></span>
                            <input type="text" id="searchProduct" class="form-control" placeholder="Search products...">
                        </div>
                    </div>
                    <div class="col-md-3 mb-2 mb-md-0">
                        <select id="filterCategory" class="form-control">
                            <option value="">All Categories</option>
                            <option value="1">✍️ Writing Instruments</option>
                            <option value="2">📄 Paper Products</option>
                            <option value="3">📎 Office Supplies</option>
                            <option value="4">🎨 Art & Craft</option>
                            <option value="5">📐 Geometry & Tools</option>
                        </select>
                    </div>
                    <div class="col-md-5 text-md-end">
                        <button class="btn btn-success" onclick="showAddProductModal()">
                            <i class="fas fa-plus-circle"></i> Add New Product
                        </button>
                    </div>
                </div>
            </div>
            
            <div class="table-responsive">
                <table class="table table-bordered table-hover" style="background: white; border-radius: 12px; overflow: hidden;">
                    <thead style="background: linear-gradient(135deg, #667eea, #764ba2); color: white;">
                        <tr>
                            <th style="width: 8%; text-align: center;">ID</th>
                            <th style="width: 32%;">Product Name</th>
                            <th style="width: 20%;">Category</th>
                            <th style="width: 12%; text-align: right;">Price (₹)</th>
                            <th style="width: 12%; text-align: right;">Stock</th>
                            <th style="width: 16%; text-align: center;">Actions</th>
                        </tr>
                    </thead>
                    <tbody id="productsTableBody">
                        ${prods.map(p => `
                            <tr data-category="${p.categoryId}" data-name="${p.name.toLowerCase()}" data-id="${p.id}">
                                <td style="text-align: center; vertical-align: middle;"><strong>#${p.id}</strong></td>
                                <td style="vertical-align: middle;"><span class="fw-bold">${escapeHtml(p.name)}</span></td>
                                <td style="vertical-align: middle;">${getCategoryName(p.categoryId)}</td>
                                <td style="text-align: right; vertical-align: middle;">
                                    <span class="current-price-${p.id}">₹${parseFloat(p.price).toFixed(2)}</span>
                                </td>
                                <td style="text-align: right; vertical-align: middle;">
                                    <span class="current-stock-${p.id}">${p.stockQuantity}</span> <span class="text-muted">units</span>
                                </td>
                                <td style="text-align: center; vertical-align: middle;">
                                    <button class="btn btn-sm btn-outline-primary me-1" onclick="updatePrice(${p.id}, ${p.price})" title="Update Price">
                                        <i class="fas fa-tag"></i> Price
                                    </button>
                                    <button class="btn btn-sm btn-outline-warning me-1" onclick="updateStock(${p.id}, ${p.stockQuantity})" title="Update Stock">
                                        <i class="fas fa-boxes"></i> Stock
                                    </button>
                                    <button class="btn btn-sm btn-outline-danger" onclick="deleteProduct(${p.id}, '${escapeHtml(p.name)}')" title="Delete Product">
                                        <i class="fas fa-trash"></i>
                                    </button>
                                </td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            </div>
        `;
        
        document.getElementById('tabContent').innerHTML = html;
        
        // Add filter event listeners
        const searchInput = document.getElementById('searchProduct');
        const categorySelect = document.getElementById('filterCategory');
        
        if (searchInput) searchInput.addEventListener('keyup', filterProducts);
        if (categorySelect) categorySelect.addEventListener('change', filterProducts);
        
    } catch (error) {
        console.error('Error loading products:', error);
        document.getElementById('tabContent').innerHTML = '<div class="alert alert-danger">Error loading products</div>';
    }
}

function getCategoryName(catId) {
    const categories = {
        1: '✍️ Writing Instruments',
        2: '📄 Paper Products',
        3: '📎 Office Supplies', 
        4: '🎨 Art & Craft',
        5: '📐 Geometry & Tools'
    };
    return categories[catId] || 'Unknown';
}

function filterProducts() {
    const searchTerm = document.getElementById('searchProduct')?.value.toLowerCase() || '';
    const categoryFilter = document.getElementById('filterCategory')?.value || '';
    
    const rows = document.querySelectorAll('#productsTableBody tr');
    rows.forEach(row => {
        const name = row.dataset.name || '';
        const category = row.dataset.category || '';
        
        const matchesSearch = name.includes(searchTerm);
        const matchesCategory = !categoryFilter || category === categoryFilter;
        
        row.style.display = (matchesSearch && matchesCategory) ? '' : 'none';
    });
}

async function updatePrice(productId, currentPrice) {
    const newPrice = prompt(`Enter new price for product #${productId}\nCurrent Price: ₹${currentPrice}`, currentPrice);
    
    if (newPrice && !isNaN(newPrice) && parseFloat(newPrice) > 0) {
        try {
            const res = await fetch(`/api/admin/products/${productId}/price?price=${newPrice}`, { method: 'PUT' });
            const data = await res.json();
            
            if (data.success) {
                const priceSpan = document.querySelector(`.current-price-${productId}`);
                if (priceSpan) priceSpan.innerHTML = `₹${parseFloat(newPrice).toFixed(2)}`;
                showToast(`Price updated to ₹${newPrice}`, 'success');
            } else {
                showToast('Failed to update price', 'danger');
            }
        } catch (error) {
            showToast('Error updating price', 'danger');
        }
    } else if (newPrice) {
        showToast('Please enter a valid price', 'warning');
    }
}

async function updateStock(productId, currentStock) {
    const newStock = prompt(`Enter new stock quantity for product #${productId}\nCurrent Stock: ${currentStock} units`, currentStock);
    
    if (newStock && !isNaN(newStock) && parseInt(newStock) >= 0) {
        try {
            const res = await fetch(`/api/admin/products/${productId}/stock?stock=${newStock}`, { method: 'PUT' });
            const data = await res.json();
            
            if (data.success) {
                const stockSpan = document.querySelector(`.current-stock-${productId}`);
                if (stockSpan) stockSpan.innerHTML = newStock;
                showToast(`Stock updated to ${newStock} units`, 'success');
                
                if (parseInt(newStock) < 10) {
                    showToast(`⚠️ Warning: Stock is low (${newStock} units remaining)`, 'warning');
                }
            } else {
                showToast('Failed to update stock', 'danger');
            }
        } catch (error) {
            showToast('Error updating stock', 'danger');
        }
    } else if (newStock) {
        showToast('Please enter a valid stock quantity', 'warning');
    }
}

async function deleteProduct(productId, productName) {
    const confirmed = confirm(`Are you sure you want to delete "${productName}"?\n\nThis action cannot be undone.`);
    
    if (confirmed) {
        try {
            const res = await fetch(`/api/admin/products/${productId}`, { method: 'DELETE' });
            const data = await res.json();
            
            if (data.success) {
                showToast(`"${productName}" has been deleted`, 'success');
                loadProducts(); // Reload the products list
            } else {
                showToast('Failed to delete product', 'danger');
            }
        } catch (error) {
            showToast('Error deleting product', 'danger');
        }
    }
}

function showAddProductModal() {
    const modalHTML = `
        <div class="modal fade" id="addProductModal" tabindex="-1" data-bs-backdrop="static">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content" style="border-radius: 15px;">
                    <div class="modal-header" style="background: linear-gradient(135deg, #667eea, #764ba2); color: white; border: none;">
                        <h5 class="modal-title"><i class="fas fa-plus-circle"></i> Add New Product</h5>
                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body" style="padding: 20px;">
                        <div class="mb-3">
                            <label class="form-label fw-bold">Product Name</label>
                            <input type="text" id="newProductName" class="form-control" placeholder="e.g., Gel Pen">
                        </div>
                        <div class="mb-3">
                            <label class="form-label fw-bold">Category</label>
                            <select id="newProductCategory" class="form-control">
                                <option value="1">✍️ Writing Instruments</option>
                                <option value="2">📄 Paper Products</option>
                                <option value="3">📎 Office Supplies</option>
                                <option value="4">🎨 Art & Craft</option>
                                <option value="5">📐 Geometry & Tools</option>
                            </select>
                        </div>
                        <div class="row">
                            <div class="col-md-6 mb-3">
                                <label class="form-label fw-bold">Price (₹)</label>
                                <input type="number" id="newProductPrice" class="form-control" placeholder="e.g., 25.00" step="0.01">
                            </div>
                            <div class="col-md-6 mb-3">
                                <label class="form-label fw-bold">Initial Stock</label>
                                <input type="number" id="newProductStock" class="form-control" placeholder="e.g., 100">
                            </div>
                        </div>
                        <div class="mb-3">
                            <label class="form-label fw-bold">Description (Optional)</label>
                            <textarea id="newProductDesc" class="form-control" rows="2" placeholder="Product description..."></textarea>
                        </div>
                    </div>
                    <div class="modal-footer" style="border-top: 1px solid #eee;">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                        <button type="button" class="btn btn-primary" onclick="addNewProduct()">Add Product</button>
                    </div>
                </div>
            </div>
        </div>
    `;
    
    // Remove existing modal if any
    const existingModal = document.getElementById('addProductModal');
    if (existingModal) existingModal.remove();
    
    document.body.insertAdjacentHTML('beforeend', modalHTML);
    const modal = new bootstrap.Modal(document.getElementById('addProductModal'));
    modal.show();
}

async function addNewProduct() {
    const name = document.getElementById('newProductName')?.value;
    const categoryId = parseInt(document.getElementById('newProductCategory')?.value);
    const price = parseFloat(document.getElementById('newProductPrice')?.value);
    const stock = parseInt(document.getElementById('newProductStock')?.value);
    const description = document.getElementById('newProductDesc')?.value || "";
    
    if (!name) {
        showToast('Please enter product name', 'warning');
        return;
    }
    if (!price || isNaN(price) || price <= 0) {
        showToast('Please enter a valid price', 'warning');
        return;
    }
    if (isNaN(stock) || stock < 0) {
        showToast('Please enter a valid stock quantity', 'warning');
        return;
    }
    
    const newProduct = {
        name: name,
        categoryId: categoryId,
        price: price,
        stockQuantity: stock,
        description: description,
        reorderLevel: 10
    };
    
    try {
        const res = await fetch('/api/admin/products', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(newProduct)
        });
        const data = await res.json();
        
        if (data.success) {
            showToast(`"${name}" added successfully!`, 'success');
            const modal = bootstrap.Modal.getInstance(document.getElementById('addProductModal'));
            if (modal) modal.hide();
            loadProducts(); // Refresh the product list
        } else {
            showToast('Failed to add product', 'danger');
        }
    } catch (error) {
        console.error('Error:', error);
        showToast('Error adding product', 'danger');
    }
}

// ========== BILLS MANAGEMENT ==========
async function loadBills() {
    try {
        const res = await fetch('/api/admin/bills');
        const bills = await res.json();
        const sorted = [...bills].sort((a,b) => b.id - a.id);
        const paid = sorted.filter(b => b.status === 'PAID').length;
        const pending = sorted.filter(b => b.status === 'PENDING').length;
        const failed = sorted.filter(b => b.status === 'FAILED').length;
        const expired = sorted.filter(b => b.status === 'EXPIRED').length;
        
        let html = `
            <div class="row mb-4">
                <div class="col-md-3 mb-3">
                    <div class="card text-center border-success shadow-sm">
                        <div class="card-body">
                            <i class="fas fa-check-circle text-success fa-2x"></i>
                            <h4 class="mt-2 text-success">${paid}</h4>
                            <p class="text-muted small mb-0">Paid Bills</p>
                        </div>
                    </div>
                </div>
                <div class="col-md-3 mb-3">
                    <div class="card text-center border-warning shadow-sm">
                        <div class="card-body">
                            <i class="fas fa-clock text-warning fa-2x"></i>
                            <h4 class="mt-2 text-warning">${pending}</h4>
                            <p class="text-muted small mb-0">Pending Bills</p>
                        </div>
                    </div>
                </div>
                <div class="col-md-3 mb-3">
                    <div class="card text-center border-danger shadow-sm">
                        <div class="card-body">
                            <i class="fas fa-times-circle text-danger fa-2x"></i>
                            <h4 class="mt-2 text-danger">${failed}</h4>
                            <p class="text-muted small mb-0">Failed Bills</p>
                        </div>
                    </div>
                </div>
                <div class="col-md-3 mb-3">
                    <div class="card text-center border-secondary shadow-sm">
                        <div class="card-body">
                            <i class="fas fa-hourglass-end text-secondary fa-2x"></i>
                            <h4 class="mt-2 text-secondary">${expired}</h4>
                            <p class="text-muted small mb-0">Expired Bills</p>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="filter-section">
                <div class="row">
                    <div class="col-md-4 mb-2 mb-md-0">
                        <div class="input-group">
                            <span class="input-group-text"><i class="fas fa-search"></i></span>
                            <input type="text" id="searchBill" class="form-control" placeholder="Search by bill number...">
                        </div>
                    </div>
                    <div class="col-md-3 mb-2 mb-md-0">
                        <select id="filterStatus" class="form-control">
                            <option value="">All Status</option>
                            <option value="PAID">✅ Paid</option>
                            <option value="PENDING">⏳ Pending</option>
                            <option value="FAILED">❌ Failed</option>
                            <option value="EXPIRED">⌛ Expired</option>
                        </select>
                    </div>
                    <div class="col-md-3 mb-2 mb-md-0">
                        <input type="date" id="filterDate" class="form-control">
                    </div>
                    <div class="col-md-2">
                        <button class="btn btn-outline-secondary w-100" onclick="clearBillFilters()">
                            <i class="fas fa-undo-alt"></i> Clear
                        </button>
                    </div>
                </div>
            </div>
            
            <div class="table-responsive">
                <table class="table table-bordered table-hover" style="background: white; border-radius: 12px; overflow: hidden;">
                    <thead style="background: linear-gradient(135deg, #667eea, #764ba2); color: white;">
                        <tr>
                            <th style="width: 8%; text-align: center;">ID</th>
                            <th style="width: 25%;">Bill Number</th>
                            <th style="width: 20%;">Date</th>
                            <th style="width: 15%; text-align: right;">Amount</th>
                            <th style="width: 20%; text-align: center;">Status</th>
                            <th style="width: 12%; text-align: center;">Action</th>
                        </tr>
                    </thead>
                    <tbody id="billsTableBody">
                        ${sorted.map(bill => `
                            <tr data-status="${bill.status}" data-billnumber="${bill.billNumber.toLowerCase()}" data-date="${bill.date.split(' ')[0]}">
                                <td style="text-align: center; vertical-align: middle;"><strong>#${bill.id}</strong></td>
                                <td style="vertical-align: middle;"><code>${bill.billNumber}</code></td>
                                <td style="vertical-align: middle;">${new Date(bill.date).toLocaleDateString()}</td>
                                <td style="text-align: right; vertical-align: middle;" class="fw-bold text-primary">₹${parseFloat(bill.total).toFixed(2)}</td>
                                <td style="text-align: center; vertical-align: middle;">${getStatusBadge(bill.status)}</td>
                                <td style="text-align: center; vertical-align: middle;">
                                    <button class="btn btn-sm btn-primary" onclick="viewBillDetails(${bill.id})">
                                        <i class="fas fa-eye"></i> View
                                    </button>
                                </td>
                            </tr>
                        `).join('')}
                        ${sorted.length === 0 ? '<tr><td colspan="6" class="text-center">No bills found</td></tr>' : ''}
                    </tbody>
                </table>
            </div>
        `;
        
        document.getElementById('tabContent').innerHTML = html;
        
        // Attach filter events
        const searchBill = document.getElementById('searchBill');
        const filterStatus = document.getElementById('filterStatus');
        const filterDate = document.getElementById('filterDate');
        
        if (searchBill) searchBill.addEventListener('keyup', filterBills);
        if (filterStatus) filterStatus.addEventListener('change', filterBills);
        if (filterDate) filterDate.addEventListener('change', filterBills);
        
    } catch (error) {
        console.error('Error loading bills:', error);
        document.getElementById('tabContent').innerHTML = '<div class="alert alert-danger">Error loading bills</div>';
    }
}

function filterBills() {
    const searchTerm = document.getElementById('searchBill')?.value.toLowerCase() || '';
    const statusFilter = document.getElementById('filterStatus')?.value || '';
    const dateFilter = document.getElementById('filterDate')?.value || '';
    
    const rows = document.querySelectorAll('#billsTableBody tr');
    rows.forEach(row => {
        const billNumber = row.dataset.billnumber || '';
        const status = row.dataset.status || '';
        const date = row.dataset.date || '';
        
        const matchesSearch = billNumber.includes(searchTerm);
        const matchesStatus = !statusFilter || status === statusFilter;
        const matchesDate = !dateFilter || date === dateFilter;
        
        row.style.display = (matchesSearch && matchesStatus && matchesDate) ? '' : 'none';
    });
}

function clearBillFilters() {
    const searchInput = document.getElementById('searchBill');
    const statusSelect = document.getElementById('filterStatus');
    const dateInput = document.getElementById('filterDate');
    
    if (searchInput) searchInput.value = '';
    if (statusSelect) statusSelect.value = '';
    if (dateInput) dateInput.value = '';
    
    filterBills();
}

// View Bill Details with Professional Modal
async function viewBillDetails(billId) {
    try {
        const res = await fetch(`/api/admin/bill/status/${billId}`);
        const bill = await res.json();
        
        if (!bill || bill.success === false) {
            showToast('Bill not found', 'danger');
            return;
        }
        
        const modalHTML = `
            <div id="billReceiptModal" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.6); z-index: 10000; justify-content: center; align-items: center;">
                <div style="background: white; border-radius: 20px; width: 90%; max-width: 500px; margin: 20px; box-shadow: 0 20px 40px rgba(0,0,0,0.3); animation: fadeInModal 0.3s;">
                    <div style="background: linear-gradient(135deg, #667eea, #764ba2); color: white; padding: 20px; text-align: center; border-radius: 20px 20px 0 0;">
                        <i class="fas fa-receipt" style="font-size: 2rem; margin-bottom: 10px;"></i>
                        <h3 style="margin: 0;">TAX INVOICE</h3>
                        <p style="margin: 5px 0 0; opacity: 0.9;">Stationery Shop</p>
                    </div>
                    
                    <div style="padding: 20px;">
                        <div style="border-bottom: 2px dashed #dee2e6; padding-bottom: 15px; margin-bottom: 15px;">
                            <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
                                <span style="color: #666;">Bill Number:</span>
                                <strong><code>${bill.billNumber || 'N/A'}</code></strong>
                            </div>
                            <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
                                <span style="color: #666;">Bill Date:</span>
                                <strong>${bill.billDate ? new Date(bill.billDate).toLocaleString() : 'N/A'}</strong>
                            </div>
                            <div style="display: flex; justify-content: space-between;">
                                <span style="color: #666;">Status:</span>
                                <span>${getStatusBadge(bill.status)}</span>
                            </div>
                        </div>
                        
                        <div style="background: #f8f9fa; padding: 15px; border-radius: 12px; margin-bottom: 15px;">
                            <div style="display: flex; justify-content: space-between; margin-bottom: 10px;">
                                <span>Bill ID:</span>
                                <strong>#${bill.id || billId}</strong>
                            </div>
                            <div style="display: flex; justify-content: space-between;">
                                <span style="font-size: 1.1rem;">Total Amount:</span>
                                <strong style="font-size: 1.4rem; color: #28a745;">₹${parseFloat(bill.total || 0).toFixed(2)}</strong>
                            </div>
                        </div>
                        
                        ${bill.paymentDate ? `
                        <div style="border-top: 1px solid #eee; padding-top: 15px;">
                            <div style="display: flex; justify-content: space-between;">
                                <span style="color: #666;">Payment Date:</span>
                                <strong>${new Date(bill.paymentDate).toLocaleString()}</strong>
                            </div>
                        </div>
                        ` : ''}
                        
                        <div style="border-top: 1px solid #eee; padding-top: 15px; margin-top: 15px; text-align: center;">
                            <small style="color: #999;">Thank you for shopping with us!</small>
                        </div>
                    </div>
                    
                    <div style="border-top: 1px solid #eee; padding: 15px 20px; display: flex; justify-content: flex-end; gap: 10px; background: #f8f9fa; border-radius: 0 0 20px 20px;">
                        <button onclick="closeBillModal()" style="padding: 8px 20px; background: #6c757d; color: white; border: none; border-radius: 8px; cursor: pointer;">
                            <i class="fas fa-times"></i> Close
                        </button>
                        <button onclick="printBillReceipt()" style="padding: 8px 20px; background: linear-gradient(135deg, #667eea, #764ba2); color: white; border: none; border-radius: 8px; cursor: pointer;">
                            <i class="fas fa-print"></i> Print
                        </button>
                    </div>
                </div>
            </div>
        `;
        
        const existingModal = document.getElementById('billReceiptModal');
        if (existingModal) existingModal.remove();
        
        document.body.insertAdjacentHTML('beforeend', modalHTML);
        const modalElement = document.getElementById('billReceiptModal');
        modalElement.style.display = 'flex';
        
        modalElement.addEventListener('click', function(e) {
            if (e.target === modalElement) {
                closeBillModal();
            }
        });
        
    } catch (error) {
        console.error('Error:', error);
        showToast('Error loading bill details', 'danger');
    }
}

function closeBillModal() {
    const modal = document.getElementById('billReceiptModal');
    if (modal) modal.remove();
}

function printBillReceipt() {
    const modalContent = document.getElementById('billReceiptModal');
    if (!modalContent) return;
    
    const printContent = modalContent.cloneNode(true);
    printContent.style.display = 'flex';
    printContent.style.position = 'relative';
    printContent.style.background = 'white';
    
    const printWindow = window.open('', '_blank');
    printWindow.document.write(`
        <html>
            <head>
                <title>Bill Receipt</title>
                <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
                <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
                <style>
                    body { padding: 20px; }
                    @media print {
                        button, .btn { display: none; }
                        body { padding: 0; }
                    }
                </style>
            </head>
            <body>
                ${printContent.innerHTML}
                <script>
                    window.print();
                    setTimeout(() => window.close(), 1000);
                <\/script>
            </body>
        </html>
    `);
    printWindow.document.close();
}

// ========== LOW STOCK ==========
async function loadLowStock() {
    try {
        const res = await fetch('/api/admin/lowstock');
        const products = await res.json();
        
        if (products.length === 0) {
            document.getElementById('tabContent').innerHTML = `
                <div class="chart-card">
                    <div class="chart-card-header">
                        <i class="fas fa-exclamation-triangle"></i> Low Stock Alerts
                    </div>
                    <div class="text-center p-5">
                        <i class="fas fa-check-circle text-success fa-3x mb-3"></i>
                        <h4 class="text-success">All products have sufficient stock!</h4>
                        <p class="text-muted">No items below reorder level.</p>
                    </div>
                </div>
            `;
            return;
        }
        
        document.getElementById('tabContent').innerHTML = `
            <div class="chart-card">
                <div class="chart-card-header">
                    <i class="fas fa-exclamation-triangle text-warning"></i> Low Stock Alerts (${products.length} items)
                </div>
                <div class="table-responsive">
                    <table class="table table-bordered table-hover mb-0" style="background: white;">
                        <thead style="background: linear-gradient(135deg, #667eea, #764ba2); color: white;">
                            <tr>
                                <th style="width: 10%; text-align: center;">ID</th>
                                <th style="width: 45%;">Product Name</th>
                                <th style="width: 15%; text-align: center;">Current Stock</th>
                                <th style="width: 15%; text-align: center;">Reorder Level</th>
                                <th style="width: 15%; text-align: center;">Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${products.map(p => `
                                <tr>
                                    <td style="text-align: center;"><strong>#${p.id}</strong></td>
                                    <td><strong>${escapeHtml(p.name)}</strong></td>
                                    <td style="text-align: center;"><span class="badge bg-danger" style="font-size: 1rem;">${p.stockQuantity}</span></td>
                                    <td style="text-align: center;">${p.reorderLevel}</td>
                                    <td style="text-align: center;">
                                        <button class="btn btn-sm btn-warning" onclick="updateStock(${p.id}, ${p.stockQuantity})">
                                            <i class="fas fa-truck"></i> Restock
                                        </button>
                                    </td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>
            </div>
        `;
    } catch (error) {
        console.error('Error:', error);
        document.getElementById('tabContent').innerHTML = '<div class="alert alert-danger">Error loading low stock alerts</div>';
    }
}

// ========== UTILITIES ==========
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
    toast.style.minWidth = '280px';
    toast.style.animation = 'slideIn 0.3s';
    toast.style.boxShadow = '0 4px 12px rgba(0,0,0,0.15)';
    toast.style.borderRadius = '8px';
    toast.innerHTML = `<i class="fas fa-${type === 'success' ? 'check-circle' : type === 'danger' ? 'exclamation-circle' : 'info-circle'} me-2"></i> ${message}`;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
}

// Add animation styles
const modalStyle = document.createElement('style');
modalStyle.textContent = `
    @keyframes fadeInModal {
        from { opacity: 0; transform: scale(0.95); }
        to { opacity: 1; transform: scale(1); }
    }
    @keyframes slideIn {
        from { transform: translateX(100%); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }
`;
document.head.appendChild(modalStyle);

// ========== TAB NAVIGATION ==========
document.querySelectorAll('#adminTabs .nav-link').forEach(tab => {
    tab.addEventListener('click', () => {
        const target = tab.dataset.tab;
        document.querySelectorAll('#adminTabs .nav-link').forEach(t => t.classList.remove('active'));
        tab.classList.add('active');
        
        if (target === 'dashboard') loadDashboard();
        else if (target === 'products') loadProducts();
        else if (target === 'bills') loadBills();
        else if (target === 'lowstock') loadLowStock();
    });
});

// Load dashboard by default
loadDashboard();