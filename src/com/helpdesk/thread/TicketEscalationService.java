package com.helpdesk.thread;

import com.helpdesk.dao.TicketDAO;
import com.helpdesk.model.Priority;
import com.helpdesk.model.Ticket;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Automated Ticket Escalation Service (Multithreading)
 * Background thread running periodically to monitor unresolved tickets.
 * Automatically escalates tickets exceeding SLA thresholds and notifies listeners.
 */
public class TicketEscalationService implements Runnable {

    public interface EscalationListener {
        void onTicketEscalated(Ticket ticket, Priority oldPriority, Priority newPriority, String reason);
        void onScanCompleted(int scannedCount, int escalatedCount);
    }

    private static TicketEscalationService instance;
    private final TicketDAO ticketDAO;
    private Thread workerThread;
    private volatile boolean running = false;
    private long checkIntervalMillis = 15000; // Check every 15 seconds for demonstration
    private final List<EscalationListener> listeners = new CopyOnWriteArrayList<>();
    private final List<String> escalationLogs = new CopyOnWriteArrayList<>();
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private TicketEscalationService(TicketDAO ticketDAO) {
        this.ticketDAO = ticketDAO;
    }

    public static synchronized TicketEscalationService getInstance(TicketDAO ticketDAO) {
        if (instance == null) {
            instance = new TicketEscalationService(ticketDAO);
        }
        return instance;
    }

    public void addListener(EscalationListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(EscalationListener listener) {
        listeners.remove(listener);
    }

    public synchronized void start() {
        if (!running) {
            running = true;
            workerThread = new Thread(this, "TicketEscalationMonitorThread");
            workerThread.setDaemon(true); // Daemon thread so JVM exits cleanly
            workerThread.start();
            log("Escalation Monitor Thread started (Interval: " + (checkIntervalMillis / 1000) + "s).");
        }
    }

    public synchronized void stop() {
        running = false;
        if (workerThread != null) {
            workerThread.interrupt();
            log("Escalation Monitor Thread stopped.");
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void setCheckIntervalSeconds(int seconds) {
        this.checkIntervalMillis = Math.max(5, seconds) * 1000L;
        log("Check interval updated to " + seconds + " seconds.");
    }

    public List<String> getEscalationLogs() {
        return new ArrayList<>(escalationLogs);
    }

    private void log(String message) {
        String entry = "[" + LocalDateTime.now().format(TIME_FMT) + "] " + message;
        escalationLogs.add(entry);
        if (escalationLogs.size() > 100) {
            escalationLogs.remove(0);
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                performScan();
                Thread.sleep(checkIntervalMillis);
            } catch (InterruptedException e) {
                // Thread interrupted on stop
                break;
            } catch (Exception e) {
                log("Error during escalation scan: " + e.getMessage());
            }
        }
    }

    /**
     * Scans active tickets and escalates priority if pending.
     */
    public synchronized void performScan() {
        try {
            List<Ticket> activeTickets = ticketDAO.getUnresolvedTickets();
            int escalatedCount = 0;
            LocalDateTime now = LocalDateTime.now();

            for (Ticket ticket : activeTickets) {
                // Determine if ticket should be escalated
                // For demo/classroom: tickets older than 1 minute or non-escalated tickets with HIGH/MEDIUM priority
                // In SLA terms: if SLA is exceeded or ticket has been pending for over 2 minutes without resolution
                long secondsPending = java.time.Duration.between(ticket.getCreatedAt(), now).getSeconds();
                
                // If ticket is not yet escalated, or has been pending for more than 45 seconds in demo
                if (!ticket.isEscalated() && secondsPending >= 45 && ticket.getPriority() != Priority.CRITICAL) {
                    Priority oldPriority = ticket.getPriority();
                    Priority newPriority = oldPriority.nextEscalationLevel();
                    String reason = "Pending resolution for " + secondsPending + "s (SLA warning threshold reached).";

                    boolean updated = ticketDAO.escalateTicket(ticket.getId(), newPriority, reason);
                    if (updated) {
                        ticket.setPriority(newPriority);
                        ticket.setEscalated(true);
                        escalatedCount++;
                        String logMsg = "ESCALATED: " + ticket.getTicketCode() + " [" + oldPriority + " -> " + newPriority + "] Reason: " + reason;
                        log(logMsg);

                        for (EscalationListener listener : listeners) {
                            try {
                                listener.onTicketEscalated(ticket, oldPriority, newPriority, reason);
                            } catch (Exception ex) {
                                // Ignore listener errors
                            }
                        }
                    }
                }
            }

            for (EscalationListener listener : listeners) {
                try {
                    listener.onScanCompleted(activeTickets.size(), escalatedCount);
                } catch (Exception ex) {
                    // Ignore
                }
            }

        } catch (Exception e) {
            log("Escalation check error: " + e.getMessage());
        }
    }
}
