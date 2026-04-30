package edu.ucd.comp2013j.lab.model;

import java.time.LocalDateTime;

public class MaintenanceTicket {
    private final int id;
    private final String assetTag;
    private final String equipmentName;
    private final String reporterName;
    private final String technicianName;
    private final String title;
    private final String priority;
    private final String status;
    private final LocalDateTime reportedAt;

    public MaintenanceTicket(int id, String assetTag, String equipmentName, String reporterName,
                             String technicianName, String title, String priority,
                             String status, LocalDateTime reportedAt) {
        this.id = id;
        this.assetTag = assetTag;
        this.equipmentName = equipmentName;
        this.reporterName = reporterName;
        this.technicianName = technicianName;
        this.title = title;
        this.priority = priority;
        this.status = status;
        this.reportedAt = reportedAt;
    }

    public int getId() {
        return id;
    }

    public String getAssetTag() {
        return assetTag;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public String getReporterName() {
        return reporterName;
    }

    public String getTechnicianName() {
        return technicianName;
    }

    public String getTitle() {
        return title;
    }

    public String getPriority() {
        return priority;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getReportedAt() {
        return reportedAt;
    }
}
