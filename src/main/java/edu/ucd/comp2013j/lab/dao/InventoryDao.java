package edu.ucd.comp2013j.lab.dao;

import edu.ucd.comp2013j.lab.db.Database;
import edu.ucd.comp2013j.lab.db.Db;
import edu.ucd.comp2013j.lab.model.Consumable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class InventoryDao {
    private final Database database;

    public InventoryDao(Database database) {
        this.database = database;
    }

    public List<Consumable> findAll() {
        String sql = """
                SELECT c.consumable_id, l.lab_code, c.item_name, c.unit, c.quantity, c.reorder_level
                FROM consumables c
                JOIN labs l ON c.lab_id = l.lab_id
                ORDER BY l.lab_code, c.item_name
                """;
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Consumable> items = new ArrayList<>();
            while (rs.next()) {
                items.add(mapConsumable(rs));
            }
            return items;
        } catch (SQLException ex) {
            throw Db.fail(ex);
        }
    }

    public int getQuantityForUpdate(Connection connection, int consumableId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT quantity FROM consumables WHERE consumable_id = ? FOR UPDATE")) {
            ps.setInt(1, consumableId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("quantity");
                }
            }
        }
        throw new SQLException("Consumable not found: " + consumableId);
    }

    public int create(int labId, String itemName, String unit, int quantity, int reorderLevel) {
        String sql = """
                INSERT INTO consumables (lab_id, item_name, unit, quantity, reorder_level)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, labId);
            ps.setString(2, itemName);
            ps.setString(3, unit);
            ps.setInt(4, quantity);
            ps.setInt(5, reorderLevel);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new SQLException("Consumable id was not generated");
        } catch (SQLIntegrityConstraintViolationException ex) {
            throw new IllegalArgumentException("Consumable item already exists in this lab");
        } catch (SQLException ex) {
            throw Db.fail(ex);
        }
    }

    public void updateQuantity(Connection connection, int consumableId, int newQuantity) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE consumables SET quantity = ? WHERE consumable_id = ?")) {
            ps.setInt(1, newQuantity);
            ps.setInt(2, consumableId);
            ps.executeUpdate();
        }
    }

    public void addStockTransaction(Connection connection, int consumableId, int userId, int changeAmount, String reason)
            throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO stock_transactions (consumable_id, user_id, change_amount, reason) VALUES (?, ?, ?, ?)")) {
            ps.setInt(1, consumableId);
            ps.setInt(2, userId);
            ps.setInt(3, changeAmount);
            ps.setString(4, reason);
            ps.executeUpdate();
        }
    }

    private Consumable mapConsumable(ResultSet rs) throws SQLException {
        return new Consumable(
                rs.getInt("consumable_id"),
                rs.getString("lab_code"),
                rs.getString("item_name"),
                rs.getString("unit"),
                rs.getInt("quantity"),
                rs.getInt("reorder_level")
        );
    }
}
