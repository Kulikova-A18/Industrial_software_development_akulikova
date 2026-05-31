package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.http.MediaType;

import java.sql.*;
import java.io.*;
import java.util.regex.Pattern;

@SpringBootApplication
public class SecurityApp {
    public static void main(String[] args) {
        SpringApplication.run(SecurityApp.class, args);
        System.out.println("=== Security Test App запущен на http://localhost:8080 ===");
    }
}

@RestController
class VulnerableController {
    
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:test.db");
    }
    
    private static final String API_KEY = System.getenv("API_KEY") != null ? 
        System.getenv("API_KEY") : "sk_test_placeholder";
    private static final String PASSWORD = System.getenv("APP_PASSWORD") != null ? 
        System.getenv("APP_PASSWORD") : "change_me";
    
    @GetMapping("/user")
    public String getUser(@RequestParam String id) {
        if (id == null || !id.matches("\\d+")) {
            return "Invalid input: ID must be a number";
        }
        
        try (Connection conn = getConnection()) {
            String query = "SELECT username FROM users WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, id);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    return "User: " + rs.getString("username");
                }
                return "User not found";
            }
        } catch (SQLException e) {
            return "Database error occurred";
        }
    }
    
    private static final Pattern ALLOWED_IP_PATTERN = Pattern.compile("^([0-9]{1,3}\\.){3}[0-9]{1,3}$");
    
    @GetMapping("/ping")
    public String ping(@RequestParam String ip) {
        if (ip == null || !ALLOWED_IP_PATTERN.matcher(ip).matches()) {
            return "Invalid IP address format";
        }
        
        try {
            ProcessBuilder pb = new ProcessBuilder("ping", "-c", "1", ip);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            int exitCode = process.waitFor();
            return output.toString() + "\nExit code: " + exitCode;
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Error: Could not execute command";
        }
    }
    
    private static final String BASE_PATH = System.getProperty("java.io.tmpdir");
    private static final Pattern ALLOWED_FILENAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+$");
    
    @GetMapping("/readfile")
    public String readFile(@RequestParam String filename) {
        if (filename == null || !ALLOWED_FILENAME_PATTERN.matcher(filename).matches()) {
            return "Invalid filename format";
        }
        
        java.nio.file.Path safePath = java.nio.file.Paths.get(BASE_PATH).resolve(filename).normalize();
        
        if (!safePath.startsWith(java.nio.file.Paths.get(BASE_PATH))) {
            return "Access denied: Path traversal attempt detected";
        }
        
        java.io.File file = safePath.toFile();
        if (!file.exists()) {
            return "File not found";
        }
        if (!file.isFile()) {
            return "Not a regular file";
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder content = new StringBuilder();
            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null && lineCount++ < 100) {
                content.append(line).append("\n");
            }
            reader.close();
            return content.toString();
        } catch (IOException e) {
            return "Error reading file";
        }
    }
    
    @PostMapping(value = "/parsexml", consumes = MediaType.TEXT_PLAIN_VALUE)
    public String parseXml(@RequestBody String xml) {
        try {
            javax.xml.parsers.DocumentBuilderFactory factory = 
                javax.xml.parsers.DocumentBuilderFactory.newInstance();
            
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            
            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(
                new java.io.ByteArrayInputStream(xml.getBytes()));
            return "XML parsed successfully";
        } catch (Exception e) {
            return "Error parsing XML";
        }
    }
    
    private static final String ADMIN_TOKEN = System.getenv("ADMIN_TOKEN") != null ? 
        System.getenv("ADMIN_TOKEN") : java.util.UUID.randomUUID().toString();
    
    @GetMapping("/admin")
    public String adminPanel(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (ADMIN_TOKEN.equals(token)) {
                return "Admin Panel: Sensitive Data";
            }
        }
        return "Access Denied";
    }
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(VulnerableController.class);
    
    @GetMapping("/log")
    public String logMessage(@RequestParam String message) {
        if (message == null || message.length() > 1000) {
            return "Invalid message";
        }
        
        logger.error("User message: {}", message);
        return "Logged: " + message;
    }
    
    @GetMapping("/init")
    public String initDb() {
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY, username TEXT, password TEXT)");
            stmt.execute("INSERT OR IGNORE INTO users VALUES (1, 'admin', 'admin123')");
            stmt.execute("INSERT OR IGNORE INTO users VALUES (2, 'user', 'password456')");
            return "Database initialized";
        } catch (SQLException e) {
            return "Error initializing database";
        }
    }
}