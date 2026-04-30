package edu.ucd.comp2013j.lab.model;

public class ReportRow {
    private final String label;
    private final int countA;
    private final int countB;
    private final int countC;

    public ReportRow(String label, int countA, int countB, int countC) {
        this.label = label;
        this.countA = countA;
        this.countB = countB;
        this.countC = countC;
    }

    public String getLabel() {
        return label;
    }

    public int getCountA() {
        return countA;
    }

    public int getCountB() {
        return countB;
    }

    public int getCountC() {
        return countC;
    }
}
