package com.syos.web.servlet;

import com.syos.web.model.Product;
import com.syos.web.service.ConcurrentInventoryService;
import com.syos.web.exception.ConcurrencyException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;

public class AdminProductServlet extends HttpServlet {

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
                case "add":
                    showAddForm(request, response);
                    break;
                case "edit":
                    showEditForm(request, response);
                    break;
                case "delete":
                    deleteProduct(request, response);
                    break;
                default:
                    listProducts(request, response);
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
                addProduct(request, response);
            } else if ("edit".equals(action)) {
                updateProduct(request, response);
            } else if ("updateStock".equals(action)) {
                updateStock(request, response);
            }
        } catch (SQLException | ConcurrencyException e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    private void listProducts(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        var products = inventoryService.getAllProducts();
        request.setAttribute("products", products);
        request.getRequestDispatcher("/admin/products-list.jsp").forward(request, response);
    }

    private void showAddForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/admin/product-form.jsp").forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        String code = request.getParameter("code");
        Product product = inventoryService.getProduct(code);

        if (product == null) {
            request.setAttribute("error", "Product not found");
            listProducts(request, response);
            return;
        }

        request.setAttribute("product", product);
        request.getRequestDispatcher("/admin/product-form.jsp").forward(request, response);
    }

    private void addProduct(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        Product product = new Product();
        product.setCode(request.getParameter("code"));
        product.setName(request.getParameter("name"));
        product.setCategory(request.getParameter("category"));
        product.setPrice(Double.parseDouble(request.getParameter("price")));
        product.setQuantityInStore(Integer.parseInt(request.getParameter("quantityInStore")));
        product.setQuantityOnShelf(Integer.parseInt(request.getParameter("quantityOnShelf")));
        product.setReorderLevel(Integer.parseInt(request.getParameter("reorderLevel")));
        product.setState("AVAILABLE");

        String purchaseDate = request.getParameter("purchaseDate");
        if (purchaseDate != null && !purchaseDate.isEmpty()) {
            product.setPurchaseDate(Date.valueOf(purchaseDate));
        }

        String expiryDate = request.getParameter("expiryDate");
        if (expiryDate != null && !expiryDate.isEmpty()) {
            product.setExpiryDate(Date.valueOf(expiryDate));
        }

        boolean success = inventoryService.addProduct(product);

        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin/products?message=Product added successfully");
        } else {
            request.setAttribute("error", "Failed to add product");
            request.setAttribute("product", product);
            request.getRequestDispatcher("/admin/product-form.jsp").forward(request, response);
        }
    }

    private void updateProduct(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException, ConcurrencyException {

        Product product = new Product();
        product.setCode(request.getParameter("code"));
        product.setName(request.getParameter("name"));
        product.setCategory(request.getParameter("category"));
        product.setPrice(Double.parseDouble(request.getParameter("price")));
        product.setQuantityInStore(Integer.parseInt(request.getParameter("quantityInStore")));
        product.setQuantityOnShelf(Integer.parseInt(request.getParameter("quantityOnShelf")));
        product.setReorderLevel(Integer.parseInt(request.getParameter("reorderLevel")));
        product.setState(request.getParameter("state"));
        product.setVersion(Integer.parseInt(request.getParameter("version")));

        String purchaseDate = request.getParameter("purchaseDate");
        if (purchaseDate != null && !purchaseDate.isEmpty()) {
            product.setPurchaseDate(Date.valueOf(purchaseDate));
        }

        String expiryDate = request.getParameter("expiryDate");
        if (expiryDate != null && !expiryDate.isEmpty()) {
            product.setExpiryDate(Date.valueOf(expiryDate));
        }

        try {
            boolean success = inventoryService.updateProduct(product);

            if (success) {
                response.sendRedirect(request.getContextPath() + "/admin/products?message=Product updated successfully");
            } else {
                request.setAttribute("error", "Failed to update product");
                request.setAttribute("product", product);
                request.getRequestDispatcher("/admin/product-form.jsp").forward(request, response);
            }
        } catch (ConcurrencyException e) {
            request.setAttribute("error", "Concurrency conflict: " + e.getMessage());
            Product currentProduct = inventoryService.getProduct(product.getCode());
            request.setAttribute("product", currentProduct);
            request.getRequestDispatcher("/admin/product-form.jsp").forward(request, response);
        }
    }

    private void updateStock(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        String productCode = request.getParameter("code");
        int quantityChange = Integer.parseInt(request.getParameter("quantity"));
        String changeType = request.getParameter("changeType");

        boolean success = inventoryService.updateStock(productCode, quantityChange, changeType);

        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin/products?message=Stock updated successfully");
        } else {
            request.setAttribute("error", "Failed to update stock");
            response.sendRedirect(request.getContextPath() + "/admin/products");
        }
    }

    private void deleteProduct(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        String code = request.getParameter("code");
        boolean success = inventoryService.deleteProduct(code);

        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin/products?message=Product deleted successfully");
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/products?error=Failed to delete product");
        }
    }
}