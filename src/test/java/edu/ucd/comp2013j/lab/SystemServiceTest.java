package edu.ucd.comp2013j.lab;

import edu.ucd.comp2013j.lab.dao.EquipmentDao;
import edu.ucd.comp2013j.lab.dao.InventoryDao;
import edu.ucd.comp2013j.lab.dao.ReservationDao;
import edu.ucd.comp2013j.lab.dao.UserDao;
import edu.ucd.comp2013j.lab.db.Database;
import edu.ucd.comp2013j.lab.model.Consumable;
import edu.ucd.comp2013j.lab.model.Equipment;
import edu.ucd.comp2013j.lab.model.User;
import edu.ucd.comp2013j.lab.service.InventoryService;
import edu.ucd.comp2013j.lab.service.MaintenanceService;
import edu.ucd.comp2013j.lab.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.time.LocalDateTime;

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
        database = new Database(
                "jdbc:mysql://localhost:3306/lab_equipment_test?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai",
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
    void reservationConflictIsRejected() {
        User student = userDao.login("student1", "student123").orElseThrow();
        LocalDateTime start = LocalDateTime.of(2026, 5, 4, 11, 0);
        LocalDateTime end = LocalDateTime.of(2026, 5, 4, 12, 30);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reservationService.requestReservation(3, student.getId(), 1, start, end, "Overlapping test"));

        assertTrue(ex.getMessage().contains("already booked"));
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
}
