package edu.ucd.comp2013j.lab.service;

import edu.ucd.comp2013j.lab.dao.ReservationDao;
import edu.ucd.comp2013j.lab.db.Database;
import edu.ucd.comp2013j.lab.db.Db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class ReservationService {
    private final Database database;
    private final ReservationDao reservationDao;

    public ReservationService(Database database, ReservationDao reservationDao) {
        this.database = database;
        this.reservationDao = reservationDao;
    }

    public int requestReservation(int equipmentId, int requesterId, Integer courseId,
                                  LocalDateTime start, LocalDateTime end, String purpose) {
        validateTime(start, end);
        if (purpose == null || purpose.isBlank()) {
            throw new IllegalArgumentException("Purpose is required");
        }

        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                // This transaction is the important part: check availability and insert as one unit.
                checkEquipmentCanBeReserved(connection, equipmentId);
                if (reservationDao.hasTimeConflict(connection, equipmentId, start, end, null)) {
                    throw new IllegalArgumentException("This equipment is already booked in the selected time.");
                }
                int id = reservationDao.create(connection, equipmentId, requesterId, courseId, start, end, purpose.trim());
                connection.commit();
                return id;
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

    public void decideReservation(int reservationId, int approverId, boolean approve, String comment) {
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                reservationDao.decide(connection, reservationId, approverId, approve, comment == null ? "" : comment.trim());
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

    private void validateTime(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start time and end time are required");
        }
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }

    private void checkEquipmentCanBeReserved(Connection connection, int equipmentId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT status FROM equipment WHERE equipment_id = ? FOR UPDATE")) {
            ps.setInt(1, equipmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Equipment does not exist");
                }
                String status = rs.getString("status");
                if (!"AVAILABLE".equals(status) && !"RESERVED".equals(status)) {
                    throw new IllegalArgumentException("Equipment status is " + status + ", so it cannot be reserved.");
                }
            }
        }
    }
}
