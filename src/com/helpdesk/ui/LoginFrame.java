package com.helpdesk.ui;

import com.helpdesk.dao.FeedbackDAO;
import com.helpdesk.dao.TicketDAO;
import com.helpdesk.dao.UserDAO;
import com.helpdesk.exception.HelpdeskException;
import com.helpdesk.model.Agent;
import com.helpdesk.model.Customer;
import com.helpdesk.model.User;
import com.helpdesk.thread.TicketEscalationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Module 1: User Registration and Authentication Frame.
 * Provides login, user signup, role selection, and quick demo credentials for presentation.
 */
public class LoginFrame extends JFrame {
    private final UserDAO userDAO;
    private final TicketDAO ticketDAO;
    private final FeedbackDAO feedbackDAO;
    private final TicketEscalationService escalationService;

    // Login Form Fields
    private JTextField txtLoginUsername;
    private JPasswordField txtLoginPassword;

    // Register Form Fields
    private JTextField txtRegFullName;
    private JTextField txtRegUsername;
    private JPasswordField txtRegPassword;
    private JTextField txtRegEmail;
    private JComboBox<String> cmbRegRole;
    private JTextField txtRegDeptOrPhone;
    private JLabel lblRegDeptOrPhone;

    public LoginFrame(UserDAO userDAO, TicketDAO ticketDAO, FeedbackDAO feedbackDAO, TicketEscalationService escalationService) {
        this.userDAO = userDAO;
        this.ticketDAO = ticketDAO;
        this.feedbackDAO = feedbackDAO;
        this.escalationService = escalationService;

        setTitle("Online Complaint & Helpdesk System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(560, 640);
        setResizable(false);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(UITheme.BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Header Banner
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(UITheme.PRIMARY);
        headerPanel.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel lblTitle = new JLabel("Online Complaint & Helpdesk System");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Service Incident Logging & Support Portal");
        lblSub.setFont(UITheme.FONT_BODY);
        lblSub.setForeground(new Color(224, 231, 255));
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(lblTitle);
        headerPanel.add(Box.createVerticalStrut(6));
        headerPanel.add(lblSub);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Tabbed Panel: Sign In vs Register
        JTabbedPane authTabs = new JTabbedPane();
        authTabs.setFont(UITheme.FONT_BOLD);

        authTabs.addTab("Sign In", createSignInPanel());
        authTabs.addTab("New User Registration", createRegisterPanel());

        mainPanel.add(authTabs, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private JPanel createSignInPanel() {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(0, 12));

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 12));
        form.setBackground(UITheme.CARD_BG);

        JLabel lblUname = new JLabel("Username:");
        lblUname.setFont(UITheme.FONT_BOLD);
        txtLoginUsername = new JTextField("himanshu");
        txtLoginUsername.setFont(UITheme.FONT_BODY);

        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(UITheme.FONT_BOLD);
        txtLoginPassword = new JPasswordField("user123");
        txtLoginPassword.setFont(UITheme.FONT_BODY);

        form.add(lblUname);
        form.add(txtLoginUsername);
        form.add(lblPass);
        form.add(txtLoginPassword);

        card.add(form, BorderLayout.NORTH);

        // Sign In Action
        JButton btnSignIn = UITheme.createPrimaryButton("Sign In to System");
        btnSignIn.addActionListener(e -> handleLogin());

        JPanel centerBtnWrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerBtnWrap.setBackground(UITheme.CARD_BG);
        centerBtnWrap.add(btnSignIn);

        // Quick Demo Logins Section
        JPanel demoBox = new JPanel();
        demoBox.setLayout(new BoxLayout(demoBox, BoxLayout.Y_AXIS));
        demoBox.setBackground(new Color(248, 250, 252));
        demoBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Quick Demo Login"),
                new EmptyBorder(8, 8, 8, 8)
        ));

        JPanel demoButtons1 = new JPanel(new GridLayout(2, 2, 8, 6));
        demoButtons1.setOpaque(false);

        JButton btnDemoLeader = new JButton("Himanshu (Leader / User)");
        btnDemoLeader.setFont(UITheme.FONT_SMALL);
        btnDemoLeader.addActionListener(e -> setCredentials("himanshu", "user123"));

        JButton btnDemoAgent1 = new JButton("Shubham (H/W Agent)");
        btnDemoAgent1.setFont(UITheme.FONT_SMALL);
        btnDemoAgent1.addActionListener(e -> setCredentials("shubham", "agent123"));

        JButton btnDemoAgent2 = new JButton("Sameer (S/W Agent)");
        btnDemoAgent2.setFont(UITheme.FONT_SMALL);
        btnDemoAgent2.addActionListener(e -> setCredentials("sameer", "agent123"));

        JButton btnDemoAdmin = new JButton("Administrator (Admin)");
        btnDemoAdmin.setFont(UITheme.FONT_SMALL);
        btnDemoAdmin.addActionListener(e -> setCredentials("admin", "admin123"));

        demoButtons1.add(btnDemoLeader);
        demoButtons1.add(btnDemoAgent1);
        demoButtons1.add(btnDemoAgent2);
        demoButtons1.add(btnDemoAdmin);

        demoBox.add(demoButtons1);

        JPanel centerContainer = new JPanel(new BorderLayout(0, 10));
        centerContainer.setBackground(UITheme.CARD_BG);
        centerContainer.add(centerBtnWrap, BorderLayout.NORTH);
        centerContainer.add(demoBox, BorderLayout.CENTER);

        card.add(centerContainer, BorderLayout.CENTER);

        return card;
    }

    private JPanel createRegisterPanel() {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(0, 10));

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));
        form.setBackground(UITheme.CARD_BG);

        txtRegFullName = new JTextField();
        txtRegUsername = new JTextField();
        txtRegPassword = new JPasswordField();
        txtRegEmail = new JTextField();
        cmbRegRole = new JComboBox<>(new String[]{"USER", "AGENT"});
        txtRegDeptOrPhone = new JTextField();
        lblRegDeptOrPhone = new JLabel("Phone Number:");

        cmbRegRole.addActionListener(e -> {
            String role = (String) cmbRegRole.getSelectedItem();
            if ("AGENT".equalsIgnoreCase(role)) {
                lblRegDeptOrPhone.setText("Department:");
            } else {
                lblRegDeptOrPhone.setText("Phone Number:");
            }
        });

        form.add(new JLabel("Full Name:"));
        form.add(txtRegFullName);
        form.add(new JLabel("Username:"));
        form.add(txtRegUsername);
        form.add(new JLabel("Password:"));
        form.add(txtRegPassword);
        form.add(new JLabel("Email Address:"));
        form.add(txtRegEmail);
        form.add(new JLabel("Role:"));
        form.add(cmbRegRole);
        form.add(lblRegDeptOrPhone);
        form.add(txtRegDeptOrPhone);

        card.add(form, BorderLayout.CENTER);

        JButton btnRegister = UITheme.createSuccessButton("Register Account");
        btnRegister.addActionListener(e -> handleRegister());

        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnWrap.setBackground(UITheme.CARD_BG);
        btnWrap.add(btnRegister);
        card.add(btnWrap, BorderLayout.SOUTH);

        return card;
    }

    private void setCredentials(String username, String password) {
        txtLoginUsername.setText(username);
        txtLoginPassword.setText(password);
        handleLogin();
    }

    private void handleLogin() {
        String username = txtLoginUsername.getText().trim();
        String password = new String(txtLoginPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Required Fields", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User user = userDAO.authenticate(username, password);
            if (user != null) {
                dispose(); // Close login window
                MainDashboard dashboard = new MainDashboard(user, userDAO, ticketDAO, feedbackDAO, escalationService);
                dashboard.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password.", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (HelpdeskException e) {
            JOptionPane.showMessageDialog(this, "Login error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRegister() {
        String fullName = txtRegFullName.getText().trim();
        String username = txtRegUsername.getText().trim();
        String password = new String(txtRegPassword.getPassword()).trim();
        String email = txtRegEmail.getText().trim();
        String role = (String) cmbRegRole.getSelectedItem();
        String extra = txtRegDeptOrPhone.getText().trim();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required registration fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User newUser;
            if ("AGENT".equalsIgnoreCase(role)) {
                newUser = new Agent(username, password, fullName, email, extra.isEmpty() ? "IT Support" : extra);
            } else {
                newUser = new Customer(username, password, fullName, email, extra);
            }

            boolean ok = userDAO.registerUser(newUser);
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "Registration successful for " + fullName + "!\nPlease switch to the Sign In tab and log in.",
                        "Account Created", JOptionPane.INFORMATION_MESSAGE);
                txtRegFullName.setText("");
                txtRegUsername.setText("");
                txtRegPassword.setText("");
                txtRegEmail.setText("");
                txtRegDeptOrPhone.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Registration failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (HelpdeskException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
