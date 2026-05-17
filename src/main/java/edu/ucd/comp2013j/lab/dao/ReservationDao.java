package edu.ucd.comp2013j.lab.dao;

import edu.ucd.comp2013j.lab.db.Database;
import edu.ucd.comp2013j.lab.db.Db;
import edu.ucd.comp2013j.lab.model.Reservation;
import edu.ucd.comp2013j.lab.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReservationDao {
    private final Database database;

    public ReservationDao(Database database) {
        this.database = database;
    }

    public boolean hasTimeConflict(Connection connection, int equipmentId, LocalDateTime start, LocalDateTime end,
                                   Integer exceptReservationId) throws SQLException {
        // This overlap test catches partial overlaps and fully contained bookings.
        String sql = """
                SELECT COUNT(*) AS total
                FROM reservations r
                JOIN reservation_equipment re ON r.reservation_id = re.reservation_id
                WHERE re.equipment_id = ?
                  AND r.status IN ('PENDING', 'APPROVED')
                  AND r.start_time < ?
                  AND r.end_time > ?
                """;
        if (exceptReservationId != null) {
            sql += " AND r.reservation_id <> ?";
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, equipmentId);
            ps.setTimestamp(2, Timestamp.valueOf(end));
            ps.setTimestamp(3, Timestamp.valueOf(start));
            if (exceptReservationId != null) {
                ps.setInt(4, exceptReservationId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("total") > 0;
            }
        }
    }

    public int create(Connection connection, List<Integer> equipmentIds, int requesterId, Integer courseId,
                      LocalDateTime start, LocalDateTime end, String purpose,
                      Map<Integer, Integer> consumableRequests) throws SQLException {
        String sql = """
                INSERT INTO reservations (requester_id, course_id, start_time, end_time, purpose, status)
                VALUES (?, ?, ?, ?, ?, 'PENDING')
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, requesterId);
            if (courseId == null) {
                ps.setNull(2, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, courseId);
            }
            ps.setTimestamp(3, Timestamp.valueOf(start));
            ps.setTimestamp(4, Timestamp.valueOf(end));
            ps.setString(5, purpose);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int reservationId = keys.getInt(1);
                    // A reservation can contain many equipment items and optional consumable needs.
                    insertEquipment(connection, reservationId, equipmentIds);
                    insertConsumableRequests(connection, reservationId, consumableRequests);
                    return reservationId;
                }
            }
        }
        throw new SQLException("Reservation id was not generated");
    }

    public List<Reservation> findVisibleFor(User user) {
        String sql = """
                SELECT reservation_id, asset_tags, equipment_names, full_name, start_time, end_time,
                       purpose, consumable_needs, status
                FROM v_user_reservation_history
                """;
        if (!user.isAdmin() && !user.isTeacher()) {
            sql += " WHERE requester_id = ?";
        }
        sql += " ORDER BY start_time DESC";

        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            if (!user.isAdmin() && !user.isTeacher()) {
                ps.setInt(1, user.getId());
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Reservation> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(mapReservation(rs));
                }
                return rows;
            }
        } catch (SQLException ex) {
            throw Db.fail(ex);
        }
    }

    public void decide(Connection connection, int reservationId, int approverId, boolean approve, String comment)
            throws SQLException {
        String status = approve ? "APPROVED" : "REJECTED";
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE reservations SET status = ? WHERE reservation_id = ? AND status = 'PENDING'")) {
            ps.setString(1, status);
            ps.setInt(2, reservationId);
            int changed = ps.executeUpdate();
            if (changed != 1) {
                throw new SQLException("Only pending reservations can be decided");
            }
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO approvals (reservation_id, approver_id, decision, comment) VALUES (?, ?, ?, ?)")) {
            ps.setInt(1, reservationId);
            ps.setInt(2, approverId);
            ps.setString(3, status);
            ps.setString(4, comment);
            ps.executeUpdate();
        }
    }

    public void cancel(int reservationId, int requesterId) {
        String sql = """
                UPDATE reservations
                SET status = 'CANCELLED'
                WHERE reservation_id = ? AND requester_id = ? AND status IN ('PENDING', 'APPROVED')
                """;
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            ps.setInt(2, requesterId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw Db.fail(ex);
        }
    }

    private void insertEquipment(Connection connection, int reservationId, List<Integer> equipmentIds)
            throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO reservation_equipment (reservation_id, equipment_id) VALUES (?, ?)")) {
            for (int equipmentId : equipmentIds) {
                ps.setInt(1, reservationId);
                ps.setInt(2, equipmentId);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void insertConsumableRequests(Connection connection, int reservationId,
                                          Map<Integer, Integer> consumableRequests) throws SQLException {
        if (consumableRequests == null || consumableRequests.isEmpty()) {
            return;
        }
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO reservation_consumables (reservation_id, consumable_id, requested_quantity) VALUES (?, ?, ?)")) {
            for (Map.Entry<Integer, Integer> entry : consumableRequests.entrySet()) {
                ps.setInt(1, reservationId);
                ps.setInt(2, entry.getKey());
                ps.setInt(3, entry.getValue());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private Reservation mapReservation(ResultSet rs) throws SQLException {
        return new Reservation(
                rs.getInt("reservation_id"),
                rs.getString("asset_tags"),
                rs.getString("equipment_names"),
                rs.getString("full_name"),
                rs.getTimestamp("start_time").toLocalDateTime(),
                rs.getTimestamp("end_time").toLocalDateTime(),
                rs.getString("purpose"),
                rs.getString("consumable_needs"),
                rs.getString("status")
        );
    }
}
