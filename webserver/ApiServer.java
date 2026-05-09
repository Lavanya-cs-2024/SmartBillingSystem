/**
 * ApiServer.java
 * 
 * PURPOSE:
 * Lightweight HTTP server that exposes your BillingService as REST API.
 * No external dependencies - uses built-in com.sun.net.httpserver.
 * 
 * HOW TO USE:
 * 1. Run this server: java webserver.ApiServer
 * 2. Open browser to: http://localhost:8080
 * 3. Web GUI will communicate with this server
 */

package webserver;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import backend.service.BillingService;
import backend.dao.ProductDAO;
import backend.dao.BillDAO;
import backend.model.Product;
import backend.model.BillItem;
import backend.util.DBConnection;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

public class ApiServer {
    
    private static BillingService billingService;
    private static ProductDAO productDAO;
    private static BillDAO billDAO;
    private static Gson gson;
    private static HttpServer server;
    
    public static void main(String[] args) throws IOException {
        billingService = new BillingService();
        productDAO = new ProductDAO();
        billDAO = new BillDAO();
        gson = new GsonBuilder().setPrettyPrinting().create();
        
        server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        // API endpoints
        server.createContext("/", new StaticFileHandler());
        server.createContext("/api/products", new ProductsHandler());
        server.createContext("/api/categories", new CategoriesHandler());
        server.createContext("/api/cart/add", new AddToCartHandler());
        server.createContext("/api/cart", new ViewCartHandler());
        server.createContext("/api/bill/generate", new GenerateBillHandler());
        server.createContext("/api/bill/pay", new PayBillHandler());
        server.createContext("/api/bill/cancel", new CancelBillHandler());
        server.createContext("/api/admin/stats", new StatsHandler());
        server.createContext("/api/admin/lowstock", new LowStockHandler());
        server.createContext("/api/admin/updatestock", new UpdateStockHandler());
        server.createContext("/api/admin/bills", new AllBillsHandler());
        
        server.setExecutor(null);
        server.start();
        
        System.out.println("=".repeat(50));
        System.out.println(" SMART BILLING SYSTEM - WEB SERVER");
        System.out.println("=".repeat(50));
        System.out.println(" Server started on http://localhost:8080");
        System.out.println(" Customer UI: http://localhost:8080");
        System.out.println(" Admin UI: http://localhost:8080/admin.html");
        System.out.println("=".repeat(50));
        System.out.println(" Press Ctrl+C to stop the server");
        System.out.println("=".repeat(50));
    }
    
    // ========== API HANDLERS ==========
    
    static class ProductsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            try {
                List<Product> products = productDAO.getAllProducts();
                String json = gson.toJson(products);
                sendResponse(exchange, 200, json);
            } catch (Exception e) {
                sendError(exchange, 500, e.getMessage());
            }
        }
    }
    
    static class CategoriesHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String sql = "SELECT id, name FROM categories ORDER BY id";
            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                JsonArray arr = new JsonArray();
                while (rs.next()) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("id", rs.getInt("id"));
                    obj.addProperty("name", rs.getString("name"));
                    arr.add(obj);
                }
                sendResponse(exchange, 200, gson.toJson(arr));
            } catch (Exception e) {
                sendError(exchange, 500, e.getMessage());
            }
        }
    }
    
    static class AddToCartHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }
            
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JsonObject json = gson.fromJson(body, JsonObject.class);
            int productId = json.get("productId").getAsInt();
            int quantity = json.get("quantity").getAsInt();
            
            boolean success = billingService.addToCart(productId, quantity);
            JsonObject response = new JsonObject();
            response.addProperty("success", success);
            sendResponse(exchange, 200, gson.toJson(response));
        }
    }
    
    static class ViewCartHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            List<BillItem> cart = billingService.getCartItems();
            JsonObject response = new JsonObject();
            
            JsonArray items = new JsonArray();
            double total = 0;
            for (BillItem item : cart) {
                JsonObject obj = new JsonObject();
                obj.addProperty("productId", item.getProductId());
                obj.addProperty("productName", item.getProductName());
                obj.addProperty("quantity", item.getQuantity());
                obj.addProperty("price", item.getUnitPrice());
                obj.addProperty("subtotal", item.getSubtotal());
                items.add(obj);
                total += item.getSubtotal();
            }
            response.add("items", items);
            response.addProperty("total", total);
            response.addProperty("count", cart.size());
            sendResponse(exchange, 200, gson.toJson(response));
        }
    }
    
    static class GenerateBillHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }
            
            int billId = billingService.generateBill();
            JsonObject response = new JsonObject();
            if (billId > 0) {
                response.addProperty("success", true);
                response.addProperty("billId", billId);
            } else {
                response.addProperty("success", false);
                response.addProperty("message", "Cart is empty!");
            }
            sendResponse(exchange, 200, gson.toJson(response));
        }
    }
    
    static class PayBillHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }
            
            boolean success = billingService.processPayment();
            JsonObject response = new JsonObject();
            response.addProperty("success", success);
            if (success) {
                response.addProperty("message", "Payment successful!");
            } else {
                response.addProperty("message", "Payment failed!");
            }
            sendResponse(exchange, 200, gson.toJson(response));
        }
    }
    
    static class CancelBillHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }
            
            boolean success = billingService.cancelBill();
            JsonObject response = new JsonObject();
            response.addProperty("success", success);
            sendResponse(exchange, 200, gson.toJson(response));
        }
    }
    
    static class StatsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            try (Connection conn = DBConnection.getConnection()) {
                JsonObject stats = new JsonObject();
                
                // Total bills and revenue
                String sql = "SELECT COUNT(*) as count, SUM(total_amount) as revenue FROM bills WHERE status = 'PAID'";
                try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                    if (rs.next()) {
                        stats.addProperty("totalBills", rs.getInt("count"));
                        stats.addProperty("totalRevenue", rs.getDouble("revenue"));
                    }
                }
                
                // Status breakdown
                JsonArray breakdown = new JsonArray();
                String statusSql = "SELECT status, COUNT(*) as count, SUM(total_amount) as amount FROM bills GROUP BY status";
                try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(statusSql)) {
                    while (rs.next()) {
                        JsonObject obj = new JsonObject();
                        String status = rs.getString("status");
                        String displayStatus;
                        switch (status) {
                            case "PAID": displayStatus = "Payment Successful"; break;
                            case "FAILED": displayStatus = "Payment Cancelled"; break;
                            case "PENDING": displayStatus = "Payment Pending"; break;
                            default: displayStatus = status;
                        }
                        obj.addProperty("status", displayStatus);
                        obj.addProperty("count", rs.getInt("count"));
                        obj.addProperty("amount", rs.getDouble("amount"));
                        breakdown.add(obj);
                    }
                }
                stats.add("breakdown", breakdown);
                sendResponse(exchange, 200, gson.toJson(stats));
            } catch (Exception e) {
                sendError(exchange, 500, e.getMessage());
            }
        }
    }
    
    static class LowStockHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String sql = "SELECT p.id, p.name, p.stock_quantity, p.reorder_level, c.name as category " +
                        "FROM products p JOIN categories c ON p.category_id = c.id " +
                        "WHERE p.stock_quantity < p.reorder_level ORDER BY p.stock_quantity ASC";
            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                JsonArray arr = new JsonArray();
                while (rs.next()) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("id", rs.getInt("id"));
                    obj.addProperty("name", rs.getString("name"));
                    obj.addProperty("category", rs.getString("category"));
                    obj.addProperty("stock", rs.getInt("stock_quantity"));
                    obj.addProperty("reorderLevel", rs.getInt("reorder_level"));
                    arr.add(obj);
                }
                sendResponse(exchange, 200, gson.toJson(arr));
            } catch (Exception e) {
                sendError(exchange, 500, e.getMessage());
            }
        }
    }
    
    static class UpdateStockHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }
            
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JsonObject json = gson.fromJson(body, JsonObject.class);
            int productId = json.get("productId").getAsInt();
            int newStock = json.get("stock").getAsInt();
            
            String sql = "UPDATE products SET stock_quantity = ? WHERE id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, newStock);
                ps.setInt(2, productId);
                int affected = ps.executeUpdate();
                JsonObject response = new JsonObject();
                response.addProperty("success", affected > 0);
                sendResponse(exchange, 200, gson.toJson(response));
            } catch (Exception e) {
                sendError(exchange, 500, e.getMessage());
            }
        }
    }
    
    static class AllBillsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String sql = "SELECT id, bill_number, bill_date, total_amount, status FROM bills ORDER BY id DESC LIMIT 30";
            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                JsonArray arr = new JsonArray();
                while (rs.next()) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("id", rs.getInt("id"));
                    obj.addProperty("billNumber", rs.getString("bill_number"));
                    obj.addProperty("date", rs.getTimestamp("bill_date").toString());
                    obj.addProperty("amount", rs.getDouble("total_amount"));
                    obj.addProperty("status", rs.getString("status"));
                    arr.add(obj);
                }
                sendResponse(exchange, 200, gson.toJson(arr));
            } catch (Exception e) {
                sendError(exchange, 500, e.getMessage());
            }
        }
    }
    
    static class StaticFileHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/")) path = "/index.html";
        if (path.equals("/admin")) path = "/admin.html";
        
        String filePath = "webapp" + path;
        File file = new File(filePath);
        
        if (file.exists() && !file.isDirectory()) {
            // Set correct content type
            String contentType = "text/html";
            if (path.endsWith(".css")) contentType = "text/css";
            if (path.endsWith(".js")) contentType = "application/javascript";
            
            exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
            
            // Read file and send response
            byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } else {
            String notFound = "<h1>404 - Page Not Found</h1><p>The requested page does not exist.</p>";
            byte[] bytes = notFound.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(404, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
    }
    
    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
    
    private static void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        JsonObject error = new JsonObject();
        error.addProperty("error", message);
        sendResponse(exchange, statusCode, gson.toJson(error));
    }
}
