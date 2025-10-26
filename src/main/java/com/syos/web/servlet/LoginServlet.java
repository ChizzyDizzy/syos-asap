package com.syos.web.servlet;

import com.syos.web.dao.UserDAO;
import com.syos.web.model.User;
import com.syos.pos.model.Role;
import com.syos.web.util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

public class LoginServlet extends HttpServlet {

    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            request.setAttribute("error", "Username and password are required");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        try {
            User user = userDAO.getUserByUsername(username);

            if (user == null) {
                request.setAttribute("error", "Invalid username or password");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
                return;
            }

            if (!user.isActive()) {
                request.setAttribute("error", "Your account has been deactivated");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
                return;
            }

            if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
                request.setAttribute("error", "Invalid username or password");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
                return;
            }

            userDAO.updateLastLogin(user.getId());

            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole());

            String redirectUrl = getRedirectUrlForRole(user.getRole());
            response.sendRedirect(request.getContextPath() + redirectUrl);

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "Database error: " + e.getMessage());
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }

    private String getRedirectUrlForRole(Role role) {
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