<%@ page import="com.syos.web.model.User" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    User user = (User) session.getAttribute("user");
    if (user == null || !user.isCashier()) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cashier Dashboard - SYOS POS</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: #f5f5f5;
        }

        .header {
            background: linear-gradient(135deg, #ff6f00 0%, #e65100 100%);
            color: white;
            padding: 20px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }

        .header-content {
            max-width: 1200px;
            margin: 0 auto;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .header h1 { font-size: 24px; }

        .user-info {
            display: flex;
            align-items: center;
            gap: 15px;
        }

        .user-info span { font-size: 14px; }

        .btn-logout {
            background: rgba(255,255,255,0.2);
            color: white;
            padding: 8px 16px;
            border: 1px solid white;
            border-radius: 5px;
            text-decoration: none;
            font-size: 14px;
        }

        .btn-logout:hover { background: rgba(255,255,255,0.3); }

        .container {
            max-width: 1200px;
            margin: 30px auto;
            padding: 0 20px;
        }

        .welcome {
            background: white;
            padding: 30px;
            border-radius: 10px;
            margin-bottom: 30px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }

        .welcome h2 { color: #333; margin-bottom: 10px; }
        .welcome p { color: #666; }

        .dashboard-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
            gap: 20px;
        }

        .dashboard-card {
            background: white;
            padding: 25px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            transition: transform 0.2s;
            text-decoration: none;
            color: inherit;
            display: block;
        }

        .dashboard-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 5px 20px rgba(0,0,0,0.15);
        }

        .card-icon {
            width: 50px;
            height: 50px;
            border-radius: 10px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 24px;
            margin-bottom: 15px;
        }

        .card-title {
            font-size: 18px;
            font-weight: 600;
            color: #333;
            margin-bottom: 10px;
        }

        .card-description {
            font-size: 14px;
            color: #666;
        }

        .green { background: #e8f5e9; color: #388e3c; }
        .orange { background: #fff3e0; color: #f57c00; }
        .blue { background: #e3f2fd; color: #1976d2; }
    </style>
</head>
<body>
<div class="header">
    <div class="header-content">
        <h1>SYOS POS - Cashier Dashboard</h1>
        <div class="user-info">
            <span>Welcome, <%= user.getFullName() != null ? user.getFullName() : user.getUsername() %></span>
            <a href="<%= request.getContextPath() %>/logout" class="btn-logout">Logout</a>
        </div>
    </div>
</div>

<div class="container">
    <div class="welcome">
        <h2>Cashier Point of Sale</h2>
        <p>Process sales transactions and view product information.</p>
    </div>

    <div class="dashboard-grid">
        <a href="<%= request.getContextPath() %>/sales?action=create" class="dashboard-card">
            <div class="card-icon green">💰</div>
            <div class="card-title">Create Sale</div>
            <div class="card-description">Process new sales transactions with automatic stock updates.</div>
        </a>

        <a href="<%= request.getContextPath() %>/sales?action=list" class="dashboard-card">
            <div class="card-icon orange">📊</div>
            <div class="card-title">My Sales</div>
            <div class="card-description">View your sales transactions history.</div>
        </a>

        <a href="<%= request.getContextPath() %>/products?action=list" class="dashboard-card">
            <div class="card-icon blue">🔍</div>
            <div class="card-title">Browse Products</div>
            <div class="card-description">Search and view product information.</div>
        </a>
    </div>
</div>
</body>
</html>
