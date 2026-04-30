package edu.ucd.comp2013j.lab.db;

import java.sql.SQLException;

public final class Db {
    private Db() {
    }

    public static RuntimeException fail(SQLException ex) {
        return new IllegalStateException("Database operation failed: " + ex.getMessage(), ex);
    }
}
