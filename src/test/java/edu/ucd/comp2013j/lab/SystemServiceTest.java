package edu.ucd.comp2013j.lab;

import edu.ucd.comp2013j.lab.dao.EquipmentDao;
import edu.ucd.comp2013j.lab.dao.InventoryDao;
import edu.ucd.comp2013j.lab.dao.ReservationDao;
import edu.ucd.comp2013j.lab.dao.UserDao;
import edu.ucd.comp2013j.lab.db.Database;
import edu.ucd.comp2013j.lab.model.Consumable;
import edu.ucd.comp2013j.lab.model.Equipment;
import edu.ucd.comp2013j.lab.model.User;
import edu.ucd.comp2013j.lab.service.AuthService;
import edu.ucd.comp2013j.lab.service.InventoryService;
import edu.ucd.comp2013j.lab.service.MaintenanceService;
import edu.ucd.comp2013j.lab.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfEnvironmentVariable(named = "DB_PASSWORD", matches = ".+")
class SystemServiceTest {
    private Database database;
    private UserDao userDao;
    private EquipmentDao equipmentDao;
    private InventoryDao inventoryDao;
    private ReservationService reservationService;
    private InventoryService inventoryService;
    private MaintenanceService maintenanceService;

    @BeforeEach
    void setUp() {
        // Each test starts with fresh schema and seed data, so tests do not depend on run order.
        database = new Database(
                System.getenv().getOrDefault("DB_TEST_URL",
                        "jdbc:mysql://localhost:3306/lab_equipment_test?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai"),
                System.getenv().getOrDefault("DB_USER", "root"),
                System.getenv("DB_PASSWORD"),
                true
        );
        database.init();
        userDao = new UserDao(database);
        equipmentDao = new EquipmentDao(database);
        inventoryDao = new InventoryDao(database);
        ReservationDao reservationDao = new ReservationDao(database);
        reservationService = new ReservationService(database, reservationDao);
        inventoryService = new InventoryService(database, inventoryDao);
        maintenanceService = new MaintenanceService(database, new edu.ucd.comp2013j.lab.dao.MaintenanceDao(database));
    }

    @Test
    void loginWorksForSeededUser() {
        assertTrue(userDao.login("admin", "admin123").isPresent());
        assertFalse(userDao.login("admin", "wrong").isPresent());
    }

    @Test
    void studentRegistrationCreatesLoginAccount() {
        AuthService authService = new AuthService(userDao);

        User user = authService.registerStudent("newstudent", "newpass123",
                "New Student", "new.student@student.edu");

        assertEquals("STUDENT", user.getRole());
        assertTrue(userDao.login("newstudent", "newpass123").isPresent());
    }

    @Test
    void reservationConflictIsRejected() {
        User student = userDao.login("student1", "student123").orElseThrow();
        LocalDateTime start = java.time.LocalDate.now().plusDays(3).atTime(8, 0);
        LocalDateTime end = java.time.LocalDate.now().plusDays(3).atTime(14, 0);

        reservationService.requestReservation(List.of(5), student.getId(), 1, start, end,
                "First booking", Map.of());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reservationService.requestReservation(List.of(5), student.getId(), 1, start, end,
                        "Overlapping test", Map.of()));

        assertTrue(ex.getMessage().contains("already booked"));
    }

    @Test
    void reservationMustUseFixedTimeSlot() {
        User student = userDao.login("student1", "student123").orElseThrow();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reservationService.requestReservation(List.of(5), student.getId(), 1,
                        LocalDateTime.of(2026, 5, 25, 9, 0),
                        LocalDateTime.of(2026, 5, 25, 12, 0),
                        "Custom time test", Map.of()));

        assertTrue(ex.getMessage().contains("fixed time slot"));
    }

    @Test
    void reservationAllowsOnlyOneEquipmentItem() {
        User student = userDao.login("student1", "student123").orElseThrow();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reservationService.requestReservation(List.of(3, 5), student.getId(), 1,
                        LocalDateTime.of(2026, 5, 25, 8, 0),
                        LocalDateTime.of(2026, 5, 25, 14, 0),
                        "Multi equipment test", Map.of()));

        assertTrue(ex.getMessage().contains("Only one equipment"));
    }

    @Test
    void slotAvailabilityShowsBookedSeedSlot() {
        User student = userDao.login("student1", "student123").orElseThrow();
        java.time.LocalDate date = java.time.LocalDate.now().plusDays(4);

        reservationService.requestReservation(List.of(5), student.getId(), 1,
                date.atTime(8, 0),
                date.atTime(14, 0),
                "Slot availability test", Map.of());

        List<Map<String, ?>> slots = reservationService.fixedSlotAvailability(
                5,
                date,
                1
        );

        Map<String, ?> morning = slots.stream()
                .filter(slot -> "MORNING".equals(slot.get("slot")))
                .findFirst()
                .orElseThrow();

        assertEquals(false, morning.get("available"));
        assertEquals("BOOKED", morning.get("reason"));
    }

    @Test
    void inventoryCannotGoBelowZero() {
        User admin = userDao.login("admin", "admin123").orElseThrow();
        Consumable item = inventoryDao.findAll().get(0);

        assertThrows(IllegalArgumentException.class,
                () -> inventoryService.changeStock(item.getId(), admin.getId(), -999, "bad data"));
    }

    @Test
    void stockChangeUpdatesQuantity() {
        User admin = userDao.login("admin", "admin123").orElseThrow();
        Consumable item = inventoryDao.findAll().get(0);

        inventoryService.changeStock(item.getId(), admin.getId(), -1, "used in test");

        int newQuantity = inventoryDao.findAll().stream()
                .filter(c -> c.getId() == item.getId())
                .findFirst()
                .orElseThrow()
                .getQuantity();
        assertEquals(item.getQuantity() - 1, newQuantity);
    }

    @Test
    void adminCanAddConsumableType() {
        int newId = inventoryService.addConsumable(1, "USB-C cable", "piece", 5, 2);

        Consumable created = inventoryDao.findAll().stream()
                .filter(c -> c.getId() == newId)
                .findFirst()
                .orElseThrow();
        assertEquals("USB-C cable", created.getItemName());
        assertEquals(5, created.getQuantity());
    }

    @Test
    void maintenanceReportMovesEquipmentToMaintenance() {
        User student = userDao.login("student1", "student123").orElseThrow();
        Equipment equipment = equipmentDao.findAll().stream()
                .filter(e -> e.getAssetTag().equals("NET-IOT-005"))
                .findFirst()
                .orElseThrow();

        maintenanceService.reportProblem(equipment.getId(), student.getId(),
                "Gateway cannot boot", "The gateway power light is blinking.", "HIGH");

        String newStatus = equipmentDao.findAll().stream()
                .filter(e -> e.getId() == equipment.getId())
                .findFirst()
                .orElseThrow()
                .getStatus();
        assertEquals("MAINTENANCE", newStatus);
    }

    @Test
    void maintenanceDaoFindsActiveTicketForEquipment() {
        Equipment seedTicketEquipment = equipmentDao.findAll().stream()
                .filter(e -> e.getAssetTag().equals("BIO-ECG-004"))
                .findFirst()
                .orElseThrow();
        Equipment availableEquipment = equipmentDao.findAll().stream()
                .filter(e -> e.getAssetTag().equals("NET-IOT-005"))
                .findFirst()
                .orElseThrow();

        assertTrue(new edu.ucd.comp2013j.lab.dao.MaintenanceDao(database)
                .hasActiveTicketForEquipment(seedTicketEquipment.getId()));
        assertFalse(new edu.ucd.comp2013j.lab.dao.MaintenanceDao(database)
                .hasActiveTicketForEquipment(availableEquipment.getId()));
    }

    @Test
    void equipmentStaysInMaintenanceUntilAllTicketsAreFinished() {
        User admin = userDao.login("admin", "admin123").orElseThrow();
        Equipment equipment = equipmentDao.findAll().stream()
                .filter(e -> e.getAssetTag().equals("NET-SW-006"))
                .findFirst()
                .orElseThrow();
        assertEquals("MAINTENANCE", equipment.getStatus());

        int newTicket = maintenanceService.reportProblem(equipment.getId(), admin.getId(),
                "Second switch issue", "Another fault is still being checked.", "MEDIUM");

        maintenanceService.updateTicket(newTicket, admin.getId(), "CLOSED", admin.getId(), "Second issue closed.");
        String statusWithSeedTicketStillOpen = equipmentDao.findAll().stream()
                .filter(e -> e.getId() == equipment.getId())
                .findFirst()
                .orElseThrow()
                .getStatus();
        assertEquals("MAINTENANCE", statusWithSeedTicketStillOpen);

        maintenanceService.updateTicket(2, admin.getId(), "CLOSED", admin.getId(), "Original switch ticket closed.");
        String statusAfterAllTicketsClosed = equipmentDao.findAll().stream()
                .filter(e -> e.getId() == equipment.getId())
                .findFirst()
                .orElseThrow()
                .getStatus();
        assertEquals("AVAILABLE", statusAfterAllTicketsClosed);
    }
}
