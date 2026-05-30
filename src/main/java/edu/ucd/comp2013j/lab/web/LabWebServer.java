package edu.ucd.comp2013j.lab.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import edu.ucd.comp2013j.lab.dao.EquipmentDao;
import edu.ucd.comp2013j.lab.dao.InventoryDao;
import edu.ucd.comp2013j.lab.dao.MaintenanceDao;
import edu.ucd.comp2013j.lab.dao.ReportDao;
import edu.ucd.comp2013j.lab.dao.ReservationDao;
import edu.ucd.comp2013j.lab.dao.UserDao;
import edu.ucd.comp2013j.lab.db.Database;
import edu.ucd.comp2013j.lab.model.Consumable;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LabWebServer {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final Database database;
    private final String bindHost;
    private final int port;
    private final UserDao userDao;
    private final EquipmentDao equipmentDao;
    private final ReservationDao reservationDao;
    private final MaintenanceDao maintenanceDao;
    private final InventoryDao inventoryDao;
    private final ReportDao reportDao;
    private final AuthService authService;
    private final ReservationService reservationService;
    private final MaintenanceService maintenanceService;
    private final InventoryService inventoryService;

    public LabWebServer(Database database, int port) {
        this(database, "0.0.0.0", port);
    }

    public LabWebServer(Database database, String bindHost, int port) {
        this.database = database;
        this.bindHost = bindHost;
        this.port = port;
        this.userDao = new UserDao(database);
        this.equipmentDao = new EquipmentDao(database);
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
            HttpServer server = HttpServer.create(new InetSocketAddress(bindHost, port), 0);
            // Keeping the routes in one small server avoids a heavy MVC framework for this coursework.
            server.createContext("/", this::handleStatic);
            server.createContext("/api/login", api(this::handleLogin));
            server.createContext("/api/register", api(this::handleRegister));
            server.createContext("/api/labs", api(this::handleLabs));
            server.createContext("/api/equipment", api(this::handleEquipment));
            server.createContext("/api/equipment/save", api(this::handleSaveEquipment));
            server.createContext("/api/equipment/retire", api(this::handleRetireEquipment));
            server.createContext("/api/users/technicians", api(this::handleTechnicians));
            server.createContext("/api/reservations", api(this::handleReservations));
            server.createContext("/api/reservations/slots", api(this::handleReservationSlots));
            server.createContext("/api/reservations/create", api(this::handleCreateReservation));
            server.createContext("/api/reservations/decide", api(this::handleDecideReservation));
            server.createContext("/api/reservations/cancel", api(this::handleCancelReservation));
            server.createContext("/api/maintenance", api(this::handleMaintenance));
            server.createContext("/api/maintenance/report", api(this::handleReportProblem));
            server.createContext("/api/maintenance/update", api(this::handleUpdateTicket));
            server.createContext("/api/maintenance/updates", api(this::handleMaintenanceUpdates));
            server.createContext("/api/inventory", api(this::handleInventory));
            server.createContext("/api/inventory/add", api(this::handleAddInventory));
            server.createContext("/api/inventory/change", api(this::handleChangeStock));
            server.createContext("/api/inventory/transactions", api(this::handleStockTransactions));
            server.createContext("/api/reports/lab-usage", api(this::handleLabUsage));
            server.createContext("/api/reports/equipment-status", api(this::handleEquipmentStatusReport));
            server.setExecutor(null);
            server.start();
            System.out.println("Lab Equipment System running at http://" + bindHost + ":" + port);
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
            sendJson(exchange, userJson(user.get(), null));
        } else {
            sendJson(exchange, Json.error("Invalid username or password"));
        }
    }

    private void handleRegister(HttpExchange exchange) throws IOException {
        onlyPost(exchange);
        FormData form = FormData.from(exchange);
        User user = authService.registerStudent(
                form.get("username"),
                form.get("password"),
                form.get("fullName"),
                form.get("email"),
                parseIdList(form.get("labIds"))
        );
        sendJson(exchange, userJson(user, "Registration finished."));
    }

    private void handleEquipment(HttpExchange exchange) throws IOException {
        onlyGet(exchange);
        List<Map<String, ?>> rows = new ArrayList<>();
        String keyword = queryParam(exchange, "q");
        User user = currentUser(exchange);
        List<Equipment> equipment = keyword.isBlank()
                ? equipmentDao.findVisibleFor(user)
                : equipmentDao.searchVisibleFor(user, keyword);
        for (Equipment e : equipment) {
            rows.add(Json.row(
                    "id", e.getId(),
                    "assetTag", e.getAssetTag(),
                    "name", e.getName(),
                    "category", e.getCategory(),
                    "labId", e.getLabId(),
                    "labCode", e.getLabCode(),
                    "status", e.getStatus(),
                    "purchaseDate", e.getPurchaseDate(),
                    "riskLevel", e.getRiskLevel(),
                    "notes", e.getNotes(),
                    "openTicketCount", e.getOpenTicketCount()
            ));
        }
        sendJson(exchange, Json.array(rows));
    }

    private void handleLabs(HttpExchange exchange) throws IOException {
        onlyGet(exchange);
        sendJson(exchange, Json.array(equipmentDao.findLabs()));
    }

    private void handleSaveEquipment(HttpExchange exchange) throws IOException {
        onlyPost(exchange);
        FormData form = FormData.from(exchange);
        // The front-end hides this button for non-admins, and the API checks again here.
        requireAdmin(userFromForm(form));
        Integer equipmentId = form.getOptionalInt("equipmentId");
        String status = form.get("status");
        if (equipmentId == null) {
            if ("MAINTENANCE".equals(status)) {
                throw new IllegalArgumentException("Create equipment as available first, then report a maintenance ticket.");
            }
            int id = equipmentDao.create(
                    form.get("assetTag"),
                    form.get("name"),
                    form.get("category"),
                    form.getInt("labId"),
                    status,
                    form.get("purchaseDate"),
                    form.get("riskLevel"),
                    form.get("notes")
            );
            sendJson(exchange, Json.ok("Equipment #" + id + " saved."));
        } else {
            if ("MAINTENANCE".equals(status) && !maintenanceDao.hasActiveTicketForEquipment(equipmentId)) {
                throw new IllegalArgumentException("Create a maintenance ticket before setting equipment to maintenance.");
            }
            equipmentDao.update(
                    equipmentId,
                    form.get("assetTag"),
                    form.get("name"),
                    form.get("category"),
                    form.getInt("labId"),
                    status,
                    form.get("purchaseDate"),
                    form.get("riskLevel"),
                    form.get("notes")
            );
            sendJson(exchange, Json.ok("Equipment updated."));
        }
    }

    private void handleRetireEquipment(HttpExchange exchange) throws IOException {
        onlyPost(exchange);
        FormData form = FormData.from(exchange);
        requireAdmin(userFromForm(form));
        equipmentDao.retire(form.getInt("equipmentId"));
        sendJson(exchange, Json.ok("Equipment retired."));
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
                    "canCancel", r.getStartTime().isAfter(LocalDateTime.now())
                            && ("PENDING".equals(r.getStatus()) || "APPROVED".equals(r.getStatus())),
                    "status", r.getStatus(),
                    "purpose", r.getPurpose(),
                    "consumableNeeds", r.getConsumableNeeds()
            ));
        }
        sendJson(exchange, Json.array(rows));
    }

    private void handleReservationSlots(HttpExchange exchange) throws IOException {
        onlyGet(exchange);
        FormData query = FormData.fromQuery(exchange.getRequestURI().getRawQuery());
        User user = currentUser(exchange);
        requireStudent(user);
        int equipmentId = query.getInt("equipmentId");
        String rawDate = query.get("startDate");
        LocalDate startDate = rawDate.isBlank() ? LocalDate.now() : LocalDate.parse(rawDate);
        Integer requestedDays = query.getOptionalInt("days");
        int days = requestedDays == null ? 7 : requestedDays;
        sendJson(exchange, Json.array(reservationService.fixedSlotAvailability(
                equipmentId,
                user.getId(),
                startDate,
                days
        )));
    }

    private void handleCreateReservation(HttpExchange exchange) throws IOException {
        onlyPost(exchange);
        FormData form = FormData.from(exchange);
        User user = userFromForm(form);
        requireStudent(user);
        int id = reservationService.requestReservation(
                parseIdList(form.get("equipmentIds")),
                user.getId(),
                LocalDateTime.parse(form.get("startTime"), TIME_FORMAT),
                LocalDateTime.parse(form.get("endTime"), TIME_FORMAT),
                form.get("purpose"),
                parseConsumableRequests(form.get("consumableRequests"))
        );
        sendJson(exchange, Json.ok("Reservation request #" + id + " submitted."));
    }

    private void handleDecideReservation(HttpExchange exchange) throws IOException {
        onlyPost(exchange);
        FormData form = FormData.from(exchange);
        User user = userFromForm(form);
        requireAdmin(user);
        reservationService.decideReservation(
                form.getInt("reservationId"),
                user.getId(),
                Boolean.parseBoolean(form.get("approve")),
                form.get("comment")
        );
        sendJson(exchange, Json.ok("Reservation decision saved."));
    }

    private void handleCancelReservation(HttpExchange exchange) throws IOException {
        onlyPost(exchange);
        FormData form = FormData.from(exchange);
        User user = userFromForm(form);
        requireStudent(user);
        reservationDao.cancel(form.getInt("reservationId"), user.getId());
        sendJson(exchange, Json.ok("Cancel request finished."));
    }

    private void handleMaintenance(HttpExchange exchange) throws IOException {
        onlyGet(exchange);
        requireTechnician(currentUser(exchange));
        List<Map<String, ?>> rows = new ArrayList<>();
        for (MaintenanceTicket t : maintenanceDao.findAll()) {
            rows.add(Json.row(
                    "id", t.getId(),
                    "assetTag", t.getAssetTag(),
                    "equipmentName", t.getEquipmentName(),
                    "reporterName", t.getReporterName(),
                    "technicianId", t.getTechnicianId(),
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
        User user = userFromForm(form);
        int id = maintenanceService.reportProblem(
                form.getInt("equipmentId"),
                user.getId(),
                form.get("title"),
                form.get("description"),
                form.get("priority")
        );
        sendJson(exchange, Json.ok("Maintenance ticket #" + id + " created."));
    }

    private void handleUpdateTicket(HttpExchange exchange) throws IOException {
        onlyPost(exchange);
        FormData form = FormData.from(exchange);
        User user = userFromForm(form);
        requireTechnician(user);
        maintenanceService.handleTicketAction(
                form.getInt("ticketId"),
                form.get("action"),
                user.getId()
        );
        sendJson(exchange, Json.ok("Maintenance ticket updated."));
    }

    private void handleMaintenanceUpdates(HttpExchange exchange) throws IOException {
        onlyGet(exchange);
        requireTechnician(currentUser(exchange));
        int ticketId = FormData.fromQuery(exchange.getRequestURI().getRawQuery()).getInt("ticketId");
        sendJson(exchange, Json.array(maintenanceDao.findUpdatesForTicket(ticketId)));
    }

    private void handleInventory(HttpExchange exchange) throws IOException {
        onlyGet(exchange);
        User user = currentUser(exchange);
        List<Map<String, ?>> rows = new ArrayList<>();
        for (Consumable c : inventoryDao.findVisibleFor(user)) {
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

    private void handleAddInventory(HttpExchange exchange) throws IOException {
        onlyPost(exchange);
        FormData form = FormData.from(exchange);
        User user = userFromForm(form);
        requireAdmin(user);
        inventoryService.addConsumable(
                form.getInt("labId"),
                form.get("itemName"),
                form.get("unit"),
                form.getInt("quantity"),
                form.getInt("reorderLevel"),
                user.getId()
        );
        sendJson(exchange, Json.ok("Consumable item added."));
    }

    private void handleChangeStock(HttpExchange exchange) throws IOException {
        onlyPost(exchange);
        FormData form = FormData.from(exchange);
        User user = userFromForm(form);
        requireInventoryOperator(user);
        inventoryService.changeStock(
                form.getInt("consumableId"),
                user.getId(),
                form.getInt("amount"),
                form.get("reason")
        );
        sendJson(exchange, Json.ok("Stock changed."));
    }

    private void handleStockTransactions(HttpExchange exchange) throws IOException {
        onlyGet(exchange);
        User user = currentUser(exchange);
        requireInventoryOperator(user);
        int consumableId = FormData.fromQuery(exchange.getRequestURI().getRawQuery()).getInt("consumableId");
        sendJson(exchange, Json.array(inventoryDao.findTransactionsForConsumable(consumableId)));
    }

    private void handleLabUsage(HttpExchange exchange) throws IOException {
        onlyGet(exchange);
        requireAdmin(currentUser(exchange));
        sendJson(exchange, reportRows(reportDao.labUsage(), "reservations", "approved", "completed"));
    }

    private void handleEquipmentStatusReport(HttpExchange exchange) throws IOException {
        onlyGet(exchange);
        requireAdmin(currentUser(exchange));
        sendJson(exchange, reportRows(reportDao.equipmentStatusSummary(), "equipmentCount", "highRisk", "openTickets"));
    }

    private String reportRows(List<ReportRow> reportRows, String a, String b, String c) {
        List<Map<String, ?>> rows = new ArrayList<>();
        for (ReportRow row : reportRows) {
            rows.add(Json.row("label", row.getLabel(), a, row.getCountA(), b, row.getCountB(), c, row.getCountC()));
        }
        return Json.array(rows);
    }

    private String userJson(User user, String message) {
        // Login and registration return the same shape so app.js can reuse enterApp().
        Map<String, Object> values = Json.row(
                "ok", true,
                "id", user.getId(),
                "username", user.getUsername(),
                "fullName", user.getFullName(),
                "role", user.getRole()
        );
        if (message != null) {
            values.put("message", message);
        }
        return Json.object(values);
    }

    private User currentUser(HttpExchange exchange) {
        FormData query = FormData.fromQuery(exchange.getRequestURI().getRawQuery());
        int userId = query.getInt("userId");
        return userDao.findById(userId).orElseThrow(() -> new IllegalArgumentException("Unknown user id: " + userId));
    }

    private User userFromForm(FormData form) {
        int userId = form.getInt("userId");
        return userDao.findById(userId).orElseThrow(() -> new IllegalArgumentException("Unknown user id: " + userId));
    }

    private void requireAdmin(User user) {
        if (!user.isAdmin()) {
            throw new IllegalArgumentException("Admin permission required");
        }
    }

    private void requireStudent(User user) {
        if (!"STUDENT".equals(user.getRole())) {
            throw new IllegalArgumentException("Only students can create reservations.");
        }
    }

    private void requireTechnician(User user) {
        if (!"TECHNICIAN".equals(user.getRole())) {
            throw new IllegalArgumentException("Technician permission required");
        }
    }

    private void requireInventoryOperator(User user) {
        if (!user.isAdmin() && !"TECHNICIAN".equals(user.getRole())) {
            throw new IllegalArgumentException("Inventory permission required");
        }
    }

    private List<Integer> parseIdList(String raw) {
        List<Integer> ids = new ArrayList<>();
        if (raw == null || raw.isBlank()) {
            return ids;
        }
        for (String part : raw.split(",")) {
            String value = part.trim();
            if (!value.isEmpty()) {
                ids.add(Integer.parseInt(value));
            }
        }
        return ids;
    }

    private Map<Integer, Integer> parseConsumableRequests(String raw) {
        Map<Integer, Integer> requests = new LinkedHashMap<>();
        if (raw == null || raw.isBlank()) {
            return requests;
        }
        for (String part : raw.split(",")) {
            String value = part.trim();
            if (value.isEmpty()) {
                continue;
            }
            String[] pieces = value.split(":");
            if (pieces.length != 2) {
                throw new IllegalArgumentException("Bad consumable request: " + value);
            }
            int consumableId = Integer.parseInt(pieces[0]);
            int quantity = Integer.parseInt(pieces[1]);
            if (quantity > 0) {
                requests.put(consumableId, quantity);
            }
        }
        return requests;
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
