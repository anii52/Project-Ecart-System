package com.ecart.dao;

import com.ecart.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    // DB Config - UPDATE PASSWORD IF NEEDED
    private static final String URL = "jdbc:mysql://localhost:3306/ecart_db";
    private static final String USER = "root";
    private static final String PASS = "Tanu@2225"; 

    private Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM products")) {
            
            while (rs.next()) {
                String type = rs.getString("type");
                if ("Electronics".equalsIgnoreCase(type)) {
                    list.add(new Electronics(rs.getInt("id"), rs.getString("name"),
                            rs.getDouble("price"), rs.getInt("quantity"), rs.getString("brand_or_size")));
                } else {
                    list.add(new Clothing(rs.getInt("id"), rs.getString("name"),
                            rs.getDouble("price"), rs.getInt("quantity"), rs.getString("brand_or_size")));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public synchronized boolean purchaseProduct(int id) {
        try (Connection conn = getConnection()) {
            PreparedStatement pst = conn.prepareStatement("SELECT quantity FROM products WHERE id = ?");
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next() && rs.getInt("quantity") > 0) {
                PreparedStatement update = conn.prepareStatement("UPDATE products SET quantity = quantity - 1 WHERE id = ?");
                update.setInt(1, id);
                update.executeUpdate();
                return true;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }
}