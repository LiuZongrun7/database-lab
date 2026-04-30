package edu.ucd.comp2013j.lab.model;

public class Equipment {
    private final int id;
    private final String assetTag;
    private final String name;
    private final String category;
    private final String labCode;
    private final String status;
    private final String riskLevel;
    private final int openTicketCount;

    public Equipment(int id, String assetTag, String name, String category, String labCode,
                     String status, String riskLevel, int openTicketCount) {
        this.id = id;
        this.assetTag = assetTag;
        this.name = name;
        this.category = category;
        this.labCode = labCode;
        this.status = status;
        this.riskLevel = riskLevel;
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

    public String getLabCode() {
        return labCode;
    }

    public String getStatus() {
        return status;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public int getOpenTicketCount() {
        return openTicketCount;
    }

    @Override
    public String toString() {
        return assetTag + " - " + name;
    }
}
