package com.example.restaurantinspection.model;

/**
 * Violation class models the information about a
 * violation. Data includes id, status and violation dump (description).
 */


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

    public String getViolationDump() {
        return violationDump;
    }

    public int getViolation_id() {
        return violation_id;
    }

    public String getStatus() {
        return status;
    }
}
