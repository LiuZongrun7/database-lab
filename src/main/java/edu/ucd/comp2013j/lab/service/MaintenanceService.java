package edu.ucd.comp2013j.lab.service;

import edu.ucd.comp2013j.lab.dao.MaintenanceDao;
import edu.ucd.comp2013j.lab.db.Database;
import edu.ucd.comp2013j.lab.db.Db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MaintenanceService {
    private final Database database;
    private final MaintenanceDao maintenanceDao;

    public MaintenanceService(Database database, MaintenanceDao maintenanceDao) {
        this.database = database;
        this.maintenanceDao = maintenanceDao;
    }

    public int reportProblem(int equipmentId, int reporterId, String title, String description, String priority) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Ticket title is required");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }

        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int ticketId = maintenanceDao.create(connection, equipmentId, reporterId, title.trim(), description.trim(), priority);
                // When a fault is reported, the equipment is removed from new booking choices.
                try (PreparedStatement ps = connection.prepareStatement(
                        "UPDATE equipment SET status = 'MAINTENANCE' WHERE equipment_id = ?")) {
                    ps.setInt(1, equipmentId);
                    ps.executeUpdate();
                }
                connection.commit();
                return ticketId;
            } catch (RuntimeException | SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw Db.fail(ex);
        }
    }

    public void handleTicketAction(int ticketId, String action, int userId, boolean admin) {
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Maintenance action is required");
        }
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                TicketState ticket = findTicketState(connection, ticketId);
                applyAction(connection, ticketId, ticket, action.trim(), userId, admin);
                connection.commit();
            } catch (RuntimeException | SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw Db.fail(ex);
        }
    }

    private void applyAction(Connection connection, int ticketId, TicketState ticket, String action, int userId, boolean admin)
            throws SQLException {
        if ("ACCEPT".equals(action)) {
            if (!"OPEN".equals(ticket.status())) {
                throw new IllegalArgumentException("Only open tickets can be accepted");
            }
            maintenanceDao.assign(connection, ticketId, userId, "IN_PROGRESS", userId, "Ticket accepted.");
            setEquipmentStatus(connection, ticket.equipmentId(), "MAINTENANCE");
            return;
        }
        if ("RESOLVE".equals(action)) {
            if (!"IN_PROGRESS".equals(ticket.status())) {
                throw new IllegalArgumentException("Only in-progress tickets can be marked as repaired");
            }
            if (!admin && ticket.technicianId() != userId) {
                throw new IllegalArgumentException("Only the assigned technician or an admin can mark this ticket repaired");
            }
            maintenanceDao.assign(connection, ticketId, userId, "RESOLVED", userId, "Repair finished.");
            if (!hasActiveTickets(connection, ticket.equipmentId())) {
                // A device can return to booking only when all its open repair tickets are finished.
                setEquipmentStatus(connection, ticket.equipmentId(), "AVAILABLE");
            }
            return;
        }
        throw new IllegalArgumentException("Unknown maintenance action");
    }

    private TicketState findTicketState(Connection connection, int ticketId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT equipment_id, technician_id, status FROM maintenance_tickets WHERE ticket_id = ?")) {
            ps.setInt(1, ticketId);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new TicketState(
                            rs.getInt("equipment_id"),
                            rs.getInt("technician_id"),
                            rs.getString("status")
                    );
                }
            }
        }
        throw new IllegalArgumentException("Ticket does not exist");
    }

    private boolean hasActiveTickets(Connection connection, int equipmentId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("""
                SELECT COUNT(*) AS active_count
                FROM maintenance_tickets
                WHERE equipment_id = ? AND status IN ('OPEN', 'IN_PROGRESS')
                """)) {
            ps.setInt(1, equipmentId);
            try (var rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("active_count") > 0;
            }
        }
    }

    private void setEquipmentStatus(Connection connection, int equipmentId, String status) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE equipment SET status = ? WHERE equipment_id = ? AND status <> 'RETIRED'")) {
            ps.setString(1, status);
            ps.setInt(2, equipmentId);
            ps.executeUpdate();
        }
    }

    private record TicketState(int equipmentId, int technicianId, String status) {
    }
}
