package edu.ucd.comp2013j.lab.model;

public class User {
    private final int id;
    private final String username;
    private final String fullName;
    private final String email;
    private final String role;
    private final int penaltyPoints;

    public User(int id, String username, String fullName, String email, String role, int penaltyPoints) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.penaltyPoints = penaltyPoints;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public int getPenaltyPoints() {
        return penaltyPoints;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean isTeacher() {
        return "TEACHER".equals(role);
    }

    public boolean isTechnician() {
        return "TECHNICIAN".equals(role);
    }

    @Override
    public String toString() {
        return fullName + " (" + role + ")";
    }
}
