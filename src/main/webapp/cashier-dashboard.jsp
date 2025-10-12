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
    <title>Cashier Dashboard - SYOS POS</title>
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
        .container {
            max-width: 1200px;
            margin: 30px auto;
            padding: 0 20px;
        }
        .dashboard-cards {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 20px;
        }
        .card {
            background: white;
            padding: 40px;
            border-radius: 10px;
            box-shadow: 0 2px 5px rgba(0,0,0,0.1);
            text-align: center;
            cursor: pointer;
            transition: transform 0.3s;
        }
        .card:hover {
            transform: translateY(-5px);
            box-shadow: 0 5px 15px rgba(0,0,0,0.2);
        }
        .card h2 {
            color: #667eea;
            font-size: 24px;
            margin-bottom: 15px;
        }
        .card p {
            color: #666;
        }
    </style>
</head>
<body>
<div class="navbar">
    <h1>🛒 SYOS POS - Cashier</h1>
    <div class="user-info">
        <span>Welcome, <%= user.getFullName() %></span>
        <a href="logout">Logout</a>
    </div>
</div>

<div class="container">
    <div class="dashboard-cards">
        <div class="card" onclick="location.href='billing.jsp'">
            <h2>💳 New Sale</h2>
            <p>Process customer purchases and generate bills</p>
        </div>

        <div class="card">
            <h2>📋 View Products</h2>
            <p>Browse available products and prices</p>
        </div>
    </div>
</div>
</body>
</html>