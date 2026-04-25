package com.example.attendance.model;

/**
 * Model class representing a Student with authentication data.
 * Contains roll number, name, and branch.
 */
public class Student {
    private int rollNo;
    private String name;
    private String branch;

    public Student() {
    }

    public Student(int rollNo, String name, String branch) {
        this.rollNo = rollNo;
        this.name = name;
        this.branch = branch;
    }

    public int getRollNo() {
        return rollNo;
    }

    public void setRollNo(int rollNo) {
        this.rollNo = rollNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }
}