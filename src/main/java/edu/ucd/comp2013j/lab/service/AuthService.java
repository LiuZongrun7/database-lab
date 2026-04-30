package edu.ucd.comp2013j.lab.service;

import edu.ucd.comp2013j.lab.dao.UserDao;
import edu.ucd.comp2013j.lab.model.User;

import java.util.Optional;

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
}
