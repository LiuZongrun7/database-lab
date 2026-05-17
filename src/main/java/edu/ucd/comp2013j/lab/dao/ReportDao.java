package edu.ucd.comp2013j.lab.dao;

import edu.ucd.comp2013j.lab.db.Database;
import edu.ucd.comp2013j.lab.db.Db;
import edu.ucd.comp2013j.lab.model.ReportRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReportDao {
    private final Database database;

    public ReportDao(Database database) {
        this.database = database;
    }

    public List<ReportRow> labUsage() {
        // Report queries are read-only; the SQL view hides the longer reservation join.
        String sql = """
                SELECT lab_code, reservation_count, approved_count, completed_count
                FROM v_lab_usage_report
                ORDER BY reservation_count DESC, lab_code
                """;
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<ReportRow> rows = new ArrayList<>();
            while (rs.next()) {
                rows.add(new ReportRow(
                        rs.getString("lab_code"),
                        rs.getInt("reservation_count"),
                        rs.getInt("approved_count"),
                        rs.getInt("completed_count")
                ));
            }
            return rows;
        } catch (SQLException ex) {
            throw Db.fail(ex);
        }
    }

    public List<ReportRow> equipmentStatusSummary() {
        // This one uses GROUP BY directly because it is easier to explain in the report.
        String sql = """
                SELECT status, COUNT(*) AS total,
                       SUM(CASE WHEN risk_level = 'HIGH' THEN 1 ELSE 0 END) AS high_risk,
                       SUM(open_ticket_count) AS open_tickets
                FROM v_equipment_status
                GROUP BY status
                ORDER BY status
                """;
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<ReportRow> rows = new ArrayList<>();
            while (rs.next()) {
                rows.add(new ReportRow(
                        rs.getString("status"),
                        rs.getInt("total"),
                        rs.getInt("high_risk"),
                        rs.getInt("open_tickets")
                ));
            }
            return rows;
        } catch (SQLException ex) {
            throw Db.fail(ex);
        }
    }
}
