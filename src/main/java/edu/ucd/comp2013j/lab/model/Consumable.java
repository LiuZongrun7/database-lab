package edu.ucd.comp2013j.lab.model;

public class Consumable {
    private final int id;
    private final String labCode;
    private final String itemName;
    private final String unit;
    private final int quantity;
    private final int reorderLevel;

    public Consumable(int id, String labCode, String itemName, String unit, int quantity, int reorderLevel) {
        this.id = id;
        this.labCode = labCode;
        this.itemName = itemName;
        this.unit = unit;
        this.quantity = quantity;
        this.reorderLevel = reorderLevel;
    }

    public int getId() {
        return id;
    }

    public String getLabCode() {
        return labCode;
    }

    public String getItemName() {
        return itemName;
    }

    public String getUnit() {
        return unit;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }

    public boolean isLowStock() {
        return quantity <= reorderLevel;
    }
}
