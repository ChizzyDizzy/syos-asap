package com.syos.web.servlet;

import com.google.gson.Gson;
import com.syos.web.dao.BillDAO;
import com.syos.web.dao.ProductDAO;
import com.syos.web.model.Bill;
import com.syos.web.model.Product;
import com.syos.web.model.User;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.BufferedReader;

public class BillingServlet extends HttpServlet {
    private BillDAO billDAO = new BillDAO();
    private ProductDAO productDAO = new ProductDAO();
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        User user = (User) session.getAttribute("user");

        BufferedReader reader = request.getReader();
        Bill bill = gson.fromJson(reader, Bill.class);
        bill.setUserId(user.getUserId());

        // Update stock for each item
        boolean stockUpdated = true;
        for (Bill.BillItem item : bill.getItems()) {
            if (!productDAO.updateStock(item.getItemCode(), item.getQuantity())) {
                stockUpdated = false;
                break;
            }
        }

        if (stockUpdated) {
            String billNumber = billDAO.saveBill(bill);
            if (billNumber != null) {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":true,\"billNumber\":\"" + billNumber + "\"}");
                return;
            }
        }

        response.setContentType("application/json");
        response.getWriter().write("{\"success\":false,\"message\":\"Failed to process bill\"}");
    }
}