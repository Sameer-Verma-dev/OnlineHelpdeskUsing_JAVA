package com.helpdesk.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Displays Project Team Members and System Architecture Overview.
 */
public class TeamInfoPanel extends JPanel {

    public TeamInfoPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);
        setBorder(new EmptyBorder(20, 25, 20, 25));

        initUI();
    }

    private void initUI() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(UITheme.BACKGROUND);

        // 1. Project Title Card
        JPanel titleCard = UITheme.createCardPanel();
        titleCard.setLayout(new GridLayout(3, 1, 0, 4));
        JLabel lblCourse = new JLabel("Online Complaint and Helpdesk System");
        lblCourse.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblCourse.setForeground(UITheme.PRIMARY);

        JLabel lblProj = new JLabel("Enterprise Incident Logging, Dispatch & Resolution Platform");
        lblProj.setFont(UITheme.FONT_SUBHEADER);
        lblProj.setForeground(UITheme.PRIMARY_LIGHT);

        JLabel lblTag = new JLabel("Tagline: \"Log service complaints and monitor their resolution in real time.\"");
        lblTag.setFont(UITheme.FONT_BODY);
        lblTag.setForeground(UITheme.TEXT_MUTED);

        titleCard.add(lblCourse);
        titleCard.add(lblProj);
        titleCard.add(lblTag);
        container.add(titleCard);
        container.add(Box.createVerticalStrut(16));

        // 2. Team Members Table Card
        JPanel teamCard = UITheme.createCardPanel();
        teamCard.setLayout(new BorderLayout(0, 10));
        JLabel lblTeam = new JLabel("Project Team Members");
        lblTeam.setFont(UITheme.FONT_SUBHEADER);
        lblTeam.setForeground(UITheme.PRIMARY);
        teamCard.add(lblTeam, BorderLayout.NORTH);

        String[] teamCols = {"Role", "Name", "UID / Roll Number", "Status"};
        Object[][] teamData = {
                {"Project Leader", "Himanshu", "2510001185", "Active"},
                {"Core Developer", "Shubham", "2510001171", "Active"},
                {"Core Developer", "Sameer", "2510001164", "Active"},
                {"Core Developer", "Khushveer", "2510001153", "Active"}
        };
        JTable teamTable = new JTable(new DefaultTableModel(teamData, teamCols));
        teamTable.setRowHeight(28);
        teamTable.setFont(UITheme.FONT_BODY);
        teamTable.getTableHeader().setFont(UITheme.FONT_BOLD);
        teamTable.setEnabled(false);
        teamCard.add(new JScrollPane(teamTable), BorderLayout.CENTER);

        container.add(teamCard);
        container.add(Box.createVerticalStrut(16));

        // 3. System Architecture Card
        JPanel archCard = UITheme.createCardPanel();
        archCard.setLayout(new BorderLayout(0, 10));
        JLabel lblArch = new JLabel("System Architecture & Core Technical Components");
        lblArch.setFont(UITheme.FONT_SUBHEADER);
        lblArch.setForeground(UITheme.PRIMARY);
        archCard.add(lblArch, BorderLayout.NORTH);

        String[] archCols = {"System Component", "Design Pattern / Capability", "Key Classes & Implementation"};
        Object[][] archData = {
                {"User & Role Management", "Encapsulation & Access Control", "User, Agent, Admin, Customer classes"},
                {"Ticket Classification & Hierarchy", "Polymorphism & Interface Contracts", "Assignable interface; Hardware, Software, Service, General Tickets"},
                {"Automated Escalation Service", "Multithreaded Background Monitor", "TicketEscalationService (Runnable thread) with dynamic priority elevation"},
                {"Data Persistence Layer", "DAO Pattern & JDBC SQL Transactions", "DBConnection, UserDAO, TicketDAO, FeedbackDAO (SQLite)"},
                {"Helpdesk Management Dashboard", "Event-Driven Swing & Model-View Pattern", "MainDashboard, ComplaintSubmissionPanel, TicketManagementPanel, FeedbackPanel"}
        };
        JTable archTable = new JTable(new DefaultTableModel(archData, archCols));
        archTable.setRowHeight(30);
        archTable.setFont(UITheme.FONT_BODY);
        archTable.getTableHeader().setFont(UITheme.FONT_BOLD);
        archTable.setEnabled(false);
        archCard.add(new JScrollPane(archTable), BorderLayout.CENTER);

        container.add(archCard);
        container.add(Box.createVerticalStrut(16));

        // 4. Database Tables Card
        JPanel dbCard = UITheme.createCardPanel();
        dbCard.setLayout(new BorderLayout(0, 8));
        JLabel lblDb = new JLabel("Database Architecture (Tables & Relationships)");
        lblDb.setFont(UITheme.FONT_SUBHEADER);
        lblDb.setForeground(UITheme.PRIMARY);
        dbCard.add(lblDb, BorderLayout.NORTH);

        JLabel lblDbDesc = new JLabel("<html><b>1. users:</b> id, username, password, full_name, email, role (USER/AGENT/ADMIN), department_or_phone<br/>"
                + "<b>2. tickets:</b> id, ticket_code, user_id, customer_name, title, description, category, priority, status, assigned_agent_id, specific_details, escalated, resolution_notes<br/>"
                + "<b>3. feedback:</b> id, ticket_id, ticket_code, user_id, user_name, rating (1-5 stars), comments, created_at</html>");
        lblDbDesc.setFont(UITheme.FONT_BODY);
        dbCard.add(lblDbDesc, BorderLayout.CENTER);

        container.add(dbCard);

        add(new JScrollPane(container), BorderLayout.CENTER);
    }
}
