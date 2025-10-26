package com.syos.web.servlet;

import com.syos.web.model.Sale;
import com.syos.web.model.SaleItem;
import com.syos.web.model.User;
import com.syos.web.service.ConcurrentSalesService;
import com.syos.web.service.ConcurrentInventoryService;
import com.syos.web.exception.InsufficientStockException;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class SalesServlet extends HttpServlet {

    private ConcurrentSalesService salesService;
    private ConcurrentInventoryService inventoryService;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        salesService = ConcurrentSalesService.getInstance();
        inventoryService = ConcurrentInventoryService.getInstance();
        gson = new Gson();
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
                    listSales(request, response);
                    break;
                case "view":
                    viewSale(request, response);
                    break;
                case "create":
                    showCreateForm(request, response);
                    break;
                case "cancel":
                    cancelSale(request, response);
                    break;
                default:
                    listSales(request, response);
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
            if ("create".equals(action)) {
                createSale(request, response);
            }
        } catch (SQLException | InsufficientStockException e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    private void listSales(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        List<Sale> sales;

        if (user.isCashier()) {
            Date today = new Date();
            Date tomorrow = new Date(today.getTime() + 24 * 60 * 60 * 1000);
            sales = salesService.getSalesByCashier(user.getId(), today, tomorrow);
        } else {
            Date today = new Date();
            sales = salesService.getSalesByDate(today);
        }

        request.setAttribute("sales", sales);
        request.getRequestDispatcher("/sales-list.jsp").forward(request, response);
    }

    private void viewSale(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        long saleId = Long.parseLong(request.getParameter("id"));
        Map<String, Object> saleData = salesService.getSaleWithItems(saleId);

        if (saleData == null) {
            request.setAttribute("error", "Sale not found");
            listSales(request, response);
            return;
        }

        request.setAttribute("sale", saleData.get("sale"));
        request.setAttribute("items", saleData.get("items"));
        request.getRequestDispatcher("/sale-view.jsp").forward(request, response);
    }

    private void showCreateForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        var products = inventoryService.getAllProducts();
        request.setAttribute("products", products);
        request.getRequestDispatcher("/create-sale.jsp").forward(request, response);
    }

    private void createSale(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException, InsufficientStockException {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        String itemsJson = request.getParameter("items");
        List<Map<String, Object>> itemsData = gson.fromJson(itemsJson, List.class);

        Sale sale = new Sale();
        sale.setTotalAmount(Double.parseDouble(request.getParameter("totalAmount")));
        sale.setDiscount(Double.parseDouble(request.getParameter("discount")));
        sale.setTaxAmount(Double.parseDouble(request.getParameter("taxAmount")));
        sale.setPaymentMethod(request.getParameter("paymentMethod"));
        sale.setCashTendered(Double.parseDouble(request.getParameter("cashTendered")));
        sale.setChangeAmount(Double.parseDouble(request.getParameter("changeAmount")));

        List<SaleItem> saleItems = new ArrayList<>();
        for (Map<String, Object> itemData : itemsData) {
            SaleItem item = new SaleItem();
            item.setItemCode((String) itemData.get("code"));
            item.setItemName((String) itemData.get("name"));
            item.setQuantity(((Double) itemData.get("quantity")).intValue());
            item.setUnitPrice((Double) itemData.get("price"));
            item.setSubtotal((Double) itemData.get("subtotal"));
            saleItems.add(item);
        }

        Sale createdSale = salesService.createSale(sale, saleItems, user.getId());

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(createdSale));
    }

    private void cancelSale(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        long saleId = Long.parseLong(request.getParameter("id"));
        boolean success = salesService.cancelSale(saleId, user.getId());

        if (success) {
            response.sendRedirect(request.getContextPath() + "/sales?message=Sale cancelled successfully");
        } else {
            response.sendRedirect(request.getContextPath() + "/sales?error=Failed to cancel sale");
        }
    }
}