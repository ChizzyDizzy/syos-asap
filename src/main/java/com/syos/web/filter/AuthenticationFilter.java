package com.syos.web.filter;

import com.syos.web.model.User;
import com.syos.pos.model.Role;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AuthenticationFilter implements Filter {

    private static final List<String> PUBLIC_URLS = Arrays.asList(
            "/login.jsp",
            "/login",
            "/logout",
            "/css/",
            "/js/",
            "/images/"
    );

    private static final List<String> ADMIN_ONLY_URLS = Arrays.asList(
            "/admin/",
            "/users/create",
            "/users/delete",
            "/users/manage"
    );

    private static final List<String> ADMIN_MANAGER_URLS = Arrays.asList(
            "/products/add",
            "/products/edit",
            "/products/delete",
            "/reports/"
    );

    private static final List<String> STAFF_ONLY_URLS = Arrays.asList(
            "/sales/create",
            "/bills/create"
    );

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = requestURI.substring(contextPath.length());

        if (isPublicResource(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            httpResponse.sendRedirect(contextPath + "/login.jsp");
            return;
        }

        if (!hasAccess(user, path)) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    private boolean isPublicResource(String path) {
        for (String publicUrl : PUBLIC_URLS) {
            if (path.startsWith(publicUrl)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAccess(User user, String path) {
        Role role = user.getRole();

        if (role == Role.ADMIN) {
            return true;
        }

        for (String adminUrl : ADMIN_ONLY_URLS) {
            if (path.startsWith(adminUrl)) {
                return false;
            }
        }

        if (role == Role.MANAGER) {
            for (String managerUrl : ADMIN_MANAGER_URLS) {
                if (path.startsWith(managerUrl)) {
                    return true;
                }
            }
            for (String staffUrl : STAFF_ONLY_URLS) {
                if (path.startsWith(staffUrl)) {
                    return true;
                }
            }
            return !path.startsWith("/customer/");
        }

        if (role == Role.CASHIER) {
            for (String staffUrl : STAFF_ONLY_URLS) {
                if (path.startsWith(staffUrl)) {
                    return true;
                }
            }
            return path.startsWith("/products/view") || path.startsWith("/dashboard");
        }

        if (role == Role.CUSTOMER) {
            return path.startsWith("/products/view") ||
                    path.startsWith("/customer/") ||
                    path.equals("/dashboard");
        }

        return false;
    }
}