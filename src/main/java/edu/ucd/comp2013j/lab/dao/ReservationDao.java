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

public class ReservationDao {
    private final Database database;

    public ReservationDao(Database database) {
        this.database = database;
    }

    public boolean hasTimeConflict(Connection connection, int equipmentId, LocalDateTime start, LocalDateTime end,
                                   Integer exceptReservationId) throws SQLException {
        String sql = """
                SELECT COUNT(*) AS total
                FROM reservations
                WHERE equipment_id = ?
                  AND status IN ('PENDING', 'APPROVED')
                  AND start_time < ?
                  AND end_time > ?
                """;
        if (exceptReservationId != null) {
            sql += " AND reservation_id <> ?";
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

    public int create(Connection connection, int equipmentId, int requesterId, Integer courseId,
                      LocalDateTime start, LocalDateTime end, String purpose) throws SQLException {
        String sql = """
                INSERT INTO reservations (equipment_id, requester_id, course_id, start_time, end_time, purpose, status)
                VALUES (?, ?, ?, ?, ?, ?, 'PENDING')
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, equipmentId);
            ps.setInt(2, requesterId);
            if (courseId == null) {
                ps.setNull(3, java.sql.Types.INTEGER);
            } else {
                ps.setInt(3, courseId);
            }
            ps.setTimestamp(4, Timestamp.valueOf(start));
            ps.setTimestamp(5, Timestamp.valueOf(end));
            ps.setString(6, purpose);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Reservation id was not generated");
    }

    public List<Reservation> findVisibleFor(User user) {
        String sql = """
                SELECT r.reservation_id, e.asset_tag, e.equipment_name, u.full_name, r.start_time, r.end_time,
                       r.purpose, r.status
                FROM reservations r
                JOIN equipment e ON r.equipment_id = e.equipment_id
                JOIN users u ON r.requester_id = u.user_id
                """;
        if (!user.isAdmin() && !user.isTeacher()) {
            sql += " WHERE r.requester_id = ?";
        }
        sql += " ORDER BY r.start_time DESC";

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

    private Reservation mapReservation(ResultSet rs) throws SQLException {
        return new Reservation(
                rs.getInt("reservation_id"),
                rs.getString("asset_tag"),
                rs.getString("equipment_name"),
                rs.getString("full_name"),
                rs.getTimestamp("start_time").toLocalDateTime(),
                rs.getTimestamp("end_time").toLocalDateTime(),
                rs.getString("purpose"),
                rs.getString("status")
        );
    }
}
