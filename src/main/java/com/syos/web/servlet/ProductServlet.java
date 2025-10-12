package com.syos.web.servlet;

import com.google.gson.Gson;
import com.syos.web.dao.ProductDAO;
import com.syos.web.model.Product;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

public class ProductServlet extends HttpServlet {
    private ProductDAO productDAO = new ProductDAO();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        List<Product> products = productDAO.getAvailableProducts();
        String json = gson.toJson(products);
        response.getWriter().write(json);
    }
}