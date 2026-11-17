package com.helpdesk.ui;

import com.helpdesk.dao.FeedbackDAO;
import com.helpdesk.dao.TicketDAO;
import com.helpdesk.dao.UserDAO;
import com.helpdesk.model.User;
import com.helpdesk.thread.TicketEscalationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Helpdesk Management Dashboard
 * Main window hosting all ticket workflows, feedback, escalation monitoring, and reporting.
 */
public class MainDashboard extends JFrame {
    private final User currentUser;
    private final UserDAO userDAO;
    private final TicketDAO ticketDAO;
    private final FeedbackDAO feedbackDAO;
    private final TicketEscalationService escalationService;

    private JTabbedPane tabbedPane;
    private TicketManagementPanel ticketManagementPanel;
    private ComplaintSubmissionPanel complaintSubmissionPanel;
    private FeedbackPanel feedbackPanel;
    private EscalationMonitorPanel escalationMonitorPanel;
    private TeamInfoPanel teamInfoPanel;

    public MainDashboard(User currentUser, UserDAO userDAO, TicketDAO ticketDAO, 
                         FeedbackDAO feedbackDAO, TicketEscalationService escalationService) {
        this.currentUser = currentUser;
        this.userDAO = userDAO;
        this.ticketDAO = ticketDAO;
        this.feedbackDAO = feedbackDAO;
        this.escalationService = escalationService;

        setTitle("Online Complaint and Helpdesk System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(950, 600));
        setLocationRelativeTo(null);

        initUI();

        // Start background escalation service thread
        if (!escalationService.isRunning()) {
            escalationService.start();
        }
    }

    private void initUI() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(UITheme.BACKGROUND);

        // Header Panel
        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setBackground(UITheme.PRIMARY);
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel lblTitle = new JLabel("Online Complaint & Helpdesk Management System");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.WEST);

        // User Info & Logout
        JPanel userBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        userBox.setOpaque(false);

        JLabel lblUser = new JLabel("Logged in as: " + currentUser.getFullName() + " [" + currentUser.getRole() + "]");
        lblUser.setFont(UITheme.FONT_BOLD);
        lblUser.setForeground(Color.WHITE);

        JButton btnLogout = UITheme.createSecondaryButton("Logout");
        btnLogout.addActionListener(e -> logout());

        userBox.add(lblUser);
        userBox.add(btnLogout);
        header.add(userBox, BorderLayout.EAST);

        rootPanel.add(header, BorderLayout.NORTH);

        // Tabbed Pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BOLD);
        tabbedPane.setBackground(Color.WHITE);

        Runnable refreshTickets = () -> {
            if (ticketManagementPanel != null) {
                ticketManagementPanel.loadTickets();
            }
            if (feedbackPanel != null) {
                feedbackPanel.refreshData();
            }
        };

        ticketManagementPanel = new TicketManagementPanel(currentUser, ticketDAO, userDAO, refreshTickets);
        complaintSubmissionPanel = new ComplaintSubmissionPanel(currentUser, ticketDAO, () -> {
            ticketManagementPanel.loadTickets();
            tabbedPane.setSelectedIndex(0); // Switch to tickets view
        });
        feedbackPanel = new FeedbackPanel(currentUser, ticketDAO, feedbackDAO);
        escalationMonitorPanel = new EscalationMonitorPanel(escalationService, refreshTickets);
        teamInfoPanel = new TeamInfoPanel();

        tabbedPane.addTab("Manage Tickets / Complaints", ticketManagementPanel);
        tabbedPane.addTab("Log New Complaint", complaintSubmissionPanel);
        tabbedPane.addTab("Resolution & Feedback", feedbackPanel);
        tabbedPane.addTab("Automated SLA Escalation Monitor", escalationMonitorPanel);
        tabbedPane.addTab("System Overview & Team", teamInfoPanel);

        tabbedPane.addChangeListener(e -> {
            int idx = tabbedPane.getSelectedIndex();
            if (idx == 0 && ticketManagementPanel != null) {
                ticketManagementPanel.loadTickets();
            } else if (idx == 2 && feedbackPanel != null) {
                feedbackPanel.refreshData();
            }
        });

        rootPanel.add(tabbedPane, BorderLayout.CENTER);

        // Status bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(241, 245, 249));
        statusBar.setBorder(new EmptyBorder(4, 15, 4, 15));
        JLabel lblStatusMsg = new JLabel("System Ready | SQLite Database Connected | Background Escalation Service: Active");
        lblStatusMsg.setFont(UITheme.FONT_SMALL);
        lblStatusMsg.setForeground(UITheme.TEXT_MUTED);
        statusBar.add(lblStatusMsg, BorderLayout.WEST);

        JLabel lblCopy = new JLabel("Enterprise Helpdesk Edition");
        lblCopy.setFont(UITheme.FONT_SMALL);
        lblCopy.setForeground(UITheme.TEXT_MUTED);
        statusBar.add(lblCopy, BorderLayout.EAST);

        rootPanel.add(statusBar, BorderLayout.SOUTH);

        setContentPane(rootPanel);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out?", "Confirm Logout",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame(userDAO, ticketDAO, feedbackDAO, escalationService).setVisible(true);
        }
    }
}
