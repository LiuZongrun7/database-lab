package edu.ucd.comp2013j.lab.model;

public class Course {
    private final int id;
    private final String code;
    private final String name;

    public Course(int id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return code + " - " + name;
    }
}
