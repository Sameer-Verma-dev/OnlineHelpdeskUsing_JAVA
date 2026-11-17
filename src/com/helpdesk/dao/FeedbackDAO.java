package com.helpdesk.dao;

import com.helpdesk.exception.HelpdeskException;
import com.helpdesk.model.Feedback;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Feedback Data Access Object (JDBC)
 * Manages database operations on feedback table.
 */
public class FeedbackDAO {
    private static final DateTimeFormatter SQL_DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public boolean addFeedback(Feedback feedback) throws HelpdeskException {
        String sql = "INSERT INTO feedback (ticket_id, ticket_code, user_id, user_name, rating, comments, created_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?);";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, feedback.getTicketId());
            pstmt.setString(2, feedback.getTicketCode());
            pstmt.setInt(3, feedback.getUserId());
            pstmt.setString(4, feedback.getUserName());
            pstmt.setInt(5, feedback.getRating());
            pstmt.setString(6, feedback.getComments());
            pstmt.setString(7, LocalDateTime.now().format(SQL_DT_FMT));

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        feedback.setFeedbackId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new HelpdeskException("Error saving feedback: " + e.getMessage(), e);
        }
    }

    public List<Feedback> getAllFeedback() throws HelpdeskException {
        List<Feedback> list = new ArrayList<>();
        String sql = "SELECT * FROM feedback ORDER BY id DESC;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapFeedback(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new HelpdeskException("Error fetching feedback list: " + e.getMessage(), e);
        }
    }

    public Feedback getFeedbackForTicket(int ticketId) throws HelpdeskException {
        String sql = "SELECT * FROM feedback WHERE ticket_id = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, ticketId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapFeedback(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new HelpdeskException("Error checking feedback for ticket: " + e.getMessage(), e);
        }
    }

    public double getAverageRating() {
        String sql = "SELECT AVG(rating) FROM feedback;";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            // return default
        }
        return 0.0;
    }

    private Feedback mapFeedback(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int ticketId = rs.getInt("ticket_id");
        String ticketCode = rs.getString("ticket_code");
        int userId = rs.getInt("user_id");
        String userName = rs.getString("user_name");
        int rating = rs.getInt("rating");
        String comments = rs.getString("comments");

        Feedback fb = new Feedback(id, ticketId, ticketCode, userId, userName, rating, comments);
        try {
            String createdStr = rs.getString("created_at");
            if (createdStr != null && !createdStr.isEmpty()) {
                if (createdStr.length() > 19) createdStr = createdStr.substring(0, 19);
                createdStr = createdStr.replace("T", " ");
                fb.setCreatedAt(LocalDateTime.parse(createdStr, SQL_DT_FMT));
            }
        } catch (Exception ignored) {}
        return fb;
    }
}
