-- =====================================================================
-- CSE2003: Object Oriented Programming using JAVA - Mini Project
-- Group 11: Online Complaint and Helpdesk System
-- Team: Himanshu (Leader), Shubham, Sameer, Khushveer
-- Database Schema for Users, Tickets, and Feedback
-- =====================================================================

-- Table 1: users
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL, -- 'USER', 'AGENT', 'ADMIN'
    department_or_phone VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table 2: tickets
CREATE TABLE IF NOT EXISTS tickets (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ticket_code VARCHAR(20) UNIQUE NOT NULL,
    user_id INTEGER NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(30) NOT NULL, -- 'HARDWARE', 'SOFTWARE', 'SERVICE', 'GENERAL'
    priority VARCHAR(20) NOT NULL, -- 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'
    status VARCHAR(20) NOT NULL,   -- 'OPEN', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'
    assigned_agent_id INTEGER,
    assigned_agent_name VARCHAR(100),
    specific_detail_1 VARCHAR(100),
    specific_detail_2 VARCHAR(100),
    specific_detail_3 VARCHAR(100),
    escalated INTEGER DEFAULT 0,
    resolution_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (assigned_agent_id) REFERENCES users(id)
);

-- Table 3: feedback
CREATE TABLE IF NOT EXISTS feedback (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ticket_id INTEGER NOT NULL,
    ticket_code VARCHAR(20) NOT NULL,
    user_id INTEGER NOT NULL,
    user_name VARCHAR(100) NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comments TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ticket_id) REFERENCES tickets(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
