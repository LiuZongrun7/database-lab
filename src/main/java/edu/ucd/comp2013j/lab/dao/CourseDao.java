package edu.ucd.comp2013j.lab.dao;

import edu.ucd.comp2013j.lab.db.Database;
import edu.ucd.comp2013j.lab.db.Db;
import edu.ucd.comp2013j.lab.model.Course;
import edu.ucd.comp2013j.lab.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CourseDao {
    private final Database database;

    public CourseDao(Database database) {
        this.database = database;
    }

    public List<Course> findCoursesForUser(User user) {
        String sql;
        if (user.isAdmin()) {
            sql = "SELECT course_id, course_code, course_name FROM courses ORDER BY course_code";
        } else {
            // Non-admin users only see courses where they are listed in course_members.
            sql = """
                    SELECT c.course_id, c.course_code, c.course_name
                    FROM courses c
                    JOIN course_members cm ON c.course_id = cm.course_id
                    WHERE cm.user_id = ?
                    ORDER BY c.course_code
                    """;
        }

        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            if (!user.isAdmin()) {
                ps.setInt(1, user.getId());
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Course> courses = new ArrayList<>();
                while (rs.next()) {
                    courses.add(new Course(rs.getInt("course_id"), rs.getString("course_code"), rs.getString("course_name")));
                }
                return courses;
            }
        } catch (SQLException ex) {
            throw Db.fail(ex);
        }
    }
}
