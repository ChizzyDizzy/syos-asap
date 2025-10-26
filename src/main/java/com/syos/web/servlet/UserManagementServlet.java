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
import java.util.List;

public class UserManagementServlet extends HttpServlet {

    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if (action == null) {
            action = "list";
        }

        try {
            switch (action) {
                case "list":
                    listUsers(request, response);
                    break;
                case "add":
                    showAddForm(request, response);
                    break;
                case "edit":
                    showEditForm(request, response);
                    break;
                case "delete":
                    deleteUser(request, response);
                    break;
                default:
                    listUsers(request, response);
                    break;
            }
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        try {
            if ("add".equals(action)) {
                addUser(request, response);
            } else if ("edit".equals(action)) {
                updateUser(request, response);
            }
        } catch (SQLException e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    private void listUsers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        List<User> users = userDAO.getAllUsers();
        request.setAttribute("users", users);
        request.getRequestDispatcher("/admin/users-list.jsp").forward(request, response);
    }

    private void showAddForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");

        request.setAttribute("currentUser", currentUser);
        request.getRequestDispatcher("/admin/user-form.jsp").forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        long userId = Long.parseLong(request.getParameter("id"));
        User user = userDAO.getUserById(userId);

        if (user == null) {
            request.setAttribute("error", "User not found");
            listUsers(request, response);
            return;
        }

        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");

        request.setAttribute("user", user);
        request.setAttribute("currentUser", currentUser);
        request.getRequestDispatcher("/admin/user-form.jsp").forward(request, response);
    }

    private void addUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");

        String roleStr = request.getParameter("role");
        Role role = Role.valueOf(roleStr);

        if (role == Role.ADMIN && !currentUser.isAdmin()) {
            request.setAttribute("error", "Only admins can create admin users");
            showAddForm(request, response);
            return;
        }

        User user = new User();
        user.setUserId(request.getParameter("userId"));
        user.setUsername(request.getParameter("username"));
        user.setEmail(request.getParameter("email"));
        user.setFullName(request.getParameter("fullName"));
        user.setRole(role);
        user.setActive(true);

        String password = request.getParameter("password");
        user.setPasswordHash(PasswordUtil.hashPassword(password));

        boolean success = userDAO.createUser(user);

        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin/users?message=User created successfully");
        } else {
            request.setAttribute("error", "Failed to create user");
            request.setAttribute("user", user);
            request.getRequestDispatcher("/admin/user-form.jsp").forward(request, response);
        }
    }

    private void updateUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");

        long userId = Long.parseLong(request.getParameter("id"));
        User user = userDAO.getUserById(userId);

        if (user == null) {
            request.setAttribute("error", "User not found");
            listUsers(request, response);
            return;
        }

        String roleStr = request.getParameter("role");
        Role newRole = Role.valueOf(roleStr);

        if (newRole == Role.ADMIN && !currentUser.isAdmin()) {
            request.setAttribute("error", "Only admins can modify admin users");
            showEditForm(request, response);
            return;
        }

        user.setEmail(request.getParameter("email"));
        user.setFullName(request.getParameter("fullName"));
        user.setRole(newRole);
        user.setActive(Boolean.parseBoolean(request.getParameter("isActive")));

        boolean success = userDAO.updateUser(user);

        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin/users?message=User updated successfully");
        } else {
            request.setAttribute("error", "Failed to update user");
            request.setAttribute("user", user);
            request.getRequestDispatcher("/admin/user-form.jsp").forward(request, response);
        }
    }

    private void deleteUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("user");

        long userId = Long.parseLong(request.getParameter("id"));

        if (userId == currentUser.getId()) {
            response.sendRedirect(request.getContextPath() + "/admin/users?error=Cannot delete your own account");
            return;
        }

        User userToDelete = userDAO.getUserById(userId);

        if (userToDelete != null && userToDelete.getRole() == Role.ADMIN && !currentUser.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/admin/users?error=Only admins can delete admin users");
            return;
        }

        boolean success = userDAO.deleteUser(userId);

        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin/users?message=User deleted successfully");
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/users?error=Failed to delete user");
        }
    }
}