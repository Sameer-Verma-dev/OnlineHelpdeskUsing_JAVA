package com.helpdesk;

import com.helpdesk.dao.DBConnection;
import com.helpdesk.dao.FeedbackDAO;
import com.helpdesk.dao.TicketDAO;
import com.helpdesk.dao.UserDAO;
import com.helpdesk.thread.TicketEscalationService;
import com.helpdesk.ui.LoginFrame;

import javax.swing.*;

/**
 * Main Application Entry Point
 * CSE2003 Object Oriented Programming using JAVA - Mini Project
 * Group 11: Online Complaint and Helpdesk System
 * Leader: Himanshu (2510001185)
 * Members: Shubham (2510001171), Sameer (2510001164), Khushveer (2510001153)
 */
public class Main {
    public static void main(String[] args) {
        // Set modern Look and Feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored2) {}
        }

        SwingUtilities.invokeLater(() -> {
            try {
                // Initialize Database (Unit 4: JDBC)
                DBConnection.initDatabase();

                UserDAO userDAO = new UserDAO();
                TicketDAO ticketDAO = new TicketDAO();
                FeedbackDAO feedbackDAO = new FeedbackDAO();

                // Initialize Background Escalation Thread (Unit 3: Multithreading)
                TicketEscalationService escalationService = TicketEscalationService.getInstance(ticketDAO);

                // Launch GUI Login Window (Unit 5: Swing)
                LoginFrame loginFrame = new LoginFrame(userDAO, ticketDAO, feedbackDAO, escalationService);
                loginFrame.setVisible(true);

                System.out.println("====================================================");
                System.out.println(" Group 11: Online Complaint & Helpdesk System");
                System.out.println(" CSE2003 Object Oriented Programming using JAVA");
                System.out.println(" Application launched successfully!");
                System.out.println("====================================================");

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Failed to launch Helpdesk System: " + e.getMessage(),
                        "Initialization Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
