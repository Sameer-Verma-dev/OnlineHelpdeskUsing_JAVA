package com.helpdesk.dao;

import com.helpdesk.exception.HelpdeskException;
import com.helpdesk.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User Data Access Object (JDBC)
 * Handles database operations for users table.
 */
public class UserDAO {

    public User authenticate(String username, String password) throws HelpdeskException {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapUserFromResultSet(rs);
                } else {
                    return null; // Invalid credentials
                }
            }
        } catch (SQLException e) {
            throw new HelpdeskException("Database error during authentication: " + e.getMessage(), e);
        }
    }

    public boolean registerUser(User user) throws HelpdeskException {
        String sql = "INSERT INTO users (username, password, full_name, email, role, department_or_phone) VALUES (?, ?, ?, ?, ?, ?);";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getRole());

            String extra = "";
            if (user instanceof Customer) {
                extra = ((Customer) user).getContactNumber();
            } else if (user instanceof Agent) {
                extra = ((Agent) user).getDepartment();
            }
            pstmt.setString(6, extra);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        user.setId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
                throw new HelpdeskException("Username '" + user.getUsername() + "' is already taken.");
            }
            throw new HelpdeskException("Error registering user: " + e.getMessage(), e);
        }
    }

    public User getUserById(int id) throws HelpdeskException {
        String sql = "SELECT * FROM users WHERE id = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapUserFromResultSet(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new HelpdeskException("Error retrieving user: " + e.getMessage(), e);
        }
    }

    public List<Agent> getAllAgents() throws HelpdeskException {
        List<Agent> agents = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'AGENT' ORDER BY full_name ASC;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                User u = mapUserFromResultSet(rs);
                if (u instanceof Agent) {
                    agents.add((Agent) u);
                }
            }
            return agents;
        } catch (SQLException e) {
            throw new HelpdeskException("Error fetching agents: " + e.getMessage(), e);
        }
    }

    public List<User> getAllUsers() throws HelpdeskException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id ASC;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapUserFromResultSet(rs));
            }
            return users;
        } catch (SQLException e) {
            throw new HelpdeskException("Error fetching all users: " + e.getMessage(), e);
        }
    }

    private User mapUserFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String uname = rs.getString("username");
        String pass = rs.getString("password");
        String name = rs.getString("full_name");
        String email = rs.getString("email");
        String role = rs.getString("role");
        String deptOrPhone = rs.getString("department_or_phone");

        if ("ADMIN".equalsIgnoreCase(role)) {
            return new Admin(id, uname, pass, name, email);
        } else if ("AGENT".equalsIgnoreCase(role)) {
            return new Agent(id, uname, pass, name, email, deptOrPhone != null ? deptOrPhone : "IT Helpdesk");
        } else {
            return new Customer(id, uname, pass, name, email, deptOrPhone != null ? deptOrPhone : "");
        }
    }
}
