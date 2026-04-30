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

    public void updateTicket(int ticketId, Integer technicianId, String status, int userId, String note) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status is required");
        }
        if (note == null || note.isBlank()) {
            note = "Status changed to " + status;
        }

        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                maintenanceDao.assign(connection, ticketId, technicianId, status, userId, note.trim());
                if ("RESOLVED".equals(status) || "CLOSED".equals(status)) {
                    try (PreparedStatement ps = connection.prepareStatement("""
                            UPDATE equipment
                            SET status = 'AVAILABLE'
                            WHERE equipment_id = (
                                SELECT equipment_id FROM maintenance_tickets WHERE ticket_id = ?
                            )
                            """)) {
                        ps.setInt(1, ticketId);
                        ps.executeUpdate();
                    }
                }
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
}
