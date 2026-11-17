package com.helpdesk.ui;

import com.helpdesk.model.Priority;
import com.helpdesk.model.Ticket;
import com.helpdesk.thread.TicketEscalationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Automated Ticket Escalation Service Monitor
 * Interactive UI monitor that visualizes the background daemon thread.
 */
public class EscalationMonitorPanel extends JPanel implements TicketEscalationService.EscalationListener {
    private final TicketEscalationService escalationService;
    private final Runnable onTicketEscalatedCallback;

    private JLabel lblThreadStatus;
    private JLabel lblLastScanStats;
    private JTextArea txtLogs;
    private JSpinner spinInterval;
    private JButton btnToggleThread;

    public EscalationMonitorPanel(TicketEscalationService escalationService, Runnable onTicketEscalatedCallback) {
        this.escalationService = escalationService;
        this.onTicketEscalatedCallback = onTicketEscalatedCallback;

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BACKGROUND);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        initUI();
        escalationService.addListener(this);
        updateStatus();
    }

    private void initUI() {
        // Top Control Header
        JPanel controlCard = UITheme.createCardPanel();
        controlCard.setLayout(new BorderLayout(15, 10));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 4));
        titlePanel.setBackground(UITheme.CARD_BG);
        JLabel lblTitle = new JLabel("Automated Ticket Escalation & SLA Monitor");
        lblTitle.setFont(UITheme.FONT_HEADER);
        lblTitle.setForeground(UITheme.PRIMARY);

        JLabel lblSub = new JLabel("Runs a background daemon thread that periodically audits pending tickets against SLA thresholds and escalates priority.");
        lblSub.setFont(UITheme.FONT_BODY);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        titlePanel.add(lblTitle);
        titlePanel.add(lblSub);
        controlCard.add(titlePanel, BorderLayout.NORTH);

        // Controls
        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        buttonBar.setBackground(UITheme.CARD_BG);

        lblThreadStatus = new JLabel("Status: RUNNING");
        lblThreadStatus.setFont(UITheme.FONT_BOLD);

        btnToggleThread = UITheme.createSecondaryButton("Pause Thread");
        btnToggleThread.addActionListener(e -> toggleThread());

        JButton btnScanNow = UITheme.createPrimaryButton("Run Immediate Scan");
        btnScanNow.addActionListener(e -> {
            new Thread(() -> escalationService.performScan()).start();
        });

        buttonBar.add(lblThreadStatus);
        buttonBar.add(btnToggleThread);
        buttonBar.add(btnScanNow);

        buttonBar.add(new JLabel("Scan Interval (sec):"));
        spinInterval = new JSpinner(new SpinnerNumberModel(15, 5, 120, 5));
        spinInterval.addChangeListener(e -> {
            int sec = (int) spinInterval.getValue();
            escalationService.setCheckIntervalSeconds(sec);
        });
        buttonBar.add(spinInterval);

        controlCard.add(buttonBar, BorderLayout.CENTER);

        lblLastScanStats = new JLabel("Last Scan: Monitoring active...");
        lblLastScanStats.setFont(UITheme.FONT_SMALL);
        lblLastScanStats.setForeground(UITheme.TEXT_MUTED);
        controlCard.add(lblLastScanStats, BorderLayout.SOUTH);

        add(controlCard, BorderLayout.NORTH);

        // Center Log Console
        JPanel logCard = UITheme.createCardPanel();
        logCard.setLayout(new BorderLayout(0, 8));

        JLabel lblLogTitle = new JLabel("Live Background Thread Activity Console");
        lblLogTitle.setFont(UITheme.FONT_SUBHEADER);
        lblLogTitle.setForeground(UITheme.PRIMARY);
        logCard.add(lblLogTitle, BorderLayout.NORTH);

        txtLogs = new JTextArea();
        txtLogs.setEditable(false);
        txtLogs.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtLogs.setBackground(new Color(15, 23, 42)); // Dark terminal background
        txtLogs.setForeground(new Color(52, 211, 153)); // Terminal green
        txtLogs.setCaretColor(Color.WHITE);

        JScrollPane scrollLogs = new JScrollPane(txtLogs);
        scrollLogs.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        logCard.add(scrollLogs, BorderLayout.CENTER);

        JPanel bottomBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomBtn.setBackground(UITheme.CARD_BG);
        JButton btnClearLog = UITheme.createSecondaryButton("Clear Console");
        btnClearLog.addActionListener(e -> txtLogs.setText(""));
        bottomBtn.add(btnClearLog);
        logCard.add(bottomBtn, BorderLayout.SOUTH);

        add(logCard, BorderLayout.CENTER);

        // Populate initial logs
        for (String log : escalationService.getEscalationLogs()) {
            txtLogs.append(log + "\n");
        }
    }

    private void toggleThread() {
        if (escalationService.isRunning()) {
            escalationService.stop();
        } else {
            escalationService.start();
        }
        updateStatus();
    }

    private void updateStatus() {
        boolean running = escalationService.isRunning();
        if (running) {
            lblThreadStatus.setText("Thread State: RUNNING (Active)");
            lblThreadStatus.setForeground(new Color(16, 185, 129));
            btnToggleThread.setText("Pause Thread");
        } else {
            lblThreadStatus.setText("Thread State: PAUSED (Inactive)");
            lblThreadStatus.setForeground(UITheme.PRIORITY_HIGH);
            btnToggleThread.setText("Resume Thread");
        }
    }

    @Override
    public void onTicketEscalated(Ticket ticket, Priority oldPriority, Priority newPriority, String reason) {
        SwingUtilities.invokeLater(() -> {
            String logEntry = "[ALERT] Ticket " + ticket.getTicketCode() + " auto-escalated from " + oldPriority + " to " + newPriority + " (" + reason + ")";
            txtLogs.append(logEntry + "\n");
            txtLogs.setCaretPosition(txtLogs.getDocument().getLength());

            if (onTicketEscalatedCallback != null) {
                onTicketEscalatedCallback.run();
            }
        });
    }

    @Override
    public void onScanCompleted(int scannedCount, int escalatedCount) {
        SwingUtilities.invokeLater(() -> {
            lblLastScanStats.setText("Last scan completed: " + scannedCount + " active tickets analyzed, " + escalatedCount + " tickets escalated.");
            updateStatus();
        });
    }
}
