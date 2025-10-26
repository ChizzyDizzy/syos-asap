package com.syos.web.servlet;

import com.syos.web.model.User;
import com.syos.pos.model.Role;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class DashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String dashboardPage = getDashboardPageForRole(user.getRole());
        request.getRequestDispatcher(dashboardPage).forward(request, response);
    }

    private String getDashboardPageForRole(Role role) {
        switch (role) {
            case ADMIN:
                return "/admin-dashboard.jsp";
            case MANAGER:
                return "/manager-dashboard.jsp";
            case CASHIER:
                return "/cashier-dashboard.jsp";
            case CUSTOMER:
                return "/customer-dashboard.jsp";
            default:
                return "/login.jsp";
        }
    }
}