package com.syos.web.servlet;

import com.syos.web.model.Product;
import com.syos.web.service.ConcurrentInventoryService;
import com.syos.web.exception.ConcurrencyException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ProductServlet extends HttpServlet {

    private ConcurrentInventoryService inventoryService;

    @Override
    public void init() throws ServletException {
        inventoryService = ConcurrentInventoryService.getInstance();
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
                    listProducts(request, response);
                    break;
                case "view":
                    viewProduct(request, response);
                    break;
                case "category":
                    listProductsByCategory(request, response);
                    break;
                default:
                    listProducts(request, response);
                    break;
            }
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }
    }

    private void listProducts(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        List<Product> products = inventoryService.getAllProducts();
        request.setAttribute("products", products);
        request.getRequestDispatcher("/products-list.jsp").forward(request, response);
    }

    private void viewProduct(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        String code = request.getParameter("code");

        if (code == null || code.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/products?action=list");
            return;
        }

        Product product = inventoryService.getProduct(code);

        if (product == null) {
            request.setAttribute("error", "Product not found");
            response.sendRedirect(request.getContextPath() + "/products?action=list");
            return;
        }

        request.setAttribute("product", product);
        request.getRequestDispatcher("/product-view.jsp").forward(request, response);
    }

    private void listProductsByCategory(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        String category = request.getParameter("category");

        if (category == null || category.trim().isEmpty()) {
            listProducts(request, response);
            return;
        }

        List<Product> products = inventoryService.getProductsByCategory(category);
        request.setAttribute("products", products);
        request.setAttribute("category", category);
        request.getRequestDispatcher("/products-list.jsp").forward(request, response);
    }
}