package edu.ucd.comp2013j.lab.service;

import edu.ucd.comp2013j.lab.dao.UserDao;
import edu.ucd.comp2013j.lab.model.User;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class AuthService {
    private final UserDao userDao;

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public Optional<User> login(String username, String password) {
        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            return Optional.empty();
        }
        return userDao.login(username.trim(), password.trim());
    }

    public User registerStudent(String username, String password, String fullName, String email, List<Integer> labIds) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name is required");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        Set<Integer> cleanLabIds = new LinkedHashSet<>();
        if (labIds != null) {
            for (Integer labId : labIds) {
                if (labId != null && labId > 0) {
                    cleanLabIds.add(labId);
                }
            }
        }
        if (cleanLabIds.isEmpty()) {
            throw new IllegalArgumentException("At least one lab is required");
        }
        // Self-service registration is intentionally limited to the student role.
        return userDao.createStudent(username.trim(), password.trim(), fullName.trim(), email.trim(),
                List.copyOf(cleanLabIds));
    }
}
