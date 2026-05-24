package edu.ucd.comp2013j.lab.dao;

import edu.ucd.comp2013j.lab.db.Database;
import edu.ucd.comp2013j.lab.db.Db;
import edu.ucd.comp2013j.lab.model.MaintenanceTicket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class MaintenanceDao {
    private final Database database;

    public MaintenanceDao(Database database) {
        this.database = database;
    }

    public List<MaintenanceTicket> findAll() {
        String sql = """
                SELECT mt.ticket_id, e.asset_tag, e.equipment_name, reporter.full_name AS reporter_name,
                       tech.full_name AS technician_name, mt.title, mt.priority, mt.status, mt.reported_at
                FROM maintenance_tickets mt
                JOIN equipment e ON mt.equipment_id = e.equipment_id
                JOIN users reporter ON mt.reporter_id = reporter.user_id
                LEFT JOIN users tech ON mt.technician_id = tech.user_id
                ORDER BY mt.reported_at DESC
                """;
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<MaintenanceTicket> tickets = new ArrayList<>();
            while (rs.next()) {
                tickets.add(mapTicket(rs));
            }
            return tickets;
        } catch (SQLException ex) {
            throw Db.fail(ex);
        }
    }

    public boolean hasActiveTicketForEquipment(int equipmentId) {
        String sql = """
                SELECT COUNT(*) AS active_count
                FROM maintenance_tickets
                WHERE equipment_id = ? AND status IN ('OPEN', 'ASSIGNED', 'IN_PROGRESS')
                """;
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, equipmentId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("active_count") > 0;
            }
        } catch (SQLException ex) {
            throw Db.fail(ex);
        }
    }

    public int create(Connection connection, int equipmentId, int reporterId, String title,
                      String description, String priority) throws SQLException {
        String sql = """
                INSERT INTO maintenance_tickets
                    (equipment_id, reporter_id, title, description, priority, status)
                VALUES (?, ?, ?, ?, ?, 'OPEN')
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, equipmentId);
            ps.setInt(2, reporterId);
            ps.setString(3, title);
            ps.setString(4, description);
            ps.setString(5, priority);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Ticket id was not generated");
    }

    public void assign(Connection connection, int ticketId, Integer technicianId, String status, int userId, String note)
            throws SQLException {
        // Updating the ticket and inserting an update note keeps an audit trail for the repair.
        String sql = """
                UPDATE maintenance_tickets
                SET technician_id = ?, status = ?,
                    resolved_at = CASE WHEN ? IN ('RESOLVED', 'CLOSED') THEN CURRENT_TIMESTAMP ELSE resolved_at END
                WHERE ticket_id = ?
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (technicianId == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, technicianId);
            }
            ps.setString(2, status);
            ps.setString(3, status);
            ps.setInt(4, ticketId);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO maintenance_updates (ticket_id, user_id, update_text) VALUES (?, ?, ?)")) {
            ps.setInt(1, ticketId);
            ps.setInt(2, userId);
            ps.setString(3, note);
            ps.executeUpdate();
        }
    }

    private MaintenanceTicket mapTicket(ResultSet rs) throws SQLException {
        String technician = rs.getString("technician_name");
        if (technician == null) {
            technician = "Not assigned";
        }
        return new MaintenanceTicket(
                rs.getInt("ticket_id"),
                rs.getString("asset_tag"),
                rs.getString("equipment_name"),
                rs.getString("reporter_name"),
                technician,
                rs.getString("title"),
                rs.getString("priority"),
                rs.getString("status"),
                rs.getTimestamp("reported_at").toLocalDateTime()
        );
    }
}
