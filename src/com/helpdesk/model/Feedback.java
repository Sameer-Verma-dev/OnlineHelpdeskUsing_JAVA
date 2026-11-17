package com.helpdesk.model;

import java.time.LocalDateTime;

/**
 * Model representing user feedback and rating after ticket resolution.
 * Database table: feedback
 */
public class Feedback {
    private int feedbackId;
    private int ticketId;
    private String ticketCode;
    private int userId;
    private String userName;
    private int rating; // 1 to 5
    private String comments;
    private LocalDateTime createdAt;

    public Feedback(int feedbackId, int ticketId, String ticketCode, int userId, 
                    String userName, int rating, String comments) {
        this.feedbackId = feedbackId;
        this.ticketId = ticketId;
        this.ticketCode = ticketCode;
        this.userId = userId;
        this.userName = userName;
        this.rating = Math.max(1, Math.min(5, rating));
        this.comments = comments != null ? comments.trim() : "";
        this.createdAt = LocalDateTime.now();
    }

    public Feedback(int ticketId, String ticketCode, int userId, String userName, int rating, String comments) {
        this(0, ticketId, ticketCode, userId, userName, rating, comments);
    }

    // Getters and Setters
    public int getFeedbackId() { return feedbackId; }
    public void setFeedbackId(int feedbackId) { this.feedbackId = feedbackId; }

    public int getTicketId() { return ticketId; }
    public void setTicketId(int ticketId) { this.ticketId = ticketId; }

    public String getTicketCode() { return ticketCode; }
    public void setTicketCode(String ticketCode) { this.ticketCode = ticketCode; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = Math.max(1, Math.min(5, rating)); }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getStars() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rating; i++) {
            sb.append("★");
        }
        for (int i = rating; i < 5; i++) {
            sb.append("☆");
        }
        return sb.toString();
    }
}
