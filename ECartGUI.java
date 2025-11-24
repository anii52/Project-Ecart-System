import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

/**
 * E-Cart Management System (GUI Version)
 * ---------------------------------------
 * A full-stack Java application demonstrating:
 * 1. OOP Principles (Inheritance, Abstraction, Polymorphism)
 * 2. JDBC Database Connectivity
 * 3. Multithreading (Concurrency control)
 * 4. Swing GUI
 */

// ==========================================
// SECTION 1: GUI LOGGER HELPER
// ==========================================
/**
 * Utility class to print logs to the GUI JTextArea safely.
 * Since Swing components are not thread-safe, we use SwingUtilities.invokeLater.
 */
class GuiLogger {
    public static JTextArea logArea;

    public static void log(String message) {
        if (logArea != null) {
            // Updates the GUI on the Event Dispatch Thread (EDT)
            SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
        } else {
            System.out.println(message);
        }
    }
}

// ==========================================
// SECTION 2: EXCEPTION HANDLING
// ==========================================
// Custom Checked Exception: Thrown when inventory is insufficient
class OutOfStockException extends Exception {
    public OutOfStockException(String message) { super(message); }
}

// Custom Runtime Exception: Thrown for general DB errors
class DatabaseException extends RuntimeException {
    public DatabaseException(String message) { super(message); }
}

// ==========================================
// SECTION 3: INTERFACES & ABSTRACTION
// ==========================================
// Interface defining tax calculation behavior
interface Taxable {
    double calculateTax();
}

// Abstract Base Class: Cannot be instantiated directly
abstract class Product implements Taxable {
    protected int id;
    protected String name;
    protected double price;
    protected int quantity;

    public Product(int id, String name, double price, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    // Abstract method: Child classes MUST implement this
    public abstract String getDetails();

    // Getters and Setters (Encapsulation)
    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}

// ==========================================
// SECTION 4: INHERITANCE
// ==========================================
class Electronics extends Product {
    private String brand;

    public Electronics(int id, String name, double price, int quantity, String brand) {
        super(id, name, price, quantity);
        this.brand = brand;
    }

    @Override
    public double calculateTax() { return this.price * 0.18; } // 18% Tax for Electronics

    @Override
    public String getDetails() {
        return "[Electronics] " + name + " (" + brand + ") - $" + price + " | Stock: " + quantity;
    }
}

class Clothing extends Product {
    private String size;

    public Clothing(int id, String name, double price, int quantity, String size) {
        super(id, name, price, quantity);
        this.size = size;
    }

    @Override
    public double calculateTax() { return this.price * 0.05; } // 5% Tax for Clothing

    @Override
    public String getDetails() {
        return "[Clothing] " + name + " (Size: " + size + ") - $" + price + " | Stock: " + quantity;
    }
}

// ==========================================
// SECTION 5: GENERICS & COLLECTIONS
// ==========================================
/**
 * Generic Shopping Cart class.
 * <T extends Product> ensures only Product objects (or subclasses) can be added.
 */
class ShoppingCart<T extends Product> {
    private List<T> items;

    public ShoppingCart() { this.items = new ArrayList<>(); }

    public void add(T item) { items.add(item); }

    public String generateSummary() {
        StringBuilder sb = new StringBuilder();
        double total = 0;
        for (T item : items) {
            sb.append(item.getDetails()).append("\n");
            total += item.getPrice();
        }
        sb.append("Total Value in Cart: $").append(total);
        return sb.toString();
    }
}

// ==========================================
// SECTION 6: JDBC & DATABASE OPERATIONS
// ==========================================
class DBConnection {
    // Database Credentials - Change these to match your MySQL setup
    private static final String URL = "jdbc:mysql://localhost:3306/ecart_db";
    private static final String USER = "root";
    private static final String PASS = "Tanu@2225"; // <--- UPDATE THIS PASSWORD

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}

/**
 * Data Access Object (DAO) Pattern.
 * Handles all direct interactions with the Database.
 */
class ProductDAO {
    // Helper to map a ResultSet row to a Java Object
    private Product mapRowToProduct(ResultSet rs) throws SQLException {
        String type = rs.getString("type");
        if ("Electronics".equalsIgnoreCase(type)) {
            return new Electronics(rs.getInt("id"), rs.getString("name"),
                    rs.getDouble("price"), rs.getInt("quantity"), rs.getString("brand"));
        } else {
            return new Clothing(rs.getInt("id"), rs.getString("name"),
                    rs.getDouble("price"), rs.getInt("quantity"), rs.getString("size"));
        }
    }

    // Fetch a single product by ID
    public Product getProduct(int id) {
        String query = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapRowToProduct(rs);

        } catch (SQLException e) { 
            GuiLogger.log("DB Read Error: " + e.getMessage()); 
        }
        return null;
    }

    // Update stock quantity after purchase
    public void updateStock(int productId, int newQuantity) {
        String query = "UPDATE products SET quantity = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, newQuantity);
            pstmt.setInt(2, productId);
            pstmt.executeUpdate();
        } catch (SQLException e) { 
            throw new DatabaseException(e.getMessage()); 
        }
    }

    // Get all products for the catalog
    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String query = "SELECT * FROM products";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                list.add(mapRowToProduct(rs));
            }
        } catch (SQLException e) { 
            GuiLogger.log("Catalog Error: " + e.getMessage()); 
        }
        return list;
    }
}

// ==========================================
// SECTION 7: INVENTORY SERVICE (BUSINESS LOGIC)
// ==========================================
class InventoryService {
    private ProductDAO productDAO = new ProductDAO();

    /**
     * SYNCHRONIZED Method:
     * Prevents "Race Conditions". If multiple users try to buy the last item
     * at the exact same time, this lock ensures only one enters this method at a time.
     */
    public synchronized void purchaseProduct(int productId, String userName) throws OutOfStockException {
        GuiLogger.log(userName + " requesting Product ID: " + productId + "...");

        Product p = productDAO.getProduct(productId);
        if (p == null) {
            GuiLogger.log("Product not found in DB.");
            return;
        }

        // Simulating network delay to demonstrate threading issues (if synchronized was missing)
        try { Thread.sleep(500); } catch (InterruptedException e) {}

        if (p.getQuantity() > 0) {
            int newQty = p.getQuantity() - 1;
            p.setQuantity(newQty);
            productDAO.updateStock(productId, newQty); // Update DB
            GuiLogger.log("SUCCESS: " + userName + " bought " + p.getName());
        } else {
            GuiLogger.log("FAILED: " + userName + " - Out of Stock!");
            throw new OutOfStockException("Sold Out");
        }
    }
}

// Task to simulate a user clicking "Checkout"
class CheckoutTask implements Runnable {
    private InventoryService inventory;
    private int pid;
    private String user;

    public CheckoutTask(InventoryService inv, int pid, String user) {
        this.inventory = inv; this.pid = pid; this.user = user;
    }

    public void run() {
        try { 
            inventory.purchaseProduct(pid, user); 
        } catch (OutOfStockException e) {
            // Exception handled in logs
        }
    }
}

// ==========================================
// SECTION 8: MAIN GUI
// ==========================================
public class ECartGUI extends JFrame {
    private JTextArea displayArea;
    private InventoryService inventoryService;

    public ECartGUI() {
        // GUI Setup
        setTitle("E-Cart Management System");
        setSize(750, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header
        JLabel header = new JLabel("E-Cart Admin Dashboard", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 18));
        add(header, BorderLayout.NORTH);

        // Log Area
        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        GuiLogger.logArea = displayArea; // Connect Logger to GUI
        add(new JScrollPane(displayArea), BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton btnShowCatalog = new JButton("View Catalog");
        JButton btnThreadTest = new JButton("Run Stress Test");
        JButton btnClear = new JButton("Clear Log");

        buttonPanel.add(btnShowCatalog);
        buttonPanel.add(btnThreadTest);
        buttonPanel.add(btnClear);
        add(buttonPanel, BorderLayout.SOUTH);

        // Logic Initialization
        inventoryService = new InventoryService();
        ProductDAO dao = new ProductDAO();

        // --- Event Listeners ---
        
        // 1. Show Catalog Button
        btnShowCatalog.addActionListener(e -> {
            displayArea.append("\n--- Product Catalog ---\n");
            ShoppingCart<Product> displayCart = new ShoppingCart<>();
            List<Product> products = dao.getAllProducts();
            
            if (products.isEmpty()) {
                displayArea.append("No products found! Check DB connection.\n");
            } else {
                for (Product p : products) displayCart.add(p);
                displayArea.append(displayCart.generateSummary());
            }
            displayArea.append("-----------------------\n");
        });

        // 2. Stress Test Button (Multithreading)
        // Simulates 3 users trying to buy product ID 102 at the same time
        btnThreadTest.addActionListener(e -> {
            displayArea.append("\n--- Starting Stress Test ---\n");
            new Thread(new CheckoutTask(inventoryService, 102, "User-1")).start();
            new Thread(new CheckoutTask(inventoryService, 102, "User-2")).start();
            new Thread(new CheckoutTask(inventoryService, 102, "User-3")).start();
        });

        // 3. Clear Button
        btnClear.addActionListener(e -> displayArea.setText(""));
    }

    public static void main(String[] args) {
        // Ensure GUI creation runs on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new ECartGUI().setVisible(true));
    }
}