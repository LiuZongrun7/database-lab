package edu.ucd.comp2013j.lab.model;

import java.time.LocalDateTime;

public class Reservation {
    private final int id;
    private final String assetTags;
    private final String equipmentNames;
    private final String requesterName;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final String purpose;
    private final String consumableNeeds;
    private final String status;

    public Reservation(int id, String assetTags, String equipmentNames, String requesterName,
                       LocalDateTime startTime, LocalDateTime endTime, String purpose,
                       String consumableNeeds, String status) {
        this.id = id;
        this.assetTags = assetTags;
        this.equipmentNames = equipmentNames;
        this.requesterName = requesterName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.purpose = purpose;
        this.consumableNeeds = consumableNeeds;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getAssetTag() {
        return assetTags;
    }

    public String getEquipmentName() {
        return equipmentNames;
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

    public String getConsumableNeeds() {
        return consumableNeeds;
    }

    public String getStatus() {
        return status;
    }
}
