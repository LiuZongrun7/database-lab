package edu.ucd.comp2013j.lab.dao;

import edu.ucd.comp2013j.lab.db.Database;
import edu.ucd.comp2013j.lab.db.Db;
import edu.ucd.comp2013j.lab.model.Equipment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EquipmentDao {
    private final Database database;

    public EquipmentDao(Database database) {
        this.database = database;
    }

    public List<Equipment> findAll() {
        String sql = """
                SELECT equipment_id, asset_tag, equipment_name, category, lab_code, status, risk_level, open_ticket_count
                FROM v_equipment_status
                ORDER BY lab_code, asset_tag
                """;
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Equipment> items = new ArrayList<>();
            while (rs.next()) {
                items.add(mapEquipment(rs));
            }
            return items;
        } catch (SQLException ex) {
            throw Db.fail(ex);
        }
    }

    public List<Equipment> search(String keyword) {
        String like = "%" + keyword.toLowerCase() + "%";
        String sql = """
                SELECT equipment_id, asset_tag, equipment_name, category, lab_code, status, risk_level, open_ticket_count
                FROM v_equipment_status
                WHERE LOWER(asset_tag) LIKE ? OR LOWER(equipment_name) LIKE ? OR LOWER(category) LIKE ?
                ORDER BY lab_code, asset_tag
                """;
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                List<Equipment> items = new ArrayList<>();
                while (rs.next()) {
                    items.add(mapEquipment(rs));
                }
                return items;
            }
        } catch (SQLException ex) {
            throw Db.fail(ex);
        }
    }

    public void updateStatus(int equipmentId, String status) {
        String sql = "UPDATE equipment SET status = ? WHERE equipment_id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, equipmentId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw Db.fail(ex);
        }
    }

    private Equipment mapEquipment(ResultSet rs) throws SQLException {
        return new Equipment(
                rs.getInt("equipment_id"),
                rs.getString("asset_tag"),
                rs.getString("equipment_name"),
                rs.getString("category"),
                rs.getString("lab_code"),
                rs.getString("status"),
                rs.getString("risk_level"),
                rs.getInt("open_ticket_count")
        );
    }
}
