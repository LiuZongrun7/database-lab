package edu.ucd.comp2013j.lab.service;

import edu.ucd.comp2013j.lab.dao.InventoryDao;
import edu.ucd.comp2013j.lab.db.Database;
import edu.ucd.comp2013j.lab.db.Db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

public class InventoryService {
    private final Database database;
    private final InventoryDao inventoryDao;

    public InventoryService(Database database, InventoryDao inventoryDao) {
        this.database = database;
        this.inventoryDao = inventoryDao;
    }

    public void changeStock(int consumableId, int userId, int amount, String reason) {
        if (amount == 0) {
            throw new IllegalArgumentException("Change amount cannot be zero");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Reason is required");
        }

        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int oldQuantity = inventoryDao.getQuantityForUpdate(connection, consumableId);
                int newQuantity = oldQuantity + amount;
                if (newQuantity < 0) {
                    throw new IllegalArgumentException("Not enough stock. Current quantity is " + oldQuantity);
                }
                inventoryDao.updateQuantity(connection, consumableId, newQuantity);
                inventoryDao.addStockTransaction(connection, consumableId, userId, amount, reason.trim());
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

    public int addConsumable(int labId, String itemName, String unit, int quantity, int reorderLevel) {
        return addConsumable(labId, itemName, unit, quantity, reorderLevel, null);
    }

    public int addConsumable(int labId, String itemName, String unit, int quantity, int reorderLevel, Integer userId) {
        if (itemName == null || itemName.isBlank()) {
            throw new IllegalArgumentException("Consumable item is required");
        }
        if (unit == null || unit.isBlank()) {
            throw new IllegalArgumentException("Unit is required");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        if (reorderLevel < 0) {
            throw new IllegalArgumentException("Reorder level cannot be negative");
        }
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int consumableId = inventoryDao.create(connection, labId, itemName.trim(), unit.trim(), quantity, reorderLevel);
                if (userId != null && quantity > 0) {
                    inventoryDao.addStockTransaction(connection, consumableId, userId, quantity, "Initial stock count");
                }
                connection.commit();
                return consumableId;
            } catch (SQLIntegrityConstraintViolationException ex) {
                connection.rollback();
                throw new IllegalArgumentException("Consumable item already exists in this lab");
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
