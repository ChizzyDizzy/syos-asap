package com.syos.web.servlet;

import com.syos.web.service.ConcurrentSalesService;
import com.syos.web.service.ConcurrentInventoryService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class ReportServlet extends HttpServlet {

    private ConcurrentSalesService salesService;
    private ConcurrentInventoryService inventoryService;

    @Override
    public void init() throws ServletException {
        salesService = ConcurrentSalesService.getInstance();
        inventoryService = ConcurrentInventoryService.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String reportType = request.getParameter("type");

        if (reportType == null) {
            showReportsMenu(request, response);
            return;
        }

        try {
            switch (reportType) {
                case "daily-sales":
                    generateDailySalesReport(request, response);
                    break;
                case "stock":
                    generateStockReport(request, response);
                    break;
                case "reorder":
                    generateReorderReport(request, response);
                    break;
                case "top-selling":
                    generateTopSellingReport(request, response);
                    break;
                default:
                    showReportsMenu(request, response);
                    break;
            }
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }
    }

    private void showReportsMenu(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/reports.jsp").forward(request, response);
    }

    private void generateDailySalesReport(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        String dateParam = request.getParameter("date");
        Date date;

        if (dateParam != null && !dateParam.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                date = sdf.parse(dateParam);
            } catch (Exception e) {
                date = new Date();
            }
        } else {
            date = new Date();
        }

        Map<String, Object> report = salesService.getDailySalesReport(date);

        request.setAttribute("report", report);
        request.setAttribute("reportDate", date);
        request.getRequestDispatcher("/reports/daily-sales.jsp").forward(request, response);
    }

    private void generateStockReport(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        var products = inventoryService.getAllProducts();

        int totalProducts = products.size();
        int totalStockInStore = products.stream()
                .mapToInt(p -> p.getQuantityInStore()).sum();
        int totalStockOnShelf = products.stream()
                .mapToInt(p -> p.getQuantityOnShelf()).sum();
        int lowStockCount = (int) products.stream()
                .filter(p -> p.needsReorder()).count();

        request.setAttribute("products", products);
        request.setAttribute("totalProducts", totalProducts);
        request.setAttribute("totalStockInStore", totalStockInStore);
        request.setAttribute("totalStockOnShelf", totalStockOnShelf);
        request.setAttribute("lowStockCount", lowStockCount);

        request.getRequestDispatcher("/reports/stock-report.jsp").forward(request, response);
    }

    private void generateReorderReport(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        var lowStockProducts = inventoryService.getLowStockProducts();

        request.setAttribute("products", lowStockProducts);
        request.setAttribute("reportDate", new Date());
        request.getRequestDispatcher("/reports/reorder-report.jsp").forward(request, response);
    }

    private void generateTopSellingReport(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        String daysParam = request.getParameter("days");
        int days = (daysParam != null) ? Integer.parseInt(daysParam) : 7;

        Calendar cal = Calendar.getInstance();
        Date endDate = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, -days);
        Date startDate = cal.getTime();

        var topProducts = salesService.getTopSellingProducts(startDate, endDate, 10);
        var statistics = salesService.getSalesStatistics(startDate, endDate);

        request.setAttribute("topProducts", topProducts);
        request.setAttribute("statistics", statistics);
        request.setAttribute("days", days);
        request.setAttribute("startDate", startDate);
        request.setAttribute("endDate", endDate);

        request.getRequestDispatcher("/reports/top-selling.jsp").forward(request, response);
    }
}