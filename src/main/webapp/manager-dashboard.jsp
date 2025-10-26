<%@ page import="com.syos.web.model.User" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    User user = (User) session.getAttribute("user");
    if (user == null || !user.isManager()) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manager Dashboard - SYOS POS</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: #f5f5f5;
        }

        .header {
            background: linear-gradient(135deg, #43a047 0%, #1b5e20 100%);
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

        .blue { background: #e3f2fd; color: #1976d2; }
        .green { background: #e8f5e9; color: #388e3c; }
        .orange { background: #fff3e0; color: #f57c00; }
        .purple { background: #f3e5f5; color: #7b1fa2; }
        .red { background: #ffebee; color: #c62828; }
        .teal { background: #e0f2f1; color: #00796b; }
    </style>
</head>
<body>
<div class="header">
    <div class="header-content">
        <h1>SYOS POS - Manager Dashboard</h1>
        <div class="user-info">
            <span>Welcome, <%= user.getFullName() != null ? user.getFullName() : user.getUsername() %></span>
            <a href="<%= request.getContextPath() %>/logout" class="btn-logout">Logout</a>
        </div>
    </div>
</div>

<div class="container">
    <div class="welcome">
        <h2>Manager Control Panel</h2>
        <p>Manage products, process sales, generate reports, and oversee operations.</p>
    </div>

    <div class="dashboard-grid">
        <a href="<%= request.getContextPath() %>/admin/products?action=list" class="dashboard-card">
            <div class="card-icon blue">📦</div>
            <div class="card-title">Product Management</div>
            <div class="card-description">Add, edit, delete products and manage stock.</div>
        </a>

        <a href="<%= request.getContextPath() %>/sales?action=create" class="dashboard-card">
            <div class="card-icon green">💰</div>
            <div class="card-title">Create Sale</div>
            <div class="card-description">Process new sales transactions.</div>
        </a>

        <a href="<%= request.getContextPath() %>/sales?action=list" class="dashboard-card">
            <div class="card-icon orange">📊</div>
            <div class="card-title">View Sales</div>
            <div class="card-description">Browse all sales transactions.</div>
        </a>

        <a href="<%= request.getContextPath() %>/reports?type=daily-sales" class="dashboard-card">
            <div class="card-icon purple">📈</div>
            <div class="card-title">Sales Reports</div>
            <div class="card-description">Generate daily sales reports.</div>
        </a>

        <a href="<%= request.getContextPath() %>/reports?type=stock" class="dashboard-card">
            <div class="card-icon teal">📋</div>
            <div class="card-title">Stock Report</div>
            <div class="card-description">View inventory levels.</div>
        </a>

        <a href="<%= request.getContextPath() %>/reports?type=reorder" class="dashboard-card">
            <div class="card-icon red">⚠️</div>
            <div class="card-title">Reorder Report</div>
            <div class="card-description">Products needing reorder.</div>
        </a>

        <a href="<%= request.getContextPath() %>/admin/users?action=list" class="dashboard-card">
            <div class="card-icon purple">👥</div>
            <div class="card-title">User Management</div>
            <div class="card-description">Manage cashiers and customers.</div>
        </a>
    </div>
</div>
</body>
</html>
