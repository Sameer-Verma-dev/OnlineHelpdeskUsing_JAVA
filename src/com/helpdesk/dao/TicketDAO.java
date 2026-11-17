package com.helpdesk.dao;

import com.helpdesk.exception.HelpdeskException;
import com.helpdesk.exception.InvalidTicketException;
import com.helpdesk.model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Ticket Data Access Object (JDBC)
 * Handles full CRUD operations on tickets table.
 */
public class TicketDAO {
    private static final DateTimeFormatter SQL_DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public boolean createTicket(Ticket ticket) throws HelpdeskException {
        String countSql = "SELECT COUNT(*) FROM tickets;";
        String insertSql = "INSERT INTO tickets (ticket_code, user_id, customer_name, title, description, category, "
                + "priority, status, assigned_agent_id, assigned_agent_name, specific_detail_1, specific_detail_2, "
                + "specific_detail_3, escalated, resolution_notes, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        try (Connection conn = DBConnection.getConnection()) {
            // Generate ticket code
            String code = ticket.getTicketCode();
            if (code == null || code.trim().isEmpty()) {
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(countSql)) {
                    int count = 1001;
                    if (rs.next()) {
                        count += rs.getInt(1);
                    }
                    code = "TCK-" + count;
                    ticket.setTicketCode(code);
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, ticket.getTicketCode());
                pstmt.setInt(2, ticket.getUserId());
                pstmt.setString(3, ticket.getCustomerName());
                pstmt.setString(4, ticket.getTitle());
                pstmt.setString(5, ticket.getDescription());
                pstmt.setString(6, ticket.getTicketCategory());
                pstmt.setString(7, ticket.getPriority().name());
                pstmt.setString(8, ticket.getStatus().name());

                if (ticket.getAssignedAgentId() != null && ticket.getAssignedAgentId() > 0) {
                    pstmt.setInt(9, ticket.getAssignedAgentId());
                    pstmt.setString(10, ticket.getAssignedAgentName());
                } else {
                    pstmt.setNull(9, Types.INTEGER);
                    pstmt.setNull(10, Types.VARCHAR);
                }

                // Polymorphic specific details
                if (ticket instanceof HardwareTicket) {
                    HardwareTicket ht = (HardwareTicket) ticket;
                    pstmt.setString(11, ht.getDeviceType());
                    pstmt.setString(12, ht.getSerialNumber());
                    pstmt.setString(13, ht.isUnderWarranty() ? "Yes" : "No");
                } else if (ticket instanceof SoftwareTicket) {
                    SoftwareTicket st = (SoftwareTicket) ticket;
                    pstmt.setString(11, st.getSoftwareName());
                    pstmt.setString(12, st.getSoftwareVersion());
                    pstmt.setString(13, st.getOperatingSystem());
                } else if (ticket instanceof ServiceTicket) {
                    ServiceTicket st = (ServiceTicket) ticket;
                    pstmt.setString(11, st.getServiceType());
                    pstmt.setString(12, st.getDepartmentRequested());
                    pstmt.setString(13, "");
                } else if (ticket instanceof GeneralTicket) {
                    GeneralTicket gt = (GeneralTicket) ticket;
                    pstmt.setString(11, gt.getLocation());
                    pstmt.setString(12, "");
                    pstmt.setString(13, "");
                } else {
                    pstmt.setString(11, "");
                    pstmt.setString(12, "");
                    pstmt.setString(13, "");
                }

                pstmt.setInt(14, ticket.isEscalated() ? 1 : 0);
                pstmt.setString(15, ticket.getResolutionNotes());
                pstmt.setString(16, LocalDateTime.now().format(SQL_DT_FMT));
                pstmt.setString(17, LocalDateTime.now().format(SQL_DT_FMT));

                int affected = pstmt.executeUpdate();
                if (affected > 0) {
                    try (ResultSet keys = pstmt.getGeneratedKeys()) {
                        if (keys.next()) {
                            ticket.setId(keys.getInt(1));
                        }
                    }
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            throw new HelpdeskException("Error creating ticket in database: " + e.getMessage(), e);
        }
    }

    public List<Ticket> getAllTickets() throws HelpdeskException {
        List<Ticket> list = new ArrayList<>();
        String sql = "SELECT * FROM tickets ORDER BY id DESC;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Ticket t = mapTicketFromResultSet(rs);
                if (t != null) list.add(t);
            }
            return list;
        } catch (SQLException | InvalidTicketException e) {
            throw new HelpdeskException("Error fetching all tickets: " + e.getMessage(), e);
        }
    }

    public List<Ticket> getTicketsByUser(int userId) throws HelpdeskException {
        List<Ticket> list = new ArrayList<>();
        String sql = "SELECT * FROM tickets WHERE user_id = ? ORDER BY id DESC;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Ticket t = mapTicketFromResultSet(rs);
                    if (t != null) list.add(t);
                }
            }
            return list;
        } catch (SQLException | InvalidTicketException e) {
            throw new HelpdeskException("Error fetching tickets for user " + userId + ": " + e.getMessage(), e);
        }
    }

    public List<Ticket> getTicketsForAgent(int agentId) throws HelpdeskException {
        List<Ticket> list = new ArrayList<>();
        String sql = "SELECT * FROM tickets WHERE assigned_agent_id = ? ORDER BY id DESC;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, agentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Ticket t = mapTicketFromResultSet(rs);
                    if (t != null) list.add(t);
                }
            }
            return list;
        } catch (SQLException | InvalidTicketException e) {
            throw new HelpdeskException("Error fetching tickets for agent " + agentId + ": " + e.getMessage(), e);
        }
    }

    public List<Ticket> getUnresolvedTickets() throws HelpdeskException {
        List<Ticket> list = new ArrayList<>();
        String sql = "SELECT * FROM tickets WHERE status NOT IN ('RESOLVED', 'CLOSED') ORDER BY id ASC;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Ticket t = mapTicketFromResultSet(rs);
                if (t != null) list.add(t);
            }
            return list;
        } catch (SQLException | InvalidTicketException e) {
            throw new HelpdeskException("Error fetching unresolved tickets: " + e.getMessage(), e);
        }
    }

    public boolean assignTicket(int ticketId, int agentId, String agentName) throws HelpdeskException {
        String sql = "UPDATE tickets SET assigned_agent_id = ?, assigned_agent_name = ?, status = 'ASSIGNED', "
                + "updated_at = ? WHERE id = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, agentId);
            pstmt.setString(2, agentName);
            pstmt.setString(3, LocalDateTime.now().format(SQL_DT_FMT));
            pstmt.setInt(4, ticketId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new HelpdeskException("Error assigning ticket: " + e.getMessage(), e);
        }
    }

    public boolean updateStatusAndResolution(int ticketId, Status newStatus, String resolutionNotes) throws HelpdeskException {
        String sql = "UPDATE tickets SET status = ?, resolution_notes = ?, updated_at = ? WHERE id = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus.name());
            pstmt.setString(2, resolutionNotes != null ? resolutionNotes : "");
            pstmt.setString(3, LocalDateTime.now().format(SQL_DT_FMT));
            pstmt.setInt(4, ticketId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new HelpdeskException("Error updating ticket status: " + e.getMessage(), e);
        }
    }

    public boolean updatePriority(int ticketId, Priority newPriority) throws HelpdeskException {
        String sql = "UPDATE tickets SET priority = ?, updated_at = ? WHERE id = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newPriority.name());
            pstmt.setString(2, LocalDateTime.now().format(SQL_DT_FMT));
            pstmt.setInt(3, ticketId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new HelpdeskException("Error updating ticket priority: " + e.getMessage(), e);
        }
    }

    public boolean escalateTicket(int ticketId, Priority newPriority, String reason) throws HelpdeskException {
        String sql = "UPDATE tickets SET priority = ?, escalated = 1, updated_at = ? WHERE id = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newPriority.name());
            pstmt.setString(2, LocalDateTime.now().format(SQL_DT_FMT));
            pstmt.setInt(3, ticketId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new HelpdeskException("Error escalating ticket: " + e.getMessage(), e);
        }
    }

    public boolean deleteTicket(int ticketId) throws HelpdeskException {
        String sql = "DELETE FROM tickets WHERE id = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, ticketId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new HelpdeskException("Error deleting ticket: " + e.getMessage(), e);
        }
    }

    public Ticket getTicketById(int ticketId) throws HelpdeskException {
        String sql = "SELECT * FROM tickets WHERE id = ?;";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, ticketId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapTicketFromResultSet(rs);
                }
            }
            return null;
        } catch (SQLException | InvalidTicketException e) {
            throw new HelpdeskException("Error fetching ticket by id: " + e.getMessage(), e);
        }
    }

    /**
     * Polymorphic mapping of Ticket hierarchy from Database
     */
    private Ticket mapTicketFromResultSet(ResultSet rs) throws SQLException, InvalidTicketException {
        int id = rs.getInt("id");
        String ticketCode = rs.getString("ticket_code");
        int userId = rs.getInt("user_id");
        String customerName = rs.getString("customer_name");
        String title = rs.getString("title");
        String description = rs.getString("description");
        String category = rs.getString("category");
        Priority priority = Priority.valueOf(rs.getString("priority"));
        Status status = Status.valueOf(rs.getString("status"));
        int agentId = rs.getInt("assigned_agent_id");
        String agentName = rs.getString("assigned_agent_name");
        String detail1 = rs.getString("specific_detail_1");
        String detail2 = rs.getString("specific_detail_2");
        String detail3 = rs.getString("specific_detail_3");
        boolean escalated = rs.getInt("escalated") == 1;
        String resolutionNotes = rs.getString("resolution_notes");

        Ticket ticket;
        if ("HARDWARE".equalsIgnoreCase(category)) {
            boolean warranty = "Yes".equalsIgnoreCase(detail3);
            ticket = new HardwareTicket(id, ticketCode, userId, customerName, title, description, priority, detail1, detail2, warranty);
        } else if ("SOFTWARE".equalsIgnoreCase(category)) {
            ticket = new SoftwareTicket(id, ticketCode, userId, customerName, title, description, priority, detail1, detail2, detail3);
        } else if ("SERVICE".equalsIgnoreCase(category)) {
            ticket = new ServiceTicket(id, ticketCode, userId, customerName, title, description, priority, detail1, detail2);
        } else {
            ticket = new GeneralTicket(id, ticketCode, userId, customerName, title, description, priority, detail1);
        }

        ticket.setStatus(status);
        if (agentId > 0) {
            ticket.setAssignedAgentId(agentId);
            ticket.setAssignedAgentName(agentName);
        }
        ticket.setEscalated(escalated);
        ticket.setResolutionNotes(resolutionNotes != null ? resolutionNotes : "");

        try {
            String createdStr = rs.getString("created_at");
            if (createdStr != null && !createdStr.isEmpty()) {
                if (createdStr.length() > 19) createdStr = createdStr.substring(0, 19);
                createdStr = createdStr.replace("T", " ");
                ticket.setCreatedAt(LocalDateTime.parse(createdStr, SQL_DT_FMT));
            }
        } catch (Exception e) {
            // Keep default createdAt
        }

        return ticket;
    }
}
