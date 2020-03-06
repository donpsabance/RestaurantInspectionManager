package com.example.restaurantinspection.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RestaurantInspection implements Comparable<RestaurantInspection> {

    private String trackingNumber;
    private String inspectionDate;
    private String inspectionType;
    private int numCritical;
    private int numNonCritical;
    private String hazardRating;
    private String violations;
    private Date formatDate;

    public RestaurantInspection(String trackingNumber, String inspectionDate,
                                String inspectionType, String numCritical, String numNonCritical,
                                String hazardRating, String violations) {
        this.trackingNumber = trackingNumber;
        this.inspectionDate = inspectionDate;
        this.inspectionType = inspectionType;
        this.numCritical = Integer.parseInt(numCritical);
        this.numNonCritical = Integer.parseInt(numNonCritical);
        this.hazardRating = hazardRating;
        this.violations = violations;
    }

    public Date getFormatDate(){return formatDate;}

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getInspectionDate() {
        return inspectionDate;
    }



    public void setInspectionDate(String inspectionDate) {
        this.inspectionDate = inspectionDate;
    }

    public String getInspectionType() {
        return inspectionType;
    }

    public void setInspectionType(String inspectionType) {
        this.inspectionType = inspectionType;
    }

    public String getHazardRating() {
        return hazardRating;
    }

    public void setHazardRating(String hazardRating) {
        this.hazardRating = hazardRating;
    }

    public String getViolations() {
        return violations;
    }

    public void setViolations(String violations) {
        this.violations = violations;
    }

    public int getNumCritical() {
        return numCritical;
    }

    public void setNumCritical(int numCritical) {
        this.numCritical = numCritical;
    }

    public int getNumNonCritical() {
        return numNonCritical;
    }

    public void setNumNonCritical(int numNonCritical) {
        this.numNonCritical = numNonCritical;
    }

    @Override
    public String toString() {
        return "RestaurantInspection{" +
                "trackingNumber='" + trackingNumber + '\'' +
                ", inspectionDate='" + inspectionDate + '\'' +
                ", inspectionType='" + inspectionType + '\'' +
                ", numCritical=" + numCritical +
                ", numNonCritical=" + numNonCritical +
                ", hazardRating='" + hazardRating + '\'' +
                ", violations='" + violations + '\'' +
                '}';
    }


    public Date convertStringtoDate(String string) throws ParseException {
        DateFormat format = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
        formatDate = format.parse(string);
        return formatDate;
    }

    @Override
    public int compareTo(RestaurantInspection o) {
        if (getInspectionDate() == null || o.getInspectionDate() == null){
            return 0;
        }
        return getFormatDate().compareTo(o.getFormatDate());
    }
}
