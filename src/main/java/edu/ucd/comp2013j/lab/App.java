package edu.ucd.comp2013j.lab;

import edu.ucd.comp2013j.lab.db.Database;
import edu.ucd.comp2013j.lab.web.LabWebServer;

public class App {
    public static void main(String[] args) {
        Database database = new Database();
        if (shouldInitializeDatabase()) {
            database.init();
        }
        new LabWebServer(database, bindHost(), port()).start();
    }

    private static int port() {
        String value = System.getenv("APP_PORT");
        if (value == null || value.isBlank()) {
            return 8080;
        }
        return Integer.parseInt(value);
    }

    private static String bindHost() {
        String value = System.getenv("APP_BIND_HOST");
        if (value == null || value.isBlank()) {
            return "0.0.0.0";
        }
        return value;
    }

    private static boolean shouldInitializeDatabase() {
        String value = System.getenv("DB_INIT_MODE");
        return value == null || value.isBlank() || !"none".equalsIgnoreCase(value);
    }
}
