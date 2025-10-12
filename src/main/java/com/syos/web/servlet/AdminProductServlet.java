package com.syos.web.servlet;

import com.google.gson.Gson;
import com.syos.web.dao.ProductDAO;
import com.syos.web.model.Product;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Servlet for managing products in the admin panel
 * Handles CRUD operations for products
 */
@WebServlet(name = "AdminProductServlet", urlPatterns = {"/admin/products", "/admin-products"})
public class AdminProductServlet extends HttpServlet {

    private ProductDAO productDAO;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        super.init();
        productDAO = new ProductDAO();
        gson = new Gson();
        System.out.println("==========================================");
        System.out.println("AdminProductServlet INITIALIZED");
        System.out.println("Servlet Name: " + getServletName());
        System.out.println("==========================================");
    }

    /**
     * Check if user is authenticated and has admin role
     */
    private boolean isAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            System.out.println("AdminProductServlet: No session or user - Unauthorized");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Not authenticated\"}");
            return false;
        }

        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            System.out.println("AdminProductServlet: User is not admin, role=" + role);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Admin access required\"}");
            return false;
        }

        return true;
    }

    /**
     * GET - Retrieve all products or single product by code
     *
     * Examples:
     * - GET /admin-products -> returns all products
     * - GET /admin-products?code=MILK001 -> returns single product
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("==========================================");
        System.out.println("AdminProductServlet doGet called");
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Context Path: " + request.getContextPath());
        System.out.println("Servlet Path: " + request.getServletPath());
        System.out.println("==========================================");

        // Check admin access
        if (!isAdmin(request, response)) {
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // Get product code parameter (also check 'id' for backwards compatibility)
            String itemCode = request.getParameter("code");
            if (itemCode == null || itemCode.isEmpty()) {
                itemCode = request.getParameter("id");
            }

            if (itemCode != null && !itemCode.isEmpty()) {
                // Get single product by code
                System.out.println("Fetching product with code: " + itemCode);
                Product product = productDAO.getProductByCode(itemCode);

                if (product != null) {
                    String json = gson.toJson(product);
                    out.write(json);
                    System.out.println("Product found and returned");
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.write("{\"error\": \"Product not found\"}");
                    System.out.println("Product not found with code: " + itemCode);
                }
            } else {
                // Get all products
                System.out.println("Fetching all products");
                List<Product> products = productDAO.getAllProducts();
                String json = gson.toJson(products);
                out.write(json);
                System.out.println("Returning " + products.size() + " products");
            }

        } catch (Exception e) {
            System.err.println("Error in AdminProductServlet doGet: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * POST - Create new product
     *
     * Expected JSON body:
     * {
     *   "itemCode": "PROD001",
     *   "name": "Product Name",
     *   "category": "Category",
     *   "price": 10.99,
     *   "quantityInStore": 100,
     *   "quantityOnShelf": 50,
     *   "reorderLevel": 50,
     *   "state": "ON_SHELF",
     *   "expiryDate": "2025-12-31"
     * }
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("==========================================");
        System.out.println("AdminProductServlet doPost called");
        System.out.println("==========================================");

        // Check admin access
        if (!isAdmin(request, response)) {
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // Read JSON from request body
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            String json = sb.toString();
            System.out.println("Received JSON: " + json);

            // Parse JSON to Product object
            Product product = gson.fromJson(json, Product.class);

            // Validate product code
            if (product.getItemCode() == null || product.getItemCode().trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\": \"Product code is required\"}");
                System.out.println("Validation failed: Product code is required");
                return;
            }

            // Validate product name
            if (product.getName() == null || product.getName().trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\": \"Product name is required\"}");
                System.out.println("Validation failed: Product name is required");
                return;
            }

            // Validate price
            if (product.getPrice() <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\": \"Product price must be greater than 0\"}");
                System.out.println("Validation failed: Invalid price");
                return;
            }

            // Validate quantities
            if (product.getQuantityInStore() < 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\": \"Quantity in store cannot be negative\"}");
                System.out.println("Validation failed: Invalid quantity in store");
                return;
            }

            if (product.getQuantityOnShelf() < 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\": \"Quantity on shelf cannot be negative\"}");
                System.out.println("Validation failed: Invalid quantity on shelf");
                return;
            }

            // Set default values if not provided
            if (product.getCategory() == null || product.getCategory().isEmpty()) {
                product.setCategory("General");
            }

            if (product.getState() == null || product.getState().isEmpty()) {
                // Set state based on shelf quantity
                product.setState(product.getQuantityOnShelf() > 0 ? "ON_SHELF" : "IN_STORE");
            }

            if (product.getReorderLevel() == 0) {
                product.setReorderLevel(50);
            }

            // Check if product code already exists
            Product existing = productDAO.getProductByCode(product.getItemCode());
            if (existing != null) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                out.write("{\"error\": \"Product with code '" + product.getItemCode() + "' already exists\"}");
                System.out.println("Product already exists with code: " + product.getItemCode());
                return;
            }

            // Add product to database
            boolean success = productDAO.addProduct(product);

            if (success) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.write("{\"message\": \"Product created successfully\", \"product\": " + gson.toJson(product) + "}");
                System.out.println("Product created successfully: " + product.getItemCode());
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.write("{\"error\": \"Failed to create product\"}");
                System.out.println("Failed to create product in database");
            }

        } catch (Exception e) {
            System.err.println("Error in AdminProductServlet doPost: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * PUT - Update existing product
     *
     * Expected JSON body:
     * {
     *   "itemCode": "PROD001",  // Required - identifies which product to update
     *   "name": "Updated Name",
     *   "category": "Category",
     *   "price": 12.99,
     *   "quantityInStore": 150,
     *   "quantityOnShelf": 75,
     *   "reorderLevel": 50,
     *   "state": "ON_SHELF",
     *   "expiryDate": "2025-12-31"
     * }
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("==========================================");
        System.out.println("AdminProductServlet doPut called");
        System.out.println("==========================================");

        // Check admin access
        if (!isAdmin(request, response)) {
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // Read JSON from request body
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            String json = sb.toString();
            System.out.println("Received JSON for update: " + json);

            // Parse JSON to Product object
            Product product = gson.fromJson(json, Product.class);

            // Validate product code
            if (product.getItemCode() == null || product.getItemCode().trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\": \"Product code is required\"}");
                System.out.println("Validation failed: Product code is required");
                return;
            }

            // Check if product exists
            Product existing = productDAO.getProductByCode(product.getItemCode());
            if (existing == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write("{\"error\": \"Product with code '" + product.getItemCode() + "' not found\"}");
                System.out.println("Product not found with code: " + product.getItemCode());
                return;
            }

            // Validate product name
            if (product.getName() == null || product.getName().trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\": \"Product name is required\"}");
                System.out.println("Validation failed: Product name is required");
                return;
            }

            // Validate price
            if (product.getPrice() <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\": \"Product price must be greater than 0\"}");
                System.out.println("Validation failed: Invalid price");
                return;
            }

            // Validate quantities
            if (product.getQuantityInStore() < 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\": \"Quantity in store cannot be negative\"}");
                System.out.println("Validation failed: Invalid quantity in store");
                return;
            }

            if (product.getQuantityOnShelf() < 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\": \"Quantity on shelf cannot be negative\"}");
                System.out.println("Validation failed: Invalid quantity on shelf");
                return;
            }

            // Update product in database
            boolean success = productDAO.updateProduct(product);

            if (success) {
                out.write("{\"message\": \"Product updated successfully\", \"product\": " + gson.toJson(product) + "}");
                System.out.println("Product updated successfully: " + product.getItemCode());
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.write("{\"error\": \"Failed to update product\"}");
                System.out.println("Failed to update product in database");
            }

        } catch (Exception e) {
            System.err.println("Error in AdminProductServlet doPut: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * DELETE - Delete product by code
     *
     * Examples:
     * - DELETE /admin-products?code=MILK001
     * - DELETE /admin-products?id=MILK001 (backwards compatibility)
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("==========================================");
        System.out.println("AdminProductServlet doDelete called");
        System.out.println("==========================================");

        // Check admin access
        if (!isAdmin(request, response)) {
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // Get product code parameter (also check 'id' for backwards compatibility)
            String itemCode = request.getParameter("code");
            if (itemCode == null || itemCode.isEmpty()) {
                itemCode = request.getParameter("id");
            }

            if (itemCode == null || itemCode.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\": \"Product code is required\"}");
                System.out.println("Validation failed: Product code is required");
                return;
            }

            System.out.println("Attempting to delete product: " + itemCode);

            // Check if product exists
            Product product = productDAO.getProductByCode(itemCode);
            if (product == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write("{\"error\": \"Product not found\"}");
                System.out.println("Product not found with code: " + itemCode);
                return;
            }

            // Delete product
            boolean success = productDAO.deleteProduct(itemCode);

            if (success) {
                out.write("{\"message\": \"Product deleted successfully\"}");
                System.out.println("Product deleted successfully: " + itemCode);
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.write("{\"error\": \"Failed to delete product\"}");
                System.out.println("Failed to delete product from database");
            }

        } catch (Exception e) {
            System.err.println("Error in AdminProductServlet doDelete: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        System.out.println("==========================================");
        System.out.println("AdminProductServlet DESTROYED");
        System.out.println("==========================================");
    }
}