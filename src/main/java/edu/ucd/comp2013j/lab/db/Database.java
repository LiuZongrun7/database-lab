package edu.ucd.comp2013j.lab.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final boolean createDatabase;

    public Database() {
        this(
                env("DB_URL", "jdbc:mysql://localhost:3306/lab_equipment?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai"),
                env("DB_USER", "root"),
                env("DB_PASSWORD", ""),
                true
        );
    }

    public Database(String jdbcUrl) {
        this(jdbcUrl, env("DB_USER", "root"), env("DB_PASSWORD", ""), false);
    }

    public Database(String jdbcUrl, String username, String password) {
        this(jdbcUrl, username, password, false);
    }

    public Database(String jdbcUrl, String username, String password, boolean createDatabase) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.createDatabase = createDatabase;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    public void init() {
        if (createDatabase) {
            createDatabaseIfNeeded();
        }
        try (Connection connection = getConnection()) {
            runScript(connection, "/db/schema.sql");
            runScript(connection, "/db/seed.sql");
        } catch (SQLException | IOException ex) {
            throw new IllegalStateException("Cannot initialise database", ex);
        }
    }

    private void createDatabaseIfNeeded() {
        String databaseName = databaseNameFromUrl();
        String serverUrl = serverUrlFromUrl();
        try (Connection connection = DriverManager.getConnection(serverUrl, username, password);
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE DATABASE IF NOT EXISTS `" + databaseName + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        } catch (SQLException ex) {
            throw new IllegalStateException("Cannot create database " + databaseName, ex);
        }
    }

    private String databaseNameFromUrl() {
        int slash = jdbcUrl.indexOf("/", "jdbc:mysql://".length());
        int question = jdbcUrl.indexOf("?", slash);
        if (slash < 0) {
            return "lab_equipment";
        }
        if (question < 0) {
            return jdbcUrl.substring(slash + 1);
        }
        return jdbcUrl.substring(slash + 1, question);
    }

    private String serverUrlFromUrl() {
        int slash = jdbcUrl.indexOf("/", "jdbc:mysql://".length());
        if (slash < 0) {
            return jdbcUrl;
        }
        int question = jdbcUrl.indexOf("?", slash);
        if (question < 0) {
            return jdbcUrl.substring(0, slash + 1);
        }
        return jdbcUrl.substring(0, slash + 1) + jdbcUrl.substring(question);
    }

    private void runScript(Connection connection, String resourcePath) throws IOException, SQLException {
        for (String sql : loadStatements(resourcePath)) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(sql);
            }
        }
    }

    private List<String> loadStatements(String resourcePath) throws IOException {
        InputStream input = Database.class.getResourceAsStream(resourcePath);
        if (input == null) {
            throw new IOException("Missing resource: " + resourcePath);
        }

        StringBuilder sql = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.startsWith("--")) {
                    sql.append(line).append('\n');
                }
            }
        }

        List<String> statements = new ArrayList<>();
        for (String statement : sql.toString().split(";")) {
            String trimmed = statement.trim();
            if (!trimmed.isEmpty()) {
                statements.add(trimmed);
            }
        }
        return statements;
    }

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }
}
