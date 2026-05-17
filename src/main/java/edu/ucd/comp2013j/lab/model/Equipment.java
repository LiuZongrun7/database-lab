package edu.ucd.comp2013j.lab.model;

public class Equipment {
    private final int id;
    private final String assetTag;
    private final String name;
    private final String category;
    private final int labId;
    private final String labCode;
    private final String status;
    private final String purchaseDate;
    private final String riskLevel;
    private final String notes;
    private final int openTicketCount;

    public Equipment(int id, String assetTag, String name, String category, String labCode,
                     String status, String riskLevel, int openTicketCount) {
        this(id, assetTag, name, category, 0, labCode, status, "", riskLevel, "", openTicketCount);
    }

    public Equipment(int id, String assetTag, String name, String category, int labId, String labCode,
                     String status, String purchaseDate, String riskLevel, String notes, int openTicketCount) {
        this.id = id;
        this.assetTag = assetTag;
        this.name = name;
        this.category = category;
        this.labId = labId;
        this.labCode = labCode;
        this.status = status;
        this.purchaseDate = purchaseDate;
        this.riskLevel = riskLevel;
        this.notes = notes;
        this.openTicketCount = openTicketCount;
    }

    public int getId() {
        return id;
    }

    public String getAssetTag() {
        return assetTag;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public int getLabId() {
        return labId;
    }

    public String getLabCode() {
        return labCode;
    }

    public String getStatus() {
        return status;
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public String getNotes() {
        return notes;
    }

    public int getOpenTicketCount() {
        return openTicketCount;
    }

    @Override
    public String toString() {
        return assetTag + " - " + name;
    }
}
