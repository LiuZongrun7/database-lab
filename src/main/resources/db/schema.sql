DROP VIEW IF EXISTS v_equipment_status;
DROP VIEW IF EXISTS v_lab_usage_report;
DROP VIEW IF EXISTS v_user_reservation_history;

DROP TABLE IF EXISTS equipment_course_access;
DROP TABLE IF EXISTS course_members;
DROP TABLE IF EXISTS stock_transactions;
DROP TABLE IF EXISTS reservation_consumables;
DROP TABLE IF EXISTS consumables;
DROP TABLE IF EXISTS maintenance_updates;
DROP TABLE IF EXISTS maintenance_tickets;
DROP TABLE IF EXISTS approvals;
DROP TABLE IF EXISTS reservation_equipment;
DROP TABLE IF EXISTS reservations;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS equipment;
DROP TABLE IF EXISTS student_labs;
DROP TABLE IF EXISTS labs;
DROP TABLE IF EXISTS users;

-- Users cover three real system roles. Self-registration only creates STUDENT rows.
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(40) NOT NULL UNIQUE,
    password VARCHAR(80) NOT NULL,
    full_name VARCHAR(80) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'STUDENT', 'TECHNICIAN')),
    penalty_points INT NOT NULL DEFAULT 0 CHECK (penalty_points >= 0),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Labs are separate from equipment so reports can group by lab.
CREATE TABLE labs (
    lab_id INT AUTO_INCREMENT PRIMARY KEY,
    lab_code VARCHAR(20) NOT NULL UNIQUE,
    lab_name VARCHAR(100) NOT NULL,
    building VARCHAR(60) NOT NULL,
    room VARCHAR(30) NOT NULL,
    capacity INT NOT NULL CHECK (capacity > 0),
    manager_id INT,
    CONSTRAINT fk_lab_manager FOREIGN KEY (manager_id) REFERENCES users(user_id)
);

-- Equipment is kept even after retirement so historical reservations and tickets stay valid.
CREATE TABLE equipment (
    equipment_id INT AUTO_INCREMENT PRIMARY KEY,
    asset_tag VARCHAR(30) NOT NULL UNIQUE,
    equipment_name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    lab_id INT NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('AVAILABLE', 'RESERVED', 'IN_USE', 'MAINTENANCE', 'RETIRED')),
    purchase_date DATE,
    risk_level VARCHAR(15) NOT NULL CHECK (risk_level IN ('LOW', 'MEDIUM', 'HIGH')),
    notes VARCHAR(255),
    CONSTRAINT fk_equipment_lab FOREIGN KEY (lab_id) REFERENCES labs(lab_id)
);

-- Students may belong to multiple labs. This controls which equipment they can reserve.
CREATE TABLE student_labs (
    user_id INT NOT NULL,
    lab_id INT NOT NULL,
    PRIMARY KEY (user_id, lab_id),
    CONSTRAINT fk_student_lab_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_student_lab_lab FOREIGN KEY (lab_id) REFERENCES labs(lab_id) ON DELETE CASCADE
);

CREATE TABLE reservations (
    reservation_id INT AUTO_INCREMENT PRIMARY KEY,
    requester_id INT NOT NULL,
    equipment_id INT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    purpose VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED', 'COMPLETED', 'NO_SHOW')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_reservation_time CHECK (end_time > start_time),
    CONSTRAINT fk_res_requester FOREIGN KEY (requester_id) REFERENCES users(user_id),
    CONSTRAINT fk_res_equipment FOREIGN KEY (equipment_id) REFERENCES equipment(equipment_id)
);

-- Approval history is separate from the current reservation status.
CREATE TABLE approvals (
    approval_id INT AUTO_INCREMENT PRIMARY KEY,
    reservation_id INT NOT NULL,
    approver_id INT NOT NULL,
    decision VARCHAR(20) NOT NULL CHECK (decision IN ('APPROVED', 'REJECTED')),
    decision_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    comment VARCHAR(255),
    CONSTRAINT fk_approval_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id) ON DELETE CASCADE,
    CONSTRAINT fk_approval_user FOREIGN KEY (approver_id) REFERENCES users(user_id)
);

-- Maintenance tickets track the fault; maintenance_updates keeps later progress notes.
CREATE TABLE maintenance_tickets (
    ticket_id INT AUTO_INCREMENT PRIMARY KEY,
    equipment_id INT NOT NULL,
    reporter_id INT NOT NULL,
    technician_id INT,
    title VARCHAR(120) NOT NULL,
    description VARCHAR(500) NOT NULL,
    priority VARCHAR(15) NOT NULL CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED')),
    reported_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP,
    CONSTRAINT fk_ticket_equipment FOREIGN KEY (equipment_id) REFERENCES equipment(equipment_id),
    CONSTRAINT fk_ticket_reporter FOREIGN KEY (reporter_id) REFERENCES users(user_id),
    CONSTRAINT fk_ticket_technician FOREIGN KEY (technician_id) REFERENCES users(user_id)
);

CREATE TABLE maintenance_updates (
    update_id INT AUTO_INCREMENT PRIMARY KEY,
    ticket_id INT NOT NULL,
    user_id INT NOT NULL,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_text VARCHAR(500) NOT NULL,
    CONSTRAINT fk_update_ticket FOREIGN KEY (ticket_id) REFERENCES maintenance_tickets(ticket_id) ON DELETE CASCADE,
    CONSTRAINT fk_update_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- consumables stores current stock, while stock_transactions stores the history.
CREATE TABLE consumables (
    consumable_id INT AUTO_INCREMENT PRIMARY KEY,
    lab_id INT NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    quantity INT NOT NULL CHECK (quantity >= 0),
    reorder_level INT NOT NULL CHECK (reorder_level >= 0),
    CONSTRAINT uq_consumable_lab_item UNIQUE (lab_id, item_name),
    CONSTRAINT fk_consumable_lab FOREIGN KEY (lab_id) REFERENCES labs(lab_id)
);

-- These are requested consumables for a reservation. They do not directly change stock.
CREATE TABLE reservation_consumables (
    reservation_id INT NOT NULL,
    consumable_id INT NOT NULL,
    requested_quantity INT NOT NULL CHECK (requested_quantity > 0),
    PRIMARY KEY (reservation_id, consumable_id),
    CONSTRAINT fk_rc_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id) ON DELETE CASCADE,
    CONSTRAINT fk_rc_consumable FOREIGN KEY (consumable_id) REFERENCES consumables(consumable_id)
);

CREATE TABLE stock_transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    consumable_id INT NOT NULL,
    user_id INT NOT NULL,
    change_amount INT NOT NULL,
    reason VARCHAR(160) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_stock_consumable FOREIGN KEY (consumable_id) REFERENCES consumables(consumable_id),
    CONSTRAINT fk_stock_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE INDEX idx_reservation_time ON reservations(start_time, end_time);
CREATE INDEX idx_reservation_slot_lookup ON reservations(equipment_id, start_time, end_time);
CREATE INDEX idx_reservation_status ON reservations(status);
CREATE INDEX idx_ticket_status ON maintenance_tickets(status);
CREATE INDEX idx_equipment_status ON equipment(status);

-- View for the equipment page: equipment + lab + number of open repair tickets.
CREATE VIEW v_equipment_status AS
SELECT
    e.equipment_id,
    e.asset_tag,
    e.equipment_name,
    e.category,
    e.lab_id,
    e.status,
    e.purchase_date,
    e.risk_level,
    e.notes,
    l.lab_code,
    l.lab_name,
    COALESCE(open_tickets.open_count, 0) AS open_ticket_count
FROM equipment e
JOIN labs l ON e.lab_id = l.lab_id
LEFT JOIN (
    SELECT equipment_id, COUNT(*) AS open_count
    FROM maintenance_tickets
    WHERE status IN ('OPEN', 'IN_PROGRESS')
    GROUP BY equipment_id
) open_tickets ON e.equipment_id = open_tickets.equipment_id;

-- View for reservation history: one row per reservation, with equipment and consumables combined.
CREATE VIEW v_user_reservation_history AS
SELECT
    r.reservation_id,
    r.requester_id,
    u.full_name,
    u.role,
    e.asset_tag AS asset_tags,
    e.equipment_name AS equipment_names,
    r.start_time,
    r.end_time,
    r.status,
    r.purpose,
    COALESCE(GROUP_CONCAT(DISTINCT CONCAT(c.item_name, ' x', rc.requested_quantity) ORDER BY c.item_name SEPARATOR ', '), '') AS consumable_needs
FROM reservations r
JOIN users u ON r.requester_id = u.user_id
JOIN equipment e ON r.equipment_id = e.equipment_id
LEFT JOIN reservation_consumables rc ON r.reservation_id = rc.reservation_id
LEFT JOIN consumables c ON rc.consumable_id = c.consumable_id
GROUP BY r.reservation_id, r.requester_id, u.full_name, u.role, e.asset_tag, e.equipment_name, r.start_time, r.end_time, r.status, r.purpose;

-- View for reports: lab usage is counted through each reservation's single equipment item.
CREATE VIEW v_lab_usage_report AS
SELECT
    l.lab_code,
    l.lab_name,
    COUNT(DISTINCT r.reservation_id) AS reservation_count,
    COUNT(DISTINCT CASE WHEN r.status = 'APPROVED' THEN r.reservation_id END) AS approved_count,
    COUNT(DISTINCT CASE WHEN r.status = 'COMPLETED' THEN r.reservation_id END) AS completed_count
FROM labs l
LEFT JOIN equipment e ON l.lab_id = e.lab_id
LEFT JOIN reservations r ON e.equipment_id = r.equipment_id
GROUP BY l.lab_code, l.lab_name;
