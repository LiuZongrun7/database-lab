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

    public User createStudent(String username, String password, String fullName, String email) {
        // Only student self-registration is allowed from the public login page.
        String sql = """
                INSERT INTO users (username, password, full_name, email, role, penalty_points)
                VALUES (?, ?, ?, ?, 'STUDENT', 0)
                """;
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, fullName);
            ps.setString(4, email);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return new User(keys.getInt(1), username, fullName, email, "STUDENT", 0);
                }
            }
            throw new SQLException("User id was not generated");
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
                WHERE role IN ('ADMIN', 'TEACHER') AND active = TRUE
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
