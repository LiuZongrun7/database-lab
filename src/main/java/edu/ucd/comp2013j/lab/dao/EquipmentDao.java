package edu.ucd.comp2013j.lab.dao;

import edu.ucd.comp2013j.lab.db.Database;
import edu.ucd.comp2013j.lab.db.Db;
import edu.ucd.comp2013j.lab.model.Equipment;
import edu.ucd.comp2013j.lab.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EquipmentDao {
    private final Database database;

    public EquipmentDao(Database database) {
        this.database = database;
    }

    public List<Equipment> findAll() {
        return findVisibleFor(null);
    }

    public List<Equipment> findVisibleFor(User user) {
        return queryVisible(user, "");
    }

    public List<Equipment> search(String keyword) {
        return searchVisibleFor(null, keyword);
    }

    public List<Equipment> searchVisibleFor(User user, String keyword) {
        return queryVisible(user, keyword == null ? "" : keyword);
    }

    private List<Equipment> queryVisible(User user, String keyword) {
        boolean studentOnly = user != null && "STUDENT".equals(user.getRole());
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        StringBuilder sql = new StringBuilder("""
                SELECT equipment_id, asset_tag, equipment_name, category, lab_id, lab_code,
                       status, purchase_date, risk_level, notes, open_ticket_count
                FROM v_equipment_status ves
                WHERE 1 = 1
                """);
        if (studentOnly) {
            sql.append("""
                      AND EXISTS (
                          SELECT 1
                          FROM student_labs sl
                          WHERE sl.lab_id = ves.lab_id
                            AND sl.user_id = ?
                      )
                    """);
        }
        if (hasKeyword) {
            sql.append(" AND (LOWER(asset_tag) LIKE ? OR LOWER(equipment_name) LIKE ? OR LOWER(category) LIKE ?)");
        }
        sql.append(" ORDER BY lab_code, asset_tag");

        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            int index = 1;
            if (studentOnly) {
                ps.setInt(index++, user.getId());
            }
            if (hasKeyword) {
                String like = "%" + keyword.toLowerCase() + "%";
                ps.setString(index++, like);
                ps.setString(index++, like);
                ps.setString(index, like);
            }
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

    public List<Map<String, ?>> findLabs() {
        String sql = "SELECT lab_id, lab_code, lab_name FROM labs ORDER BY lab_code";
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Map<String, ?>> labs = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", rs.getInt("lab_id"));
                row.put("code", rs.getString("lab_code"));
                row.put("name", rs.getString("lab_name"));
                labs.add(row);
            }
            return labs;
        } catch (SQLException ex) {
            throw Db.fail(ex);
        }
    }

    public int create(String assetTag, String name, String category, int labId, String status,
                      String purchaseDate, String riskLevel, String notes) {
        String sql = """
                INSERT INTO equipment (asset_tag, equipment_name, category, lab_id, status, purchase_date, risk_level, notes)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            setEquipmentFields(ps, assetTag, name, category, labId, status, purchaseDate, riskLevel, notes);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new SQLException("Equipment id was not generated");
        } catch (SQLException ex) {
            throw Db.fail(ex);
        }
    }

    public void update(int equipmentId, String assetTag, String name, String category, int labId, String status,
                       String purchaseDate, String riskLevel, String notes) {
        String sql = """
                UPDATE equipment
                SET asset_tag = ?, equipment_name = ?, category = ?, lab_id = ?,
                    status = ?, purchase_date = ?, risk_level = ?, notes = ?
                WHERE equipment_id = ?
                """;
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            setEquipmentFields(ps, assetTag, name, category, labId, status, purchaseDate, riskLevel, notes);
            ps.setInt(9, equipmentId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw Db.fail(ex);
        }
    }

    public void retire(int equipmentId) {
        // Retire keeps historical reservation and ticket records valid.
        updateStatus(equipmentId, "RETIRED");
    }

    private void setEquipmentFields(PreparedStatement ps, String assetTag, String name, String category, int labId,
                                    String status, String purchaseDate, String riskLevel, String notes)
            throws SQLException {
        ps.setString(1, assetTag.trim());
        ps.setString(2, name.trim());
        ps.setString(3, category.trim());
        ps.setInt(4, labId);
        ps.setString(5, status);
        if (purchaseDate == null || purchaseDate.isBlank()) {
            ps.setNull(6, Types.DATE);
        } else {
            ps.setString(6, purchaseDate.trim());
        }
        ps.setString(7, riskLevel);
        ps.setString(8, notes == null ? "" : notes.trim());
    }

    private Equipment mapEquipment(ResultSet rs) throws SQLException {
        return new Equipment(
                rs.getInt("equipment_id"),
                rs.getString("asset_tag"),
                rs.getString("equipment_name"),
                rs.getString("category"),
                rs.getInt("lab_id"),
                rs.getString("lab_code"),
                rs.getString("status"),
                rs.getString("purchase_date"),
                rs.getString("risk_level"),
                rs.getString("notes"),
                rs.getInt("open_ticket_count")
        );
    }
}
