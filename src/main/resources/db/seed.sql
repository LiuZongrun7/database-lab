INSERT INTO users (username, password, full_name, email, role, penalty_points) VALUES
('admin', 'admin123', 'Alice Murphy', 'alice.murphy@university.edu', 'ADMIN', 0),
('teacher', 'teacher123', 'Brian O Connor', 'brian.oconnor@university.edu', 'TEACHER', 0),
('student1', 'student123', 'Chen Wei', 'chen.wei@student.edu', 'STUDENT', 1),
('student2', 'student123', 'Emma Zhang', 'emma.zhang@student.edu', 'STUDENT', 0),
('tech', 'tech123', 'Daniel Walsh', 'daniel.walsh@university.edu', 'TECHNICIAN', 0);

INSERT INTO labs (lab_code, lab_name, building, room, capacity, manager_id) VALUES
('AI-LAB', 'Artificial Intelligence Laboratory', 'Engineering Building', 'E203', 28, 2),
('BIO-LAB', 'Bio-signal Computing Laboratory', 'Science Centre', 'S114', 18, 2),
('NET-LAB', 'Networks and IoT Laboratory', 'Engineering Building', 'E305', 24, 1);

INSERT INTO equipment (asset_tag, equipment_name, category, lab_id, status, purchase_date, risk_level, notes) VALUES
('AI-GPU-001', 'GPU Workstation A', 'Computing', 1, 'AVAILABLE', '2024-09-12', 'MEDIUM', 'CUDA workstation for deep learning labs'),
('AI-ROBOT-002', 'Mobile Robot TurtleBot', 'Robotics', 1, 'AVAILABLE', '2023-11-02', 'HIGH', 'Needs supervisor permission'),
('BIO-OSC-003', 'Digital Oscilloscope', 'Measurement', 2, 'AVAILABLE', '2022-04-15', 'LOW', 'Shared electronic measuring device'),
('BIO-ECG-004', 'ECG Sensor Kit', 'Sensor', 2, 'MAINTENANCE', '2021-06-20', 'MEDIUM', 'Loose cable reported'),
('NET-IOT-005', 'IoT Gateway Set', 'Network', 3, 'AVAILABLE', '2024-01-18', 'LOW', 'Raspberry Pi and gateway kit'),
('NET-SW-006', 'Managed Switch Rack', 'Network', 3, 'MAINTENANCE', '2022-10-01', 'MEDIUM', 'Used in network configuration exercises');

INSERT INTO courses (course_code, course_name, teacher_id) VALUES
('COMP2013J', 'Databases and Information Systems', 2),
('COMP3030J', 'Machine Learning Engineering', 2);

INSERT INTO course_members (course_id, user_id, member_role) VALUES
(1, 2, 'TEACHER'),
(1, 3, 'STUDENT'),
(1, 4, 'STUDENT'),
(2, 2, 'TEACHER'),
(2, 3, 'STUDENT');

INSERT INTO equipment_course_access (equipment_id, course_id) VALUES
(1, 2),
(2, 2),
(3, 1),
(5, 1),
(6, 1);

INSERT INTO reservations (requester_id, course_id, start_time, end_time, purpose, status) VALUES
(3, 1, '2026-05-20 10:00:00', '2026-05-20 12:00:00', 'Measure sensor output for lab exercise', 'APPROVED'),
(4, 1, '2026-05-21 14:00:00', '2026-05-21 16:30:00', 'IoT gateway demo preparation', 'PENDING'),
(3, 2, '2026-05-22 09:00:00', '2026-05-22 11:00:00', 'Train small image classifier', 'APPROVED');

INSERT INTO reservation_equipment (reservation_id, equipment_id) VALUES
(1, 3),
(2, 5),
(2, 3),
(3, 1),
(3, 2);

INSERT INTO approvals (reservation_id, approver_id, decision, comment) VALUES
(1, 2, 'APPROVED', 'Approved for scheduled lab work'),
(3, 2, 'APPROVED', 'Use the workstation for two hours only');

INSERT INTO maintenance_tickets (equipment_id, reporter_id, technician_id, title, description, priority, status) VALUES
(4, 3, 5, 'ECG cable is loose', 'The signal drops when the sensor cable is moved.', 'HIGH', 'IN_PROGRESS'),
(6, 4, NULL, 'Switch fan noise', 'The switch fan is louder than usual during long lab sessions.', 'MEDIUM', 'OPEN');

INSERT INTO maintenance_updates (ticket_id, user_id, update_text) VALUES
(1, 5, 'Checked the cable connector. Need to order a replacement cable.'),
(2, 4, 'Reported after network lab practice.');

INSERT INTO consumables (lab_id, item_name, unit, quantity, reorder_level) VALUES
(1, 'Robot battery pack', 'piece', 6, 2),
(2, 'ECG electrode pad', 'pack', 8, 5),
(3, 'Ethernet cable', 'piece', 36, 10),
(3, 'Micro SD card', 'piece', 12, 4);

INSERT INTO reservation_consumables (reservation_id, consumable_id, requested_quantity) VALUES
(1, 2, 1),
(2, 3, 2),
(2, 4, 1),
(3, 1, 1);

INSERT INTO stock_transactions (consumable_id, user_id, change_amount, reason) VALUES
(1, 1, 6, 'Initial stock count'),
(2, 1, 8, 'Initial stock count'),
(3, 1, 36, 'Initial stock count'),
(4, 1, 12, 'Initial stock count');
