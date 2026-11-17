package com.helpdesk.ui;

import com.helpdesk.dao.TicketDAO;
import com.helpdesk.exception.InvalidTicketException;
import com.helpdesk.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Complaint Submission Module
 * Handles ticket creation, category categorization, and validation.
 */
public class ComplaintSubmissionPanel extends JPanel {
    private final User currentUser;
    private final TicketDAO ticketDAO;
    private final Runnable onTicketCreated;

    private JTextField txtTitle;
    private JTextArea txtDescription;
    private JComboBox<String> cmbCategory;
    private JComboBox<Priority> cmbPriority;

    // Dynamic field cards
    private JPanel dynamicCardPanel;
    private CardLayout cardLayout;

    // Hardware fields
    private JTextField txtHwDeviceType;
    private JTextField txtHwSerial;
    private JCheckBox chkHwWarranty;

    // Software fields
    private JTextField txtSwName;
    private JTextField txtSwVersion;
    private JComboBox<String> cmbSwOs;

    // Service fields
    private JTextField txtSvcType;
    private JTextField txtSvcDepartment;

    // General fields
    private JTextField txtGenLocation;

    public ComplaintSubmissionPanel(User currentUser, TicketDAO ticketDAO, Runnable onTicketCreated) {
        this.currentUser = currentUser;
        this.ticketDAO = ticketDAO;
        this.onTicketCreated = onTicketCreated;

        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);
        setBorder(new EmptyBorder(20, 25, 20, 25));

        initUI();
    }

    private void initUI() {
        JPanel container = UITheme.createCardPanel();
        container.setLayout(new BorderLayout(0, 15));

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        headerPanel.setBackground(UITheme.CARD_BG);
        JLabel lblHeader = new JLabel("Log New Service Complaint / Helpdesk Ticket");
        lblHeader.setFont(UITheme.FONT_HEADER);
        lblHeader.setForeground(UITheme.PRIMARY);

        JLabel lblSub = new JLabel("Logged in as: " + currentUser.getFullName() + " (" + currentUser.getRole() + ") - Submit issue details below.");
        lblSub.setFont(UITheme.FONT_BODY);
        lblSub.setForeground(UITheme.TEXT_MUTED);

        headerPanel.add(lblHeader);
        headerPanel.add(lblSub);
        container.add(headerPanel, BorderLayout.NORTH);

        // Form Fields (GridBagLayout)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // 1. Complaint Title
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.2;
        formPanel.add(createLabel("Complaint Title:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.8;
        txtTitle = new JTextField();
        txtTitle.setFont(UITheme.FONT_BODY);
        formPanel.add(txtTitle, gbc);
        row++;

        // 2. Category selection
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.2;
        formPanel.add(createLabel("Category / Ticket Type:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.8;
        cmbCategory = new JComboBox<>(new String[]{"HARDWARE", "SOFTWARE", "SERVICE", "GENERAL"});
        cmbCategory.setFont(UITheme.FONT_BODY);
        formPanel.add(cmbCategory, gbc);
        row++;

        // 3. Dynamic Category Specific Fields (CardLayout)
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.2;
        formPanel.add(createLabel("Category Details:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.8;
        cardLayout = new CardLayout();
        dynamicCardPanel = new JPanel(cardLayout);
        dynamicCardPanel.setBackground(UITheme.CARD_BG);

        dynamicCardPanel.add(createHardwareSubPanel(), "HARDWARE");
        dynamicCardPanel.add(createSoftwareSubPanel(), "SOFTWARE");
        dynamicCardPanel.add(createServiceSubPanel(), "SERVICE");
        dynamicCardPanel.add(createGeneralSubPanel(), "GENERAL");
        formPanel.add(dynamicCardPanel, gbc);
        row++;

        cmbCategory.addActionListener(e -> {
            String selected = (String) cmbCategory.getSelectedItem();
            if (selected != null) {
                cardLayout.show(dynamicCardPanel, selected);
            }
        });

        // 4. Priority selection
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.2;
        formPanel.add(createLabel("Initial Priority:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.8;
        cmbPriority = new JComboBox<>(Priority.values());
        cmbPriority.setSelectedItem(Priority.MEDIUM);
        cmbPriority.setFont(UITheme.FONT_BODY);
        formPanel.add(cmbPriority, gbc);
        row++;

        // 5. Description
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(createLabel("Detailed Description:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.8;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        txtDescription = new JTextArea(4, 25);
        txtDescription.setFont(UITheme.FONT_BODY);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(txtDescription);
        formPanel.add(scrollDesc, gbc);
        row++;

        container.add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setBackground(UITheme.CARD_BG);

        JButton btnClear = UITheme.createSecondaryButton("Clear Form");
        btnClear.addActionListener(e -> clearForm());

        JButton btnSubmit = UITheme.createPrimaryButton("Submit Complaint Ticket");
        btnSubmit.addActionListener(e -> submitComplaint());

        actionPanel.add(btnClear);
        actionPanel.add(btnSubmit);
        container.add(actionPanel, BorderLayout.SOUTH);

        add(new JScrollPane(container), BorderLayout.CENTER);
    }

    private JPanel createHardwareSubPanel() {
        JPanel p = new JPanel(new GridLayout(2, 2, 8, 4));
        p.setBackground(UITheme.CARD_BG);
        txtHwDeviceType = new JTextField("Laptop / Projector / PC");
        txtHwSerial = new JTextField("SN-12345");
        chkHwWarranty = new JCheckBox("Under Manufacturer Warranty", true);
        chkHwWarranty.setBackground(UITheme.CARD_BG);

        p.add(new JLabel("Device Type:"));
        p.add(txtHwDeviceType);
        p.add(new JLabel("Serial Number:"));
        JPanel serialWarr = new JPanel(new BorderLayout(5, 0));
        serialWarr.setBackground(UITheme.CARD_BG);
        serialWarr.add(txtHwSerial, BorderLayout.CENTER);
        serialWarr.add(chkHwWarranty, BorderLayout.EAST);
        p.add(serialWarr);
        return p;
    }

    private JPanel createSoftwareSubPanel() {
        JPanel p = new JPanel(new GridLayout(2, 2, 8, 4));
        p.setBackground(UITheme.CARD_BG);
        txtSwName = new JTextField("Eclipse IDE / MS Office / OS");
        txtSwVersion = new JTextField("2024.1");
        cmbSwOs = new JComboBox<>(new String[]{"Windows 11", "Windows 10", "Ubuntu Linux", "macOS"});

        p.add(new JLabel("Software Name:"));
        p.add(txtSwName);
        p.add(new JLabel("Version & OS:"));
        JPanel verOs = new JPanel(new GridLayout(1, 2, 5, 0));
        verOs.setBackground(UITheme.CARD_BG);
        verOs.add(txtSwVersion);
        verOs.add(cmbSwOs);
        p.add(verOs);
        return p;
    }

    private JPanel createServiceSubPanel() {
        JPanel p = new JPanel(new GridLayout(2, 2, 8, 4));
        p.setBackground(UITheme.CARD_BG);
        txtSvcType = new JTextField("Hostel Wi-Fi / VPN Access");
        txtSvcDepartment = new JTextField("Network & IT Center");

        p.add(new JLabel("Service Type:"));
        p.add(txtSvcType);
        p.add(new JLabel("Department:"));
        p.add(txtSvcDepartment);
        return p;
    }

    private JPanel createGeneralSubPanel() {
        JPanel p = new JPanel(new GridLayout(1, 2, 8, 4));
        p.setBackground(UITheme.CARD_BG);
        txtGenLocation = new JTextField("Computer Lab 3, Block B");

        p.add(new JLabel("Location / Facility Area:"));
        p.add(txtGenLocation);
        return p;
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_PRIMARY);
        return lbl;
    }

    private void submitComplaint() {
        try {
            String title = txtTitle.getText().trim();
            String desc = txtDescription.getText().trim();
            String category = (String) cmbCategory.getSelectedItem();
            Priority priority = (Priority) cmbPriority.getSelectedItem();

            if (title.isEmpty()) {
                throw new InvalidTicketException("Please enter a complaint title.");
            }
            if (desc.isEmpty()) {
                throw new InvalidTicketException("Please provide a detailed description of the problem.");
            }

            Ticket newTicket;

            // Specialized Ticket creation based on Category
            if ("HARDWARE".equalsIgnoreCase(category)) {
                String dev = txtHwDeviceType.getText().trim();
                String sn = txtHwSerial.getText().trim();
                boolean warr = chkHwWarranty.isSelected();
                newTicket = new HardwareTicket(0, "", currentUser.getId(), currentUser.getFullName(),
                        title, desc, priority, dev, sn, warr);
            } else if ("SOFTWARE".equalsIgnoreCase(category)) {
                String sw = txtSwName.getText().trim();
                String ver = txtSwVersion.getText().trim();
                String os = (String) cmbSwOs.getSelectedItem();
                newTicket = new SoftwareTicket(0, "", currentUser.getId(), currentUser.getFullName(),
                        title, desc, priority, sw, ver, os);
            } else if ("SERVICE".equalsIgnoreCase(category)) {
                String svc = txtSvcType.getText().trim();
                String dept = txtSvcDepartment.getText().trim();
                newTicket = new ServiceTicket(0, "", currentUser.getId(), currentUser.getFullName(),
                        title, desc, priority, svc, dept);
            } else {
                String loc = txtGenLocation.getText().trim();
                newTicket = new GeneralTicket(0, "", currentUser.getId(), currentUser.getFullName(),
                        title, desc, priority, loc);
            }

            boolean success = ticketDAO.createTicket(newTicket);
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Complaint ticket registered successfully!\n"
                                + "Ticket Code: " + newTicket.getTicketCode() + "\n"
                                + "Category: " + newTicket.getTicketCategory() + "\n"
                                + "Calculated SLA: " + newTicket.calculateSlaHours() + " hours\n"
                                + "Priority: " + newTicket.getPriority(),
                        "Ticket Registered", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                if (onTicketCreated != null) {
                    onTicketCreated.run();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save ticket in database.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (InvalidTicketException ite) {
            // Validation error handling
            JOptionPane.showMessageDialog(this,
                    "Invalid Ticket Input: " + ite.getMessage(),
                    "Ticket Validation Error",
                    JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "System error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        txtTitle.setText("");
        txtDescription.setText("");
        txtHwDeviceType.setText("");
        txtHwSerial.setText("");
        txtSwName.setText("");
        txtSwVersion.setText("");
        txtSvcType.setText("");
        txtSvcDepartment.setText("");
        txtGenLocation.setText("");
        cmbPriority.setSelectedItem(Priority.MEDIUM);
    }
}
