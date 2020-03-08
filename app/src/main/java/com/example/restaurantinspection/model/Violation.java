package com.example.restaurantinspection.model;

public class Violation {
    private int violation_id;
    private String status;
    private  String violationDump;

    public Violation(String violationDump) {
        this.violationDump = violationDump;
    }

    public Violation(int violation_id, String status, String violationDump) {
        this.violation_id = violation_id;
        this.status = status;
        this.violationDump = violationDump;
    }

    public int getViolation_id() {
        return violation_id;
    }

    public void setViolation_id(int violation_id) {
        this.violation_id = violation_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
