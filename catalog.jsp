<%@ page import="java.util.List, com.ecart.model.Product" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>E-Cart Project</title>
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f0f2f5; padding: 20px; }
        .container { max-width: 900px; margin: 0 auto; background: #fff; padding: 30px; border-radius: 10px; box-shadow: 0 4px 8px rgba(0,0,0,0.1); }
        h2 { color: #333; text-align: center; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th { background: #007bff; color: white; padding: 12px; }
        td { border-bottom: 1px solid #ddd; padding: 12px; text-align: center; }
        .btn { background: #28a745; color: white; border: none; padding: 8px 16px; cursor: pointer; border-radius: 4px; }
        .btn:hover { background: #218838; }
        .alert { padding: 15px; margin-bottom: 20px; border-radius: 5px; text-align: center; font-weight: bold; }
        .success { background: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
        .error { background: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
        .sold-out { color: red; font-weight: bold; }
    </style>
</head>
<body>
    <div class="container">
        <h2>🛍️ E-Cart System (Review 2)</h2>

        <%-- Feedback Message Block --%>
        <% String status = (String) request.getAttribute("status");
           String msg = (String) request.getAttribute("msg");
           if (status != null) { %>
            <div class="alert <%= status %>"> <%= msg %> </div>
        <% } %>

        <table>
            <thead>
                <tr>
                    <th>ID</th><th>Name</th><th>Details</th><th>Price</th><th>Stock</th><th>Action</th>
                </tr>
            </thead>
            <tbody>
                <% List<Product> list = (List<Product>) request.getAttribute("productList");
                   if (list != null) {
                       for (Product p : list) { %>
                <tr>
                    <td><%= p.getId() %></td>
                    <td><%= p.getName() %></td>
                    <td><%= p.getDetails() %></td>
                    <td>$<%= p.getPrice() %></td>
                    <td><%= p.getQuantity() %></td>
                    <td>
                        <% if (p.getQuantity() > 0) { %>
                        <form action="buy" method="post">
                            <input type="hidden" name="productId" value="<%= p.getId() %>">
                            <button type="submit" class="btn">Buy Now</button>
                        </form>
                        <% } else { %>
                            <span class="sold-out">SOLD OUT</span>
                        <% } %>
                    </td>
                </tr>
                <%     }
                   } else { %>
                <tr><td colspan="6">Loading database... Refresh the page.</td></tr>
                <% } %>
            </tbody>
        </table>
    </div>
</body>
</html>