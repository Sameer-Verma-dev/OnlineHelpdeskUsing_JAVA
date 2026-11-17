package com.helpdesk.ui;

import com.helpdesk.dao.FeedbackDAO;
import com.helpdesk.dao.TicketDAO;
import com.helpdesk.model.Feedback;
import com.helpdesk.model.Status;
import com.helpdesk.model.Ticket;
import com.helpdesk.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Module 5: Resolution & Customer Feedback Panel.
 * Allows users to rate resolution quality (1-5 stars) and view feedback history.
 */
public class FeedbackPanel extends JPanel {
    private final User currentUser;
    private final TicketDAO ticketDAO;
    private final FeedbackDAO feedbackDAO;

    private JComboBox<TicketItem> cmbResolvedTickets;
    private JComboBox<String> cmbRating;
    private JTextArea txtComments;
    private JLabel lblAverageRating;
    private JLabel lblTotalCount;

    private JTable feedbackTable;
    private DefaultTableModel tableModel;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

    private static class TicketItem {
        final Ticket ticket;
        TicketItem(Ticket ticket) { this.ticket = ticket; }
        @Override
        public String toString() {
            return ticket.getTicketCode() + " - " + ticket.getTitle() + " (" + ticket.getStatus() + ")";
        }
    }

    public FeedbackPanel(User currentUser, TicketDAO ticketDAO, FeedbackDAO feedbackDAO) {
        this.currentUser = currentUser;
        this.ticketDAO = ticketDAO;
        this.feedbackDAO = feedbackDAO;

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BACKGROUND);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        initUI();
        refreshData();
    }

    private void initUI() {
        // Top Split: Submission Card + Metrics Card
        JPanel topContainer = new JPanel(new GridLayout(1, 2, 16, 0));
        topContainer.setOpaque(false);

        // Feedback Submission Card
        JPanel submitCard = UITheme.createCardPanel();
        submitCard.setLayout(new BorderLayout(0, 10));

        JLabel lblFormTitle = new JLabel("Submit Resolution Feedback");
        lblFormTitle.setFont(UITheme.FONT_SUBHEADER);
        lblFormTitle.setForeground(UITheme.PRIMARY);
        submitCard.add(lblFormTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        form.add(new JLabel("Select Ticket:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        cmbResolvedTickets = new JComboBox<>();
        cmbResolvedTickets.setFont(UITheme.FONT_BODY);
        form.add(cmbResolvedTickets, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        form.add(new JLabel("Rating:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        cmbRating = new JComboBox<>(new String[]{
                "5 Stars - Excellent (★★★★★)",
                "4 Stars - Good (★★★★☆)",
                "3 Stars - Average (★★★☆☆)",
                "2 Stars - Poor (★★☆☆☆)",
                "1 Star - Very Poor (★☆☆☆☆)"
        });
        cmbRating.setFont(UITheme.FONT_BODY);
        form.add(cmbRating, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("Comments:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        txtComments = new JTextArea(3, 20);
        txtComments.setFont(UITheme.FONT_BODY);
        txtComments.setLineWrap(true);
        txtComments.setWrapStyleWord(true);
        form.add(new JScrollPane(txtComments), gbc);

        submitCard.add(form, BorderLayout.CENTER);

        JButton btnSubmitFeedback = UITheme.createPrimaryButton("Post Feedback");
        btnSubmitFeedback.addActionListener(e -> submitFeedback());
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnWrap.setBackground(UITheme.CARD_BG);
        btnWrap.add(btnSubmitFeedback);
        submitCard.add(btnWrap, BorderLayout.SOUTH);

        // Metrics Card
        JPanel metricsCard = UITheme.createCardPanel();
        metricsCard.setLayout(new BoxLayout(metricsCard, BoxLayout.Y_AXIS));

        JLabel lblMetricsTitle = new JLabel("Helpdesk Satisfaction Metrics");
        lblMetricsTitle.setFont(UITheme.FONT_SUBHEADER);
        lblMetricsTitle.setForeground(UITheme.PRIMARY);

        lblAverageRating = new JLabel("Average Rating: ★ 0.0 / 5.0");
        lblAverageRating.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblAverageRating.setForeground(new Color(217, 119, 6));

        lblTotalCount = new JLabel("Total Reviews Submitted: 0");
        lblTotalCount.setFont(UITheme.FONT_BODY);
        lblTotalCount.setForeground(UITheme.TEXT_MUTED);

        JLabel lblNotice = new JLabel("<html><i>Feedback helps improve response time and resolution quality across departments.</i></html>");
        lblNotice.setFont(UITheme.FONT_SMALL);
        lblNotice.setForeground(UITheme.TEXT_MUTED);

        metricsCard.add(lblMetricsTitle);
        metricsCard.add(Box.createVerticalStrut(15));
        metricsCard.add(lblAverageRating);
        metricsCard.add(Box.createVerticalStrut(8));
        metricsCard.add(lblTotalCount);
        metricsCard.add(Box.createVerticalGlue());
        metricsCard.add(lblNotice);

        topContainer.add(submitCard);
        topContainer.add(metricsCard);
        add(topContainer, BorderLayout.NORTH);

        // Bottom: All Feedback History Table
        JPanel tableContainer = UITheme.createCardPanel();
        tableContainer.setLayout(new BorderLayout(0, 8));

        JLabel lblTableTitle = new JLabel("Customer Feedback & Review Log");
        lblTableTitle.setFont(UITheme.FONT_SUBHEADER);
        lblTableTitle.setForeground(UITheme.PRIMARY);
        tableContainer.add(lblTableTitle, BorderLayout.NORTH);

        String[] cols = {"ID", "Ticket Code", "Submitted By", "Rating", "Comments", "Date"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        feedbackTable = new JTable(tableModel);
        feedbackTable.setRowHeight(26);
        feedbackTable.setFont(UITheme.FONT_BODY);
        feedbackTable.getTableHeader().setFont(UITheme.FONT_BOLD);
        feedbackTable.getTableHeader().setBackground(new Color(241, 245, 249));

        tableContainer.add(new JScrollPane(feedbackTable), BorderLayout.CENTER);
        add(tableContainer, BorderLayout.CENTER);
    }

    public void refreshData() {
        try {
            // Load resolved tickets
            List<Ticket> allTickets = ticketDAO.getAllTickets();
            cmbResolvedTickets.removeAllItems();
            for (Ticket t : allTickets) {
                if (t.getStatus() == Status.RESOLVED || t.getStatus() == Status.CLOSED) {
                    cmbResolvedTickets.addItem(new TicketItem(t));
                }
            }

            // Load feedback reviews
            List<Feedback> feedbackList = feedbackDAO.getAllFeedback();
            tableModel.setRowCount(0);
            for (Feedback fb : feedbackList) {
                String dateStr = fb.getCreatedAt() != null ? fb.getCreatedAt().format(DT_FMT) : "";
                tableModel.addRow(new Object[]{
                        fb.getFeedbackId(),
                        fb.getTicketCode(),
                        fb.getUserName(),
                        fb.getStars() + " (" + fb.getRating() + "/5)",
                        fb.getComments(),
                        dateStr
                });
            }

            // Update stats
            double avg = feedbackDAO.getAverageRating();
            lblAverageRating.setText(String.format("Average Rating: ★ %.1f / 5.0", avg));
            lblTotalCount.setText("Total Reviews Submitted: " + feedbackList.size());

        } catch (Exception e) {
            System.err.println("Error refreshing feedback panel: " + e.getMessage());
        }
    }

    private void submitFeedback() {
        TicketItem item = (TicketItem) cmbResolvedTickets.getSelectedItem();
        if (item == null) {
            JOptionPane.showMessageDialog(this, "No resolved ticket available to review.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int rating = 5 - cmbRating.getSelectedIndex();
        String comments = txtComments.getText().trim();

        try {
            Feedback fb = new Feedback(item.ticket.getId(), item.ticket.getTicketCode(),
                    currentUser.getId(), currentUser.getFullName(), rating, comments);

            boolean ok = feedbackDAO.addFeedback(fb);
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "Thank you! Your feedback for " + item.ticket.getTicketCode() + " has been recorded.",
                        "Feedback Recorded", JOptionPane.INFORMATION_MESSAGE);
                txtComments.setText("");
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to submit feedback.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
