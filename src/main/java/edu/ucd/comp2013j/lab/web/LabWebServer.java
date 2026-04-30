package edu.ucd.comp2013j.lab.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import edu.ucd.comp2013j.lab.dao.CourseDao;
import edu.ucd.comp2013j.lab.dao.EquipmentDao;
import edu.ucd.comp2013j.lab.dao.InventoryDao;
import edu.ucd.comp2013j.lab.dao.MaintenanceDao;
import edu.ucd.comp2013j.lab.dao.ReportDao;
import edu.ucd.comp2013j.lab.dao.ReservationDao;
import edu.ucd.comp2013j.lab.dao.UserDao;
import edu.ucd.comp2013j.lab.db.Database;
import edu.ucd.comp2013j.lab.model.Consumable;
import edu.ucd.comp2013j.lab.model.Course;
import edu.ucd.comp2013j.lab.model.Equipment;
import edu.ucd.comp2013j.lab.model.MaintenanceTicket;
import edu.ucd.comp2013j.lab.model.ReportRow;
import edu.ucd.comp2013j.lab.model.Reservation;
import edu.ucd.comp2013j.lab.model.User;
import edu.ucd.comp2013j.lab.service.AuthService;
import edu.ucd.comp2013j.lab.service.InventoryService;
import edu.ucd.comp2013j.lab.service.MaintenanceService;
import edu.ucd.comp2013j.lab.service.ReservationService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LabWebServer {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final Database database;
    private final int port;
    private final UserDao userDao;
    private final EquipmentDao equipmentDao;
    private final CourseDao courseDao;
    private final ReservationDao reservationDao;
    private final MaintenanceDao maintenanceDao;
    private final InventoryDao inventoryDao;
    private final ReportDao reportDao;
    private final AuthService authService;
    private final ReservationService reservationService;
    private final MaintenanceService maintenanceService;
    private final InventoryService inventoryService;

    public LabWebServer(Database database, int port) {
        this.database = database;
        this.port = port;
        this.userDao = new UserDao(database);
        this.equipmentDao = new EquipmentDao(database);
        this.courseDao = new CourseDao(database);
        this.reservationDao = new ReservationDao(database);
        this.maintenanceDao = new MaintenanceDao(database);
        this.inventoryDao = new InventoryDao(database);
        this.reportDao = new ReportDao(database);
        this.authService = new AuthService(userDao);
        this.reservationService = new ReservationService(database, reservationDao);
        this.maintenanceService = new MaintenanceService(database, maintenanceDao);
        this.inventoryService = new InventoryService(database, inventoryDao);
    }

    public void start() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", this::handleStatic);
            server.createContext("/api/login", api(this::handleLogin));
            server.createContext("/api/equipment", api(this::handleEquipment));
            server.createContext("/api/courses", api(this::handleCourses));
            server.createContext("/api/users/technicians", api(this::handleTechnicians));
            server.createContext("/api/reservations", api(this::handleReservations));
            server.createContext("/api/reservations/create", api(this::handleCreateReservation));
            server.createContext("/api/reservations/decide", api(this::handleDecideReservation));
            server.createContext("/api/reservations/cancel", api(this::handleCancelReservation));
            server.createContext("/api/maintenance", api(this::handleMaintenance));
            server.createContext("/api/maintenance/report", api(this::handleReportProblem));
            server.createContext("/api/maintenance/update", api(this::handleUpdateTicket));
            server.createContext("/api/inventory", api(this::handleInventory));
            server.createContext("/api/inventory/change", api(this::handleChangeStock));
            server.createContext("/api/reports/lab-usage", api(this::handleLabUsage));
            server.createContext("/api/reports/equipment-status", api(this::handleEquipmentStatusReport));
            server.setExecutor(null);
            server.start();
            System.out.println("Lab Equipment System running at http://localhost:" + port);
            System.out.println("Press Ctrl+C to stop the server.");
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot start web server", ex);
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        onlyPost(exchange);
        FormData form = FormData.from(exchange);
        Optional<User> user = authService.login(form.get("username"), form.get("password"));
        if (user.isPresent()) {
            User u = user.get();
            sendJson(exchange, Json.object(Json.row(
                    "ok", true,
                    "id", u.getId(),
                    "username", u.getUsername(),
                    "fullName", u.getFullName(),
                    "role", u.getRole()
            )));
        } else {
            sendJson(exchange, Json.error("Invalid username or password"));
        }
    }

    private void handleEquipment(HttpExchange exchange) throws IOException {
        onlyGet(exchange);
        List<Map<String, ?>> rows = new ArrayList<>();
        String keyword = queryParam(exchange, "q");
        List<Equipment> equipment = keyword.isBlank() ? equipmentDao.findAll() : equipmentDao.search(keyword);
        for (Equipment e : equipment) {
            rows.add(Json.row(
                    "id", e.getId(),
                    "assetTag", e.getAssetTag(),
                    "name", e.getName(),
                    "category", e.getCategory(),
                    "labCode", e.getLabCode(),
                    "status", e.getStatus(),
                    "riskLevel", e.getRiskLevel(),
                    "openTicketCount", e.getOpenTicketCount()
            ));
        }
        sendJson(exchange, Json.array(rows));
    }

    private void handleCourses(HttpExchange exchange) throws IOException {
        onlyGet(exchange);
        User user = currentUser(exchange);
        List<Map<String, ?>> rows = new ArrayList<>();
        for (Course c : courseDao.findCoursesForUser(user)) {
            rows.add(Json.row("id", c.getId(), "code", c.getCode(), "name", c.getName()));
        }
        sendJson(exchange, Json.array(rows));
    }

    private void handleTechnicians(HttpExchange exchange) throws IOException {
        onlyGet(exchange);
        List<Map<String, ?>> rows = new ArrayList<>();
        for (User user : userDao.findTechnicians()) {
            rows.add(Json.row("id", user.getId(), "fullName", user.getFullName(), "role", user.getRole()));
        }
        sendJson(exchange, Json.array(rows));
    }

    private void handleReservations(HttpExchange exchange) throws IOException {
        onlyGet(exchange);
        User user = currentUser(exchange);
        List<Map<String, ?>> rows = new ArrayList<>();
        for (Reservation r : reservationDao.findVisibleFor(user)) {
            rows.add(Json.row(
                    "id", r.getId(),
                    "assetTag", r.getAssetTag(),
                    "equipmentName", r.getEquipmentName(),
                    "requesterName", r.getRequesterName(),
                    "startTime", r.getStartTime().format(TIME_FORMAT),
                    "endTime", r.getEndTime().format(TIME_FORMAT),
                    "status", r.getStatus(),
                    "purpose", r.getPurpose()
            ));
        }
        sendJson(exchange, Json.array(rows));
    }

    private void handleCreateReservation(HttpExchange exchange) throws IOException {
        onlyPost(exchange);
        FormData form = FormData.from(exchange);
        int id = reservationService.requestReservation(
                form.getInt("equipmentId"),
                form.getInt("userId"),
                form.getOptionalInt("courseId"),
                LocalDateTime.parse(form.get("startTime"), TIME_FORMAT),
                LocalDateTime.parse(form.get("endTime"), TIME_FORMAT),
                form.get("purpose")
        );
        sendJson(exchange, Json.ok("Reservation request #" + id + " submitted."));
    }

    private void handleDecideReservation(HttpExchange exchange) throws IOException {
        onlyPost(exchange);
        FormData form = FormData.from(exchange);
        reservationService.decideReservation(
                form.getInt("reservationId"),
                form.getInt("userId"),
                Boolean.parseBoolean(form.get("approve")),
                form.get("comment")
        );
        sendJson(exchange, Json.ok("Reservation decision saved."));
    }

    private void handleCancelReservation(HttpExchange exchange) throws IOException {
        onlyPost(exchange);
        FormData form = FormData.from(exchange);
        reservationDao.cancel(form.getInt("reservationId"), form.getInt("userId"));
        sendJson(exchange, Json.ok("Cancel request finished."));
    }

    private void handleMaintenance(HttpExchange exchange) throws IOException {
        onlyGet(exchange);
        List<Map<String, ?>> rows = new ArrayList<>();
        for (MaintenanceTicket t : maintenanceDao.findAll()) {
            rows.add(Json.row(
                    "id", t.getId(),
                    "assetTag", t.getAssetTag(),
                    "equipmentName", t.getEquipmentName(),
                    "reporterName", t.getReporterName(),
                    "technicianName", t.getTechnicianName(),
                    "title", t.getTitle(),
                    "priority", t.getPriority(),
                    "status", t.getStatus(),
                    "reportedAt", t.getReportedAt().format(TIME_FORMAT)
            ));
        }
        sendJson(exchange, Json.array(rows));
    }

    private void handleReportProblem(HttpExchange exchange) throws IOException {
        onlyPost(exchange);
        FormData form = FormData.from(exchange);
        int id = maintenanceService.reportProblem(
                form.getInt("equipmentId"),
                form.getInt("userId"),
                form.get("title"),
                form.get("description"),
                form.get("priority")
        );
        sendJson(exchange, Json.ok("Maintenance ticket #" + id + " created."));
    }

    private void handleUpdateTicket(HttpExchange exchange) throws IOException {
        onlyPost(exchange);
        FormData form = FormData.from(exchange);
        maintenanceService.updateTicket(
                form.getInt("ticketId"),
                form.getOptionalInt("technicianId"),
                form.get("status"),
                form.getInt("userId"),
                form.get("note")
        );
        sendJson(exchange, Json.ok("Maintenance ticket updated."));
    }

    private void handleInventory(HttpExchange exchange) throws IOException {
        onlyGet(exchange);
        List<Map<String, ?>> rows = new ArrayList<>();
        for (Consumable c : inventoryDao.findAll()) {
            rows.add(Json.row(
                    "id", c.getId(),
                    "labCode", c.getLabCode(),
                    "itemName", c.getItemName(),
                    "unit", c.getUnit(),
                    "quantity", c.getQuantity(),
                    "reorderLevel", c.getReorderLevel(),
                    "lowStock", c.isLowStock()
            ));
        }
        sendJson(exchange, Json.array(rows));
    }

    private void handleChangeStock(HttpExchange exchange) throws IOException {
        onlyPost(exchange);
        FormData form = FormData.from(exchange);
        inventoryService.changeStock(
                form.getInt("consumableId"),
                form.getInt("userId"),
                form.getInt("amount"),
                form.get("reason")
        );
        sendJson(exchange, Json.ok("Stock changed."));
    }

    private void handleLabUsage(HttpExchange exchange) throws IOException {
        onlyGet(exchange);
        sendJson(exchange, reportRows(reportDao.labUsage(), "reservations", "approved", "completed"));
    }

    private void handleEquipmentStatusReport(HttpExchange exchange) throws IOException {
        onlyGet(exchange);
        sendJson(exchange, reportRows(reportDao.equipmentStatusSummary(), "equipmentCount", "highRisk", "openTickets"));
    }

    private String reportRows(List<ReportRow> reportRows, String a, String b, String c) {
        List<Map<String, ?>> rows = new ArrayList<>();
        for (ReportRow row : reportRows) {
            rows.add(Json.row("label", row.getLabel(), a, row.getCountA(), b, row.getCountB(), c, row.getCountC()));
        }
        return Json.array(rows);
    }

    private User currentUser(HttpExchange exchange) {
        int userId = Integer.parseInt(queryParam(exchange, "userId"));
        return userDao.findById(userId).orElseThrow(() -> new IllegalArgumentException("Unknown user id: " + userId));
    }

    private void handleStatic(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/")) {
            path = "/index.html";
        }
        if (path.contains("..")) {
            send(exchange, 400, "text/plain", "Bad path");
            return;
        }
        String resource = "/web" + path;
        try (InputStream input = LabWebServer.class.getResourceAsStream(resource)) {
            if (input == null) {
                send(exchange, 404, "text/plain", "Not found");
                return;
            }
            byte[] data = input.readAllBytes();
            send(exchange, 200, contentType(path), data);
        }
    }

    private void sendJson(HttpExchange exchange, String json) throws IOException {
        send(exchange, 200, "application/json; charset=utf-8", json);
    }

    private com.sun.net.httpserver.HttpHandler api(ApiAction action) {
        return exchange -> {
            try {
                action.handle(exchange);
            } catch (Exception ex) {
                sendJson(exchange, Json.error(ex.getMessage() == null ? "Server error" : ex.getMessage()));
            }
        };
    }

    private void send(HttpExchange exchange, int status, String contentType, String text) throws IOException {
        send(exchange, status, contentType, text.getBytes(StandardCharsets.UTF_8));
    }

    private void send(HttpExchange exchange, int status, String contentType, byte[] data) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, data.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(data);
        }
    }

    private void onlyGet(HttpExchange exchange) {
        if (!"GET".equals(exchange.getRequestMethod())) {
            throw new IllegalArgumentException("GET request required");
        }
    }

    private void onlyPost(HttpExchange exchange) {
        if (!"POST".equals(exchange.getRequestMethod())) {
            throw new IllegalArgumentException("POST request required");
        }
    }

    private String queryParam(HttpExchange exchange, String key) {
        String query = exchange.getRequestURI().getRawQuery();
        if (query == null || query.isBlank()) {
            return "";
        }
        return FormData.fromQuery(query).get(key);
    }

    private String contentType(String path) {
        if (path.endsWith(".css")) {
            return "text/css; charset=utf-8";
        }
        if (path.endsWith(".js")) {
            return "application/javascript; charset=utf-8";
        }
        return "text/html; charset=utf-8";
    }

    @FunctionalInterface
    private interface ApiAction {
        void handle(HttpExchange exchange) throws Exception;
    }
}
