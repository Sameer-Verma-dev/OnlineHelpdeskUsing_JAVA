package com.helpdesk.ui;

import com.helpdesk.model.Priority;
import com.helpdesk.model.Status;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Modern UI Theme styling, color palette, and component helpers for Swing dashboard.
 */
public class UITheme {
    // Primary Colors
    public static final Color PRIMARY = new Color(30, 58, 138);       // Deep Navy
    public static final Color PRIMARY_LIGHT = new Color(59, 130, 246); // Royal Blue
    public static final Color ACCENT = new Color(14, 165, 233);        // Sky Blue
    public static final Color BACKGROUND = new Color(248, 250, 252);   // Slate 50
    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT_PRIMARY = new Color(15, 23, 42);    // Slate 900
    public static final Color TEXT_MUTED = new Color(100, 116, 139);   // Slate 500
    public static final Color BORDER = new Color(226, 232, 240);       // Slate 200

    // Status Colors
    public static final Color STATUS_OPEN = new Color(59, 130, 246);       // Blue
    public static final Color STATUS_ASSIGNED = new Color(14, 165, 233);   // Cyan
    public static final Color STATUS_IN_PROGRESS = new Color(245, 158, 11);// Amber
    public static final Color STATUS_RESOLVED = new Color(16, 185, 129);   // Emerald
    public static final Color STATUS_CLOSED = new Color(100, 116, 139);    // Slate

    // Priority Colors
    public static final Color PRIORITY_LOW = new Color(100, 116, 139);
    public static final Color PRIORITY_MEDIUM = new Color(217, 119, 6);
    public static final Color PRIORITY_HIGH = new Color(234, 88, 12);
    public static final Color PRIORITY_CRITICAL = new Color(220, 38, 38);

    // Fonts
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY.darker(), 1),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createSuccessButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(16, 185, 129));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(5, 150, 105), 1),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(PRIORITY_CRITICAL);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIORITY_CRITICAL.darker(), 1),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(16, 16, 16, 16)
        ));
        return panel;
    }

    public static DefaultTableCellRenderer createStatusRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setHorizontalAlignment(SwingConstants.CENTER);
                c.setFont(FONT_BOLD);
                if (value instanceof Status) {
                    Status s = (Status) value;
                    switch (s) {
                        case OPEN: c.setForeground(STATUS_OPEN); break;
                        case ASSIGNED: c.setForeground(STATUS_ASSIGNED); break;
                        case IN_PROGRESS: c.setForeground(STATUS_IN_PROGRESS); break;
                        case RESOLVED: c.setForeground(STATUS_RESOLVED); break;
                        case CLOSED: c.setForeground(STATUS_CLOSED); break;
                    }
                }
                return c;
            }
        };
    }

    public static DefaultTableCellRenderer createPriorityRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setHorizontalAlignment(SwingConstants.CENTER);
                c.setFont(FONT_BOLD);
                if (value instanceof Priority) {
                    Priority p = (Priority) value;
                    switch (p) {
                        case LOW: c.setForeground(PRIORITY_LOW); break;
                        case MEDIUM: c.setForeground(PRIORITY_MEDIUM); break;
                        case HIGH: c.setForeground(PRIORITY_HIGH); break;
                        case CRITICAL: c.setForeground(PRIORITY_CRITICAL); break;
                    }
                }
                return c;
            }
        };
    }
}
