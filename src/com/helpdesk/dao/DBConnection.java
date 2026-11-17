package com.helpdesk.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database Connection Manager (JDBC)
 * Manages database connectivity using JDBC (SQLite default).
 * Auto-initializes tables and initial seed data.
 */
public class DBConnection {
    private static final String DB_URL = "jdbc:sqlite:helpdesk.db";
    private static Connection connection = null;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            initDatabase();
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found in classpath: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Database initialization error: " + e.getMessage());
        }
    }

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }

    public static void initDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            // Enable foreign keys in SQLite
            stmt.execute("PRAGMA foreign_keys = ON;");

            // 1. users table
            String createUsersTable = "CREATE TABLE IF NOT EXISTS users ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "username VARCHAR(50) UNIQUE NOT NULL, "
                    + "password VARCHAR(100) NOT NULL, "
                    + "full_name VARCHAR(100) NOT NULL, "
                    + "email VARCHAR(100) NOT NULL, "
                    + "role VARCHAR(20) NOT NULL, " // 'USER', 'AGENT', 'ADMIN'
                    + "department_or_phone VARCHAR(100), "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ");";
            stmt.execute(createUsersTable);

            // 2. tickets table
            String createTicketsTable = "CREATE TABLE IF NOT EXISTS tickets ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "ticket_code VARCHAR(20) UNIQUE NOT NULL, "
                    + "user_id INTEGER NOT NULL, "
                    + "customer_name VARCHAR(100) NOT NULL, "
                    + "title VARCHAR(150) NOT NULL, "
                    + "description TEXT NOT NULL, "
                    + "category VARCHAR(30) NOT NULL, "
                    + "priority VARCHAR(20) NOT NULL, "
                    + "status VARCHAR(20) NOT NULL, "
                    + "assigned_agent_id INTEGER, "
                    + "assigned_agent_name VARCHAR(100), "
                    + "specific_detail_1 VARCHAR(100), "
                    + "specific_detail_2 VARCHAR(100), "
                    + "specific_detail_3 VARCHAR(100), "
                    + "escalated INTEGER DEFAULT 0, "
                    + "resolution_notes TEXT, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "FOREIGN KEY (user_id) REFERENCES users(id), "
                    + "FOREIGN KEY (assigned_agent_id) REFERENCES users(id)"
                    + ");";
            stmt.execute(createTicketsTable);

            // 3. feedback table
            String createFeedbackTable = "CREATE TABLE IF NOT EXISTS feedback ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "ticket_id INTEGER NOT NULL, "
                    + "ticket_code VARCHAR(20) NOT NULL, "
                    + "user_id INTEGER NOT NULL, "
                    + "user_name VARCHAR(100) NOT NULL, "
                    + "rating INTEGER NOT NULL, "
                    + "comments TEXT, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "FOREIGN KEY (ticket_id) REFERENCES tickets(id), "
                    + "FOREIGN KEY (user_id) REFERENCES users(id)"
                    + ");";
            stmt.execute(createFeedbackTable);

            // Seed initial users if table is empty
            seedInitialData(conn);

        } catch (SQLException e) {
            System.err.println("Failed to initialize database tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void seedInitialData(Connection conn) {
        try (Statement checkStmt = conn.createStatement()) {
            var rs = checkStmt.executeQuery("SELECT COUNT(*) FROM users;");
            if (rs.next() && rs.getInt(1) == 0) {
                Statement insertStmt = conn.createStatement();
                
                // Group 11 Members seeded:
                // Leader: Himanshu (2510001185)
                // Member 1: Shubham (2510001171)
                // Member 2: Sameer (2510001164)
                // Member 3: Khushveer (2510001153)
                insertStmt.executeUpdate("INSERT INTO users (username, password, full_name, email, role, department_or_phone) VALUES "
                        + "('admin', 'admin123', 'Administrator', 'admin@helpdesk.ac.in', 'ADMIN', 'Management'), "
                        + "('himanshu', 'user123', 'Himanshu (Leader)', 'himanshu@student.ac.in', 'USER', '+91-9876543210'), "
                        + "('shubham', 'agent123', 'Shubham (Agent)', 'shubham@helpdesk.ac.in', 'AGENT', 'Hardware Support'), "
                        + "('sameer', 'agent123', 'Sameer (Agent)', 'sameer@helpdesk.ac.in', 'AGENT', 'Software Support'), "
                        + "('khushveer', 'agent123', 'Khushveer (Agent)', 'khushveer@helpdesk.ac.in', 'AGENT', 'Network & Service');");

                // Seed some initial demo tickets
                insertStmt.executeUpdate("INSERT INTO tickets (ticket_code, user_id, customer_name, title, description, category, priority, status, assigned_agent_id, assigned_agent_name, specific_detail_1, specific_detail_2, specific_detail_3, escalated, resolution_notes) VALUES "
                        + "('TCK-1001', 2, 'Himanshu (Leader)', 'Lab 3 Projector Display Flickering', 'The HDMI projector display in Room 302 keeps cutting out during presentations.', 'HARDWARE', 'HIGH', 'IN_PROGRESS', 3, 'Shubham (Agent)', 'Epson Projector', 'SN-EP8892', 'Yes', 0, 'Replaced the faulty HDMI splitter cable; currently testing display stability.'), "
                        + "('TCK-1002', 2, 'Himanshu (Leader)', 'Eclipse IDE Java Compiler Error', 'Students unable to compile Java 26 project due to missing JDK path configuration on Lab PC 12.', 'SOFTWARE', 'CRITICAL', 'OPEN', NULL, NULL, 'Eclipse IDE', 'v2024-09', 'Windows 11', 1, ''), "
                        + "('TCK-1003', 2, 'Himanshu (Leader)', 'Hostel Wi-Fi Login Gateway Timeout', 'Campus Wi-Fi captive portal times out when trying to authenticate student roll numbers.', 'SERVICE', 'MEDIUM', 'RESOLVED', 5, 'Khushveer (Agent)', 'Wi-Fi & Network', 'IT Department', '', 0, 'Reset the DHCP lease pool and restarted the captive portal radius service.');");

                // Seed initial feedback
                insertStmt.executeUpdate("INSERT INTO feedback (ticket_id, ticket_code, user_id, user_name, rating, comments) VALUES "
                        + "(3, 'TCK-1003', 2, 'Himanshu (Leader)', 5, 'Quick response! Wi-Fi login started working within an hour.');");

                System.out.println("Initial seed data successfully inserted for Group 11!");
            }
        } catch (SQLException e) {
            System.err.println("Seed data insertion error: " + e.getMessage());
        }
    }
}
