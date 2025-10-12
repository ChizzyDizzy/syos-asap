<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.syos.web.model.User" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null || !"ADMIN".equals(user.getRole())) {
        response.sendRedirect("login.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <title>Admin Dashboard - SYOS POS</title>
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
        .navbar .user-info {
            display: flex;
            gap: 20px;
            align-items: center;
        }
        .navbar a {
            color: white;
            text-decoration: none;
            padding: 8px 15px;
            background: #555;
            border-radius: 5px;
        }
        .navbar a:hover { background: #666; }
        .container {
            max-width: 1200px;
            margin: 30px auto;
            padding: 0 20px;
        }
        .dashboard-cards {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        .card {
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 2px 5px rgba(0,0,0,0.1);
            text-align: center;
            cursor: pointer;
            transition: transform 0.3s, box-shadow 0.3s;
        }
        .card:hover {
            transform: translateY(-5px);
            box-shadow: 0 5px 15px rgba(0,0,0,0.2);
        }
        .card h2 {
            color: #667eea;
            margin-bottom: 10px;
            font-size: 20px;
        }
        .card p {
            color: #666;
            font-size: 14px;
        }
    </style>
</head>
<body>
<div class="navbar">
    <h1>🛒 SYOS POS - Admin Dashboard</h1>
    <div class="user-info">
        <span>Welcome, <%= user.getFullName() %></span>
        <a href="logout">Logout</a>
    </div>
</div>

<div class="container">
    <div class="dashboard-cards">
        <div class="card" onclick="location.href='admin-products.jsp'">
            <h2>📦 Products</h2>
            <p>Manage products, add new items, update prices</p>
        </div>

        <div class="card" onclick="location.href='admin-products.jsp'">
            <h2>📊 Stock Management</h2>
            <p>View stock levels, update inventory</p>
        </div>

        <div class="card" onclick="location.href='billing.jsp'">
            <h2>💳 Billing</h2>
            <p>Process sales and generate bills</p>
        </div>

        <div class="card">
            <h2>📈 Reports</h2>
            <p>View sales reports and analytics</p>
        </div>
    </div>
</div>
</body>
</html>