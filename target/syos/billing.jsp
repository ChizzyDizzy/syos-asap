<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.syos.web.model.User" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("login.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <title>Billing - SYOS POS</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: Arial, sans-serif;
            background: #f5f5f5;
        }
        .navbar {
            background: #333;
            color: white;
            padding: 15px 30px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .navbar h1 { font-size: 24px; }
        .navbar a {
            color: white;
            text-decoration: none;
            padding: 8px 15px;
            background: #555;
            border-radius: 5px;
            margin-left: 10px;
        }
        .container {
            max-width: 1400px;
            margin: 30px auto;
            padding: 0 20px;
            display: grid;
            grid-template-columns: 2fr 1fr;
            gap: 20px;
        }
        .products-section, .cart-section {
            background: white;
            padding: 20px;
            border-radius: 10px;
            box-shadow: 0 2px 5px rgba(0,0,0,0.1);
        }
        h2 {
            margin-bottom: 20px;
            color: #333;
        }
        .products-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
            gap: 15px;
            max-height: 600px;
            overflow-y: auto;
        }
        .product-card {
            border: 1px solid #ddd;
            padding: 15px;
            border-radius: 5px;
            cursor: pointer;
            transition: transform 0.2s;
        }
        .product-card:hover {
            transform: scale(1.05);
            border-color: #667eea;
        }
        .product-card h3 {
            font-size: 16px;
            margin-bottom: 5px;
            color: #333;
        }
        .product-card .price {
            color: #667eea;
            font-weight: bold;
            font-size: 18px;
        }
        .product-card .stock {
            font-size: 12px;
            color: #666;
        }
        .cart-items {
            max-height: 400px;
            overflow-y: auto;
            margin-bottom: 20px;
        }
        .cart-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 10px;
            border-bottom: 1px solid #eee;
        }
        .cart-item-details { flex: 1; }
        .cart-item-name { font-weight: bold; }
        .cart-item-price { color: #666; font-size: 14px; }
        .cart-item-qty {
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .qty-btn {
            padding: 5px 10px;
            background: #667eea;
            color: white;
            border: none;
            border-radius: 3px;
            cursor: pointer;
        }
        .remove-btn {
            padding: 5px 10px;
            background: #e74c3c;
            color: white;
            border: none;
            border-radius: 3px;
            cursor: pointer;
        }
        .cart-summary {
            border-top: 2px solid #333;
            padding-top: 15px;
        }
        .summary-row {
            display: flex;
            justify-content: space-between;
            margin-bottom: 10px;
            font-size: 18px;
        }
        .summary-row.total {
            font-weight: bold;
            font-size: 24px;
            color: #667eea;
        }
        .payment-section {
            margin-top: 20px;
        }
        .payment-section input {
            width: 100%;
            padding: 12px;
            font-size: 18px;
            border: 1px solid #ddd;
            border-radius: 5px;
            margin-bottom: 10px;
        }
        .checkout-btn {
            width: 100%;
            padding: 15px;
            background: #27ae60;
            color: white;
            border: none;
            border-radius: 5px;
            font-size: 18px;
            cursor: pointer;
            font-weight: bold;
        }
        .checkout-btn:hover { background: #229954; }
        .checkout-btn:disabled {
            background: #95a5a6;
            cursor: not-allowed;
        }
        .empty-cart {
            text-align: center;
            color: #999;
            padding: 40px;
        }
    </style>
</head>
<body>
<div class="navbar">
    <h1>💳 Billing System</h1>
    <div>
        <% if ("ADMIN".equals(user.getRole().toString())) { %>
        <a href="<%= request.getContextPath() %>/admin-dashboard.jsp">Dashboard</a>
        <% } else { %>
        <a href="<%= request.getContextPath() %>/cashier-dashboard.jsp">Dashboard</a>
        <% } %>
        <a href="<%= request.getContextPath() %>/logout">Logout</a>
    </div>
</div>
<div class="container">
    <div class="products-section">
        <h2>Available Products</h2>
        <div class="products-grid" id="productsGrid">
            <p>Loading products...</p>
        </div>
    </div>

    <div class="cart-section">
        <h2>Shopping Cart</h2>
        <div class="cart-items" id="cartItems">
            <div class="empty-cart">Cart is empty</div>
        </div>

        <div class="cart-summary" id="cartSummary" style="display:none;">
            <div class="summary-row">
                <span>Subtotal:</span>
                <span id="subtotal">$0.00</span>
            </div>
            <div class="summary-row total">
                <span>Total:</span>
                <span id="total">$0.00</span>
            </div>

            <div class="payment-section">
                <input type="number" id="cashReceived" placeholder="Cash Received" step="0.01">
                <div class="summary-row" id="changeRow" style="display:none;">
                    <span>Change:</span>
                    <span id="change">$0.00</span>
                </div>
                <button class="checkout-btn" id="checkoutBtn" onclick="checkout()">Complete Sale</button>
            </div>
        </div>
    </div>
</div>

<script>
    let products = [];
    let cart = [];

    function loadProducts() {
        fetch('products')
            .then(res => res.json())
            .then(data => {
                products = data;
                renderProducts();
            });
    }

    function renderProducts() {
        const grid = document.getElementById('productsGrid');
        grid.innerHTML = products.map(p => `
                <div class="product-card" onclick="addToCart('${p.itemCode}')">
                    <h3>${p.name}</h3>
                    <div class="price">$${p.price.toFixed(2)}</div>
                    <div class="stock">Stock: ${p.quantityOnShelf}</div>
                    <div class="stock">${p.category}</div>
                </div>
            `).join('');
    }

    function addToCart(itemCode) {
        const product = products.find(p => p.itemCode === itemCode);
        if (!product) return;

        const cartItem = cart.find(item => item.itemCode === itemCode);

        if (cartItem) {
            if (cartItem.quantity < product.quantityOnShelf) {
                cartItem.quantity++;
            } else {
                alert('Not enough stock!');
                return;
            }
        } else {
            cart.push({
                itemCode: product.itemCode,
                itemName: product.name,
                unitPrice: product.price,
                quantity: 1,
                maxStock: product.quantityOnShelf
            });
        }

        renderCart();
    }

    function updateQuantity(itemCode, change) {
        const cartItem = cart.find(item => item.itemCode === itemCode);
        if (!cartItem) return;

        const newQty = cartItem.quantity + change;

        if (newQty <= 0) {
            removeFromCart(itemCode);
            return;
        }

        if (newQty > cartItem.maxStock) {
            alert('Not enough stock!');
            return;
        }

        cartItem.quantity = newQty;
        renderCart();
    }

    function removeFromCart(itemCode) {
        cart = cart.filter(item => item.itemCode !== itemCode);
        renderCart();
    }

    function renderCart() {
        const cartItems = document.getElementById('cartItems');
        const cartSummary = document.getElementById('cartSummary');

        if (cart.length === 0) {
            cartItems.innerHTML = '<div class="empty-cart">Cart is empty</div>';
            cartSummary.style.display = 'none';
            return;
        }

        cartSummary.style.display = 'block';

        cartItems.innerHTML = cart.map(item => `
                <div class="cart-item">
                    <div class="cart-item-details">
                        <div class="cart-item-name">${item.itemName}</div>
                        <div class="cart-item-price">$${item.unitPrice.toFixed(2)} each</div>
                    </div>
                    <div class="cart-item-qty">
                        <button class="qty-btn" onclick="updateQuantity('${item.itemCode}', -1)">-</button>
                        <span>${item.quantity}</span>
                        <button class="qty-btn" onclick="updateQuantity('${item.itemCode}', 1)">+</button>
                        <button class="remove-btn" onclick="removeFromCart('${item.itemCode}')">×</button>
                    </div>
                </div>
            `).join('');

        updateTotals();
    }

    function updateTotals() {
        const subtotal = cart.reduce((sum, item) => sum + (item.unitPrice * item.quantity), 0);

        document.getElementById('subtotal').textContent = '$' + subtotal.toFixed(2);
        document.getElementById('total').textContent = '$' + subtotal.toFixed(2);
    }

    document.getElementById('cashReceived').addEventListener('input', function() {
        const total = cart.reduce((sum, item) => sum + (item.unitPrice * item.quantity), 0);
        const cash = parseFloat(this.value) || 0;
        const change = cash - total;

        const changeRow = document.getElementById('changeRow');
        const checkoutBtn = document.getElementById('checkoutBtn');

        if (cash >= total && cart.length > 0) {
            changeRow.style.display = 'flex';
            document.getElementById('change').textContent = '$' + change.toFixed(2);
            checkoutBtn.disabled = false;
        } else {
            changeRow.style.display = 'none';
            checkoutBtn.disabled = true;
        }
    });

    function checkout() {
        const total = cart.reduce((sum, item) => sum + (item.unitPrice * item.quantity), 0);
        const cash = parseFloat(document.getElementById('cashReceived').value) || 0;

        if (cash < total) {
            alert('Insufficient payment!');
            return;
        }

        const bill = {
            items: cart.map(item => ({
                itemCode: item.itemCode,
                itemName: item.itemName,
                quantity: item.quantity,
                unitPrice: item.unitPrice,
                subtotal: item.quantity * item.unitPrice
            })),
            totalAmount: total,
            cashReceived: cash,
            changeAmount: cash - total
        };

        fetch('billing', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(bill)
        })
            .then(res => res.json())
            .then(result => {
                if (result.success) {
                    alert('Sale completed! Bill Number: ' + result.billNumber);
                    cart = [];
                    renderCart();
                    document.getElementById('cashReceived').value = '';
                    loadProducts();
                } else {
                    alert('Failed to process sale: ' + result.message);
                }
            });
    }

    loadProducts();
</script>
</body>
</html>