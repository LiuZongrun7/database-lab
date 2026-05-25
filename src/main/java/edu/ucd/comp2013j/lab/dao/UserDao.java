package edu.ucd.comp2013j.lab.dao;

import edu.ucd.comp2013j.lab.db.Database;
import edu.ucd.comp2013j.lab.db.Db;
import edu.ucd.comp2013j.lab.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {
    private final Database database;

    public UserDao(Database database) {
        this.database = database;
    }

    public Optional<User> login(String username, String password) {
        String sql = """
                SELECT user_id, username, full_name, email, role, penalty_points
                FROM users
                WHERE username = ? AND password = ? AND active = TRUE
                """;
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException ex) {
            throw Db.fail(ex);
        }
    }

    public User createStudent(String username, String password, String fullName, String email, List<Integer> labIds) {
        // Only student self-registration is allowed from the public login page.
        String sql = """
                INSERT INTO users (username, password, full_name, email, role, penalty_points)
                VALUES (?, ?, ?, ?, 'STUDENT', 0)
                """;
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, username);
                ps.setString(2, password);
                ps.setString(3, fullName);
                ps.setString(4, email);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int userId = keys.getInt(1);
                        insertStudentLabs(connection, userId, labIds);
                        connection.commit();
                        return new User(userId, username, fullName, email, "STUDENT", 0);
                    }
                }
                throw new SQLException("User id was not generated");
            } catch (RuntimeException | SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLIntegrityConstraintViolationException ex) {
            throw new IllegalArgumentException("Username or email already exists");
        } catch (SQLException ex) {
            throw Db.fail(ex);
        }
    }

    public List<User> findTechnicians() {
        String sql = """
                SELECT user_id, username, full_name, email, role, penalty_points
                FROM users
                WHERE role = 'TECHNICIAN' AND active = TRUE
                ORDER BY full_name
                """;
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<User> users = new ArrayList<>();
            while (rs.next()) {
                users.add(mapUser(rs));
            }
            return users;
        } catch (SQLException ex) {
            throw Db.fail(ex);
        }
    }

    public Optional<User> findById(int userId) {
        String sql = """
                SELECT user_id, username, full_name, email, role, penalty_points
                FROM users
                WHERE user_id = ? AND active = TRUE
                """;
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException ex) {
            throw Db.fail(ex);
        }
    }

    public List<User> findApprovers() {
        String sql = """
                SELECT user_id, username, full_name, email, role, penalty_points
                FROM users
                WHERE role = 'ADMIN' AND active = TRUE
                ORDER BY role, full_name
                """;
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<User> users = new ArrayList<>();
            while (rs.next()) {
                users.add(mapUser(rs));
            }
            return users;
        } catch (SQLException ex) {
            throw Db.fail(ex);
        }
    }

    private void insertStudentLabs(Connection connection, int userId, List<Integer> labIds) throws SQLException {
        if (labIds == null || labIds.isEmpty()) {
            throw new IllegalArgumentException("At least one lab is required");
        }
        boolean inserted = false;
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO student_labs (user_id, lab_id) VALUES (?, ?)")) {
            for (Integer labId : labIds) {
                if (labId == null || labId <= 0) {
                    continue;
                }
                ps.setInt(1, userId);
                ps.setInt(2, labId);
                ps.addBatch();
                inserted = true;
            }
            if (!inserted) {
                throw new IllegalArgumentException("At least one lab is required");
            }
            ps.executeBatch();
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("role"),
                rs.getInt("penalty_points")
        );
    }
}
