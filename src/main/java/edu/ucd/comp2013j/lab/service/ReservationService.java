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

    public int requestReservation(List<Integer> equipmentIds, int requesterId, Integer courseId,
                                  LocalDateTime start, LocalDateTime end, String purpose,
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
                checkEquipmentCanBeReserved(connection, equipmentId);
                if (reservationDao.hasTimeConflict(connection, equipmentId, start, end, null)) {
                    throw new IllegalArgumentException("This time slot is already booked.");
                }
                int id = reservationDao.create(connection, equipmentId, requesterId, courseId, start, end,
                        purpose.trim(), consumableRequests);
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

    public List<Map<String, ?>> fixedSlotAvailability(int equipmentId, LocalDate startDate, int days) {
        LocalDate firstDate = startDate == null ? LocalDate.now() : startDate;
        int safeDays = Math.max(1, Math.min(days, 21));
        try (Connection connection = database.getConnection()) {
            String equipmentStatus = equipmentStatus(connection, equipmentId);
            List<Map<String, ?>> rows = new ArrayList<>();
            for (int dayIndex = 0; dayIndex < safeDays; dayIndex++) {
                LocalDate date = firstDate.plusDays(dayIndex);
                for (FixedSlot slot : FIXED_SLOTS) {
                    LocalDateTime start = date.atTime(slot.startTime());
                    LocalDateTime end = slot.endsNextDay()
                            ? date.plusDays(1).atTime(slot.endTime())
                            : date.atTime(slot.endTime());
                    boolean past = !start.isAfter(LocalDateTime.now());
                    boolean equipmentBookable = "AVAILABLE".equals(equipmentStatus) || "RESERVED".equals(equipmentStatus);
                    boolean conflict = equipmentBookable && !past
                            && reservationDao.hasTimeConflict(connection, equipmentId, start, end, null);
                    boolean available = equipmentBookable && !past && !conflict;
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("date", date.toString());
                    row.put("slot", slot.key());
                    row.put("startTime", formatTime(start));
                    row.put("endTime", formatTime(end));
                    row.put("available", available);
                    row.put("reason", availabilityReason(equipmentStatus, equipmentBookable, past, conflict));
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

    private void checkEquipmentCanBeReserved(Connection connection, int equipmentId) throws SQLException {
        String status = equipmentStatus(connection, equipmentId, true);
        if (!"AVAILABLE".equals(status) && !"RESERVED".equals(status)) {
            throw new IllegalArgumentException("Equipment status is " + status + ", so it cannot be reserved.");
        }
    }

    private String equipmentStatus(Connection connection, int equipmentId) throws SQLException {
        return equipmentStatus(connection, equipmentId, false);
    }

    private String equipmentStatus(Connection connection, int equipmentId, boolean lock) throws SQLException {
        String sql = "SELECT status FROM equipment WHERE equipment_id = ?" + (lock ? " FOR UPDATE" : "");
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, equipmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Equipment does not exist");
                }
                return rs.getString("status");
            }
        }
    }

    private String availabilityReason(String equipmentStatus, boolean equipmentBookable, boolean past, boolean conflict) {
        if (!equipmentBookable) {
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
}
