package com.ecart.servlet;

import com.ecart.dao.ProductDAO;
import com.ecart.model.Product;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class CheckoutServlet extends HttpServlet {
    private ProductDAO productDAO = new ProductDAO();

    // Handles displaying the product list
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        List<Product> productList = productDAO.getAllProducts();
        request.setAttribute("productList", productList);
        request.getRequestDispatcher("catalog.jsp").forward(request, response);
    }

    // Handles the "Buy Now" button click
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        int productId = Integer.parseInt(request.getParameter("productId"));
        
        boolean success = productDAO.purchaseProduct(productId);
        
        if (success) {
            request.setAttribute("status", "success");
            request.setAttribute("msg", "✅ Purchase successful! Stock updated.");
        } else {
            request.setAttribute("status", "error");
            request.setAttribute("msg", "❌ Error: Item might be out of stock.");
        }
        
        // Refresh the list and show the message
        doGet(request, response);
    }
}