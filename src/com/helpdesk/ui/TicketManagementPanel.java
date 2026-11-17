package com.helpdesk.ui;

import com.helpdesk.dao.TicketDAO;
import com.helpdesk.dao.UserDAO;
import com.helpdesk.exception.InvalidTicketException;
import com.helpdesk.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Ticket Assignment, Status and Priority Updates Management Panel.
 * Handles ticket workflow, agent assignments, and resolution updates.
 */
public class TicketManagementPanel extends JPanel {
    private final User currentUser;
    private final TicketDAO ticketDAO;
    private final UserDAO userDAO;
    private final Runnable onTicketUpdated;

    private JTable ticketTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private List<Ticket> currentTicketList = new ArrayList<>();

    private JTextField txtSearch;
    private JComboBox<String> cmbFilterCategory;
    private JComboBox<String> cmbFilterStatus;
    private JCheckBox chkMyTicketsOnly;

    private static final DateTimeFormatter DT_DISPLAY = DateTimeFormatter.ofPattern("dd-MMM HH:mm");

    public TicketManagementPanel(User currentUser, TicketDAO ticketDAO, UserDAO userDAO, Runnable onTicketUpdated) {
        this.currentUser = currentUser;
        this.ticketDAO = ticketDAO;
        this.userDAO = userDAO;
        this.onTicketUpdated = onTicketUpdated;

        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.BACKGROUND);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        initUI();
        loadTickets();
    }

    private void initUI() {
        // Top Filter Bar
        JPanel filterCard = UITheme.createCardPanel();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 6));

        filterCard.add(new JLabel("Search:"));
        txtSearch = new JTextField(12);
        txtSearch.setFont(UITheme.FONT_BODY);
        txtSearch.addCaretListener(e -> applyFilter());
        filterCard.add(txtSearch);

        filterCard.add(new JLabel("Category:"));
        cmbFilterCategory = new JComboBox<>(new String[]{"ALL", "HARDWARE", "SOFTWARE", "SERVICE", "GENERAL"});
        cmbFilterCategory.addActionListener(e -> applyFilter());
        filterCard.add(cmbFilterCategory);

        filterCard.add(new JLabel("Status:"));
        cmbFilterStatus = new JComboBox<>(new String[]{"ALL", "OPEN", "ASSIGNED", "IN_PROGRESS", "RESOLVED", "CLOSED"});
        cmbFilterStatus.addActionListener(e -> applyFilter());
        filterCard.add(cmbFilterStatus);

        chkMyTicketsOnly = new JCheckBox("Assigned to Me");
        chkMyTicketsOnly.setBackground(UITheme.CARD_BG);
        chkMyTicketsOnly.setVisible("AGENT".equalsIgnoreCase(currentUser.getRole()));
        chkMyTicketsOnly.addActionListener(e -> applyFilter());
        filterCard.add(chkMyTicketsOnly);

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> loadTickets());
        filterCard.add(btnRefresh);

        add(filterCard, BorderLayout.NORTH);

        // Center Table
        String[] columns = {"ID", "Code", "Title", "Category", "Priority", "Status", "Assigned Agent", "Escalated?", "SLA (hrs)", "Created"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only cells
            }
        };

        ticketTable = new JTable(tableModel);
        ticketTable.setRowHeight(28);
        ticketTable.setFont(UITheme.FONT_BODY);
        ticketTable.getTableHeader().setFont(UITheme.FONT_BOLD);
        ticketTable.getTableHeader().setBackground(new Color(241, 245, 249));
        ticketTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Apply custom renderers
        ticketTable.getColumnModel().getColumn(4).setCellRenderer(UITheme.createPriorityRenderer());
        ticketTable.getColumnModel().getColumn(5).setCellRenderer(UITheme.createStatusRenderer());

        rowSorter = new TableRowSorter<>(tableModel);
        ticketTable.setRowSorter(rowSorter);

        JScrollPane scrollTable = new JScrollPane(ticketTable);
        scrollTable.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        add(scrollTable, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel actionPanel = UITheme.createCardPanel();
        actionPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 4));

        JButton btnViewDetails = UITheme.createSecondaryButton("View Full Details");
        btnViewDetails.addActionListener(e -> viewTicketDetails());

        JButton btnAssign = UITheme.createPrimaryButton("Assign to Agent");
        btnAssign.addActionListener(e -> assignSelectedTicket());

        JButton btnStatus = UITheme.createSuccessButton("Update Status / Resolve");
        btnStatus.addActionListener(e -> updateSelectedStatus());

        JButton btnPriority = UITheme.createSecondaryButton("Change Priority / Escalate");
        btnPriority.addActionListener(e -> updateSelectedPriority());

        // Role-based button visibility
        if ("USER".equalsIgnoreCase(currentUser.getRole())) {
            btnAssign.setEnabled(false); // Users cannot assign agents
            btnStatus.setText("Close Ticket"); // Users can mark closed
        }

        actionPanel.add(btnViewDetails);
        actionPanel.add(btnAssign);
        actionPanel.add(btnStatus);
        actionPanel.add(btnPriority);

        add(actionPanel, BorderLayout.SOUTH);
    }

    public void loadTickets() {
        try {
            if ("USER".equalsIgnoreCase(currentUser.getRole())) {
                currentTicketList = ticketDAO.getTicketsByUser(currentUser.getId());
            } else {
                currentTicketList = ticketDAO.getAllTickets();
            }
            populateTable(currentTicketList);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load tickets: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateTable(List<Ticket> tickets) {
        tableModel.setRowCount(0);
        for (Ticket t : tickets) {
            String createdStr = t.getCreatedAt() != null ? t.getCreatedAt().format(DT_DISPLAY) : "";
            tableModel.addRow(new Object[]{
                    t.getId(),
                    t.getTicketCode(),
                    t.getTitle(),
                    t.getTicketCategory(),
                    t.getPriority(),
                    t.getStatus(),
                    (t.getAssignedAgentName() != null && !t.getAssignedAgentName().isEmpty()) ? t.getAssignedAgentName() : "Unassigned",
                    t.isEscalated() ? "YES (!)" : "No",
                    t.calculateSlaHours(),
                    createdStr
            });
        }
        applyFilter();
    }

    private void applyFilter() {
        String query = txtSearch.getText().trim().toLowerCase();
        String cat = (String) cmbFilterCategory.getSelectedItem();
        String st = (String) cmbFilterStatus.getSelectedItem();
        boolean myOnly = chkMyTicketsOnly.isSelected();

        RowFilter<DefaultTableModel, Object> rf = new RowFilter<DefaultTableModel, Object>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ?> entry) {
                // Search query
                if (!query.isEmpty()) {
                    String title = entry.getStringValue(2).toLowerCase();
                    String code = entry.getStringValue(1).toLowerCase();
                    if (!title.contains(query) && !code.contains(query)) {
                        return false;
                    }
                }
                // Category
                if (cat != null && !"ALL".equalsIgnoreCase(cat)) {
                    if (!cat.equalsIgnoreCase(entry.getStringValue(3))) {
                        return false;
                    }
                }
                // Status
                if (st != null && !"ALL".equalsIgnoreCase(st)) {
                    if (!st.equalsIgnoreCase(entry.getStringValue(5))) {
                        return false;
                    }
                }
                // My assigned only
                if (myOnly) {
                    String agent = entry.getStringValue(6);
                    if (!agent.contains(currentUser.getFullName())) {
                        return false;
                    }
                }
                return true;
            }
        };
        rowSorter.setRowFilter(rf);
    }

    private Ticket getSelectedTicket() {
        int selectedRow = ticketTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a ticket from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int modelRow = ticketTable.convertRowIndexToModel(selectedRow);
        int ticketId = (int) tableModel.getValueAt(modelRow, 0);

        for (Ticket t : currentTicketList) {
            if (t.getId() == ticketId) {
                return t;
            }
        }
        return null;
    }

    private void viewTicketDetails() {
        Ticket t = getSelectedTicket();
        if (t == null) return;

        String details = "=================================================\n"
                + "TICKET CODE: " + t.getTicketCode() + " (ID: " + t.getId() + ")\n"
                + "=================================================\n"
                + "Title: " + t.getTitle() + "\n"
                + "Raised By: " + t.getCustomerName() + "\n"
                + "Category: " + t.getTicketCategory() + "\n"
                + "Priority: " + t.getPriority() + (t.isEscalated() ? " [AUTO-ESCALATED]" : "") + "\n"
                + "Status: " + t.getStatus() + "\n"
                + "Calculated SLA: " + t.calculateSlaHours() + " Hours\n"
                + "Assigned Agent: " + (t.getAssignedAgentName() != null ? t.getAssignedAgentName() : "None") + "\n"
                + "Specific Details: " + t.getSpecificDetails() + "\n"
                + "Created At: " + t.getCreatedAt() + "\n"
                + "-------------------------------------------------\n"
                + "DESCRIPTION:\n" + t.getDescription() + "\n"
                + "-------------------------------------------------\n"
                + "RESOLUTION NOTES:\n" + (t.getResolutionNotes().isEmpty() ? "No resolution notes logged yet." : t.getResolutionNotes()) + "\n"
                + "=================================================";

        JTextArea ta = new JTextArea(details);
        ta.setEditable(false);
        ta.setFont(new Font("Consolas", Font.PLAIN, 12));
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(500, 350));
        JOptionPane.showMessageDialog(this, sp, "Ticket Details - " + t.getTicketCode(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void assignSelectedTicket() {
        Ticket t = getSelectedTicket();
        if (t == null) return;

        try {
            List<Agent> agents = userDAO.getAllAgents();
            if (agents.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No support agents available in system.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Agent selected = (Agent) JOptionPane.showInputDialog(
                    this,
                    "Assign Ticket [" + t.getTicketCode() + "] to which agent?",
                    "Assign Ticket to Agent",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    agents.toArray(),
                    agents.get(0)
            );

            if (selected != null) {
                t.assignTo(selected);

                boolean ok = ticketDAO.assignTicket(t.getId(), selected.getId(), selected.getFullName());
                if (ok) {
                    JOptionPane.showMessageDialog(this,
                            "Ticket " + t.getTicketCode() + " assigned to " + selected.getFullName() + "!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadTickets();
                    if (onTicketUpdated != null) onTicketUpdated.run();
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Assignment failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSelectedStatus() {
        Ticket t = getSelectedTicket();
        if (t == null) return;

        Status[] statuses;
        if ("USER".equalsIgnoreCase(currentUser.getRole())) {
            statuses = new Status[]{Status.CLOSED};
        } else {
            statuses = Status.values();
        }

        Status newStatus = (Status) JOptionPane.showInputDialog(
                this,
                "Current Status: " + t.getStatus() + "\nSelect New Status:",
                "Update Status",
                JOptionPane.QUESTION_MESSAGE,
                null,
                statuses,
                t.getStatus()
        );

        if (newStatus != null && newStatus != t.getStatus()) {
            try {
                t.updateStatus(newStatus);

                String notes = t.getResolutionNotes();
                if (newStatus == Status.RESOLVED || newStatus == Status.CLOSED) {
                    notes = JOptionPane.showInputDialog(this,
                            "Enter Resolution / Closing Summary Notes:",
                            t.getResolutionNotes());
                    if (notes == null) notes = "";
                }

                boolean ok = ticketDAO.updateStatusAndResolution(t.getId(), newStatus, notes);
                if (ok) {
                    JOptionPane.showMessageDialog(this,
                            "Ticket " + t.getTicketCode() + " updated to " + newStatus + "!",
                            "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                    loadTickets();
                    if (onTicketUpdated != null) onTicketUpdated.run();
                }
            } catch (InvalidTicketException ite) {
                JOptionPane.showMessageDialog(this,
                        "Status Transition Error: " + ite.getMessage(),
                        "Status Transition Error", JOptionPane.WARNING_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Database update failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateSelectedPriority() {
        Ticket t = getSelectedTicket();
        if (t == null) return;

        Priority newPriority = (Priority) JOptionPane.showInputDialog(
                this,
                "Current Priority: " + t.getPriority() + "\nSelect New Priority / Escalate:",
                "Priority Update",
                JOptionPane.QUESTION_MESSAGE,
                null,
                Priority.values(),
                t.getPriority()
        );

        if (newPriority != null && newPriority != t.getPriority()) {
            try {
                boolean ok = ticketDAO.updatePriority(t.getId(), newPriority);
                if (ok) {
                    JOptionPane.showMessageDialog(this,
                            "Priority for " + t.getTicketCode() + " updated to " + newPriority + "!",
                            "Priority Updated", JOptionPane.INFORMATION_MESSAGE);
                    loadTickets();
                    if (onTicketUpdated != null) onTicketUpdated.run();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Priority update error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
