package com.example.attendance.model;

/**
 * Model class representing a Subject with attendance data.
 * Contains id, name, total classes conducted, and classes attended.
 */
public class Subject {
    private int id;
    private String name;
    private String courseCode;
    private int total;
    private int attended;

    /**
     * Default constructor
     */
    public Subject() {
    }

    /**
     * Constructor with name, courseCode, total, attended
     * @param name Subject name
     * @param courseCode Course code
     * @param total Total classes conducted
     * @param attended Classes attended
     */
    public Subject(String name, String courseCode, int total, int attended) {
        this.name = name;
        this.courseCode = courseCode;
        this.total = total;
        this.attended = attended;
    }

    /**
     * Constructor with all fields including id
     * @param id Subject ID
     * @param name Subject name
     * @param courseCode Course code
     * @param total Total classes conducted
     * @param attended Classes attended
     */
    public Subject(int id, String name, String courseCode, int total, int attended) {
        this.id = id;
        this.name = name;
        this.courseCode = courseCode;
        this.total = total;
        this.attended = attended;
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getAttended() {
        return attended;
    }

    public void setAttended(int attended) {
        this.attended = attended;
    }
}