package edu.ucd.comp2013j.lab.service;

import edu.ucd.comp2013j.lab.dao.ReservationDao;
import edu.ucd.comp2013j.lab.db.Database;
import edu.ucd.comp2013j.lab.db.Db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReservationService {
    public static final List<FixedSlot> FIXED_SLOTS = List.of(
            new FixedSlot("MORNING", LocalTime.of(8, 0), LocalTime.of(14, 0), false),
            new FixedSlot("AFTERNOON", LocalTime.of(14, 0), LocalTime.of(20, 0), false),
            new FixedSlot("NIGHT", LocalTime.of(20, 0), LocalTime.of(8, 0), true)
    );

    private final Database database;
    private final ReservationDao reservationDao;

    public ReservationService(Database database, ReservationDao reservationDao) {
        this.database = database;
        this.reservationDao = reservationDao;
    }

    public int requestReservation(List<Integer> equipmentIds, int requesterId, LocalDateTime start,
                                  LocalDateTime end, String purpose,
                                  Map<Integer, Integer> consumableRequests) {
        validateTime(start, end);
        validateFixedSlot(start, end);
        int equipmentId = cleanSingleEquipmentId(equipmentIds);
        if (purpose == null || purpose.isBlank()) {
            throw new IllegalArgumentException("Purpose is required");
        }

        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                // This transaction is the important part: check availability and insert as one unit.
                checkEquipmentCanBeReserved(connection, equipmentId, requesterId);
                if (reservationDao.hasTimeConflict(connection, equipmentId, start, end, null)) {
                    throw new IllegalArgumentException("This time slot is already booked.");
                }
                int id = reservationDao.create(connection, equipmentId, requesterId, start, end, purpose.trim(),
                        consumableRequests);
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

    public List<Map<String, ?>> fixedSlotAvailability(int equipmentId, int requesterId, LocalDate startDate, int days) {
        LocalDate firstDate = startDate == null ? LocalDate.now() : startDate;
        int safeDays = Math.max(1, Math.min(days, 21));
        try (Connection connection = database.getConnection()) {
            EquipmentAccess equipmentAccess = equipmentAccess(connection, equipmentId, false);
            boolean labAllowed = canUserAccessLab(connection, requesterId, equipmentAccess.labId());
            List<Map<String, ?>> rows = new ArrayList<>();
            for (int dayIndex = 0; dayIndex < safeDays; dayIndex++) {
                LocalDate date = firstDate.plusDays(dayIndex);
                for (FixedSlot slot : FIXED_SLOTS) {
                    LocalDateTime start = date.atTime(slot.startTime());
                    LocalDateTime end = slot.endsNextDay()
                            ? date.plusDays(1).atTime(slot.endTime())
                            : date.atTime(slot.endTime());
                    boolean past = !start.isAfter(LocalDateTime.now());
                    boolean equipmentBookable = labAllowed
                            && ("AVAILABLE".equals(equipmentAccess.status()) || "RESERVED".equals(equipmentAccess.status()));
                    boolean conflict = equipmentBookable && !past
                            && reservationDao.hasTimeConflict(connection, equipmentId, start, end, null);
                    boolean available = equipmentBookable && !past && !conflict;
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("date", date.toString());
                    row.put("slot", slot.key());
                    row.put("startTime", formatTime(start));
                    row.put("endTime", formatTime(end));
                    row.put("available", available);
                    row.put("reason", availabilityReason(equipmentAccess.status(), labAllowed, past, conflict));
                    rows.add(row);
                }
            }
            return rows;
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

    private void validateFixedSlot(LocalDateTime start, LocalDateTime end) {
        for (FixedSlot slot : FIXED_SLOTS) {
            LocalDateTime expectedStart = start.toLocalDate().atTime(slot.startTime());
            LocalDateTime expectedEnd = slot.endsNextDay()
                    ? start.toLocalDate().plusDays(1).atTime(slot.endTime())
                    : start.toLocalDate().atTime(slot.endTime());
            if (start.equals(expectedStart) && end.equals(expectedEnd)) {
                return;
            }
        }
        throw new IllegalArgumentException("Reservations must use one fixed time slot: 08:00-14:00, 14:00-20:00, or 20:00-08:00.");
    }

    private int cleanSingleEquipmentId(List<Integer> equipmentIds) {
        if (equipmentIds == null || equipmentIds.isEmpty()) {
            throw new IllegalArgumentException("At least one equipment item is required");
        }
        Set<Integer> unique = new LinkedHashSet<>();
        for (Integer id : equipmentIds) {
            if (id != null && id > 0) {
                unique.add(id);
            }
        }
        if (unique.isEmpty()) {
            throw new IllegalArgumentException("At least one equipment item is required");
        }
        if (unique.size() > 1) {
            throw new IllegalArgumentException("Only one equipment item can be reserved at a time.");
        }
        return unique.iterator().next();
    }

    private void checkEquipmentCanBeReserved(Connection connection, int equipmentId, int requesterId) throws SQLException {
        EquipmentAccess access = equipmentAccess(connection, equipmentId, true);
        if (!canUserAccessLab(connection, requesterId, access.labId())) {
            throw new IllegalArgumentException("Student can only reserve equipment from linked labs.");
        }
        if (!"AVAILABLE".equals(access.status()) && !"RESERVED".equals(access.status())) {
            throw new IllegalArgumentException("Equipment status is " + access.status() + ", so it cannot be reserved.");
        }
    }

    private EquipmentAccess equipmentAccess(Connection connection, int equipmentId, boolean lock) throws SQLException {
        String sql = "SELECT status, lab_id FROM equipment WHERE equipment_id = ?" + (lock ? " FOR UPDATE" : "");
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, equipmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Equipment does not exist");
                }
                return new EquipmentAccess(rs.getString("status"), rs.getInt("lab_id"));
            }
        }
    }

    private boolean canUserAccessLab(Connection connection, int userId, int labId) throws SQLException {
        String roleSql = "SELECT role FROM users WHERE user_id = ? AND active = TRUE";
        try (PreparedStatement ps = connection.prepareStatement(roleSql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Unknown user id: " + userId);
                }
                if (!"STUDENT".equals(rs.getString("role"))) {
                    throw new IllegalArgumentException("Only students can create reservations.");
                }
            }
        }
        String labSql = "SELECT COUNT(*) AS total FROM student_labs WHERE user_id = ? AND lab_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(labSql)) {
            ps.setInt(1, userId);
            ps.setInt(2, labId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("total") > 0;
            }
        }
    }

    private String availabilityReason(String equipmentStatus, boolean labAllowed, boolean past, boolean conflict) {
        if (!labAllowed) {
            return "NO_LAB_ACCESS";
        }
        if (!"AVAILABLE".equals(equipmentStatus) && !"RESERVED".equals(equipmentStatus)) {
            return equipmentStatus;
        }
        if (past) {
            return "PAST";
        }
        if (conflict) {
            return "BOOKED";
        }
        return "";
    }

    private String formatTime(LocalDateTime value) {
        return value.toString().replace('T', ' ');
    }

    public record FixedSlot(String key, LocalTime startTime, LocalTime endTime, boolean endsNextDay) {
    }

    private record EquipmentAccess(String status, int labId) {
    }
}
