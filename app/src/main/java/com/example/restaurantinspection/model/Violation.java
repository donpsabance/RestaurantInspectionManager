package com.example.restaurantinspection.model;

public class Violation {
    private int violation_id;
    private String status;
    private  String wholeString;

    public Violation(String wholeString) {
        this.wholeString = wholeString;
    }

    public Violation(int violation_id, String status, String wholeString) {
        this.violation_id = violation_id;
        this.status = status;
        this.wholeString = wholeString;
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
