package edu.ucd.comp2013j.lab.model;

import java.time.LocalDateTime;

public class Reservation {
    private final int id;
    private final String assetTag;
    private final String equipmentName;
    private final String requesterName;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final String purpose;
    private final String status;

    public Reservation(int id, String assetTag, String equipmentName, String requesterName,
                       LocalDateTime startTime, LocalDateTime endTime, String purpose, String status) {
        this.id = id;
        this.assetTag = assetTag;
        this.equipmentName = equipmentName;
        this.requesterName = requesterName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.purpose = purpose;
        this.status = status;
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

    public String getRequesterName() {
        return requesterName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getPurpose() {
        return purpose;
    }

    public String getStatus() {
        return status;
    }
}
