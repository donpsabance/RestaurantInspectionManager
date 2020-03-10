package com.example.restaurantinspection.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * RestaurantInspection class models the information about an inspection.
 * Data includes tracking number, date (in string and Date format),
 * type, hazard rating, violations, number of critical and
 * non-critical issues, and a list of associated violations.
 */

public class RestaurantInspection implements Comparable<RestaurantInspection> {

    private List<Violation> violationsList = new ArrayList<>();

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
        parseViolations(violations);
    }

    private void parseViolations(String violationdump) {
        String[] arr = violations.split("\\|");
        for (String s : arr) {
            Log.d("TAG", s);

            //if s == "No violations" do not add to list
            if (!violationdump.equalsIgnoreCase("No violations")) {
                // TODO: for each string s, parse the violation # and Critical/NonCritical
                String[] arr2 = s.split("!");
                //Log.d("Head",arr2[0]+' '+arr2[1]+' '+arr2[2]+' '+arr2[3]);
                int violationnum = Integer.parseInt(arr2[0]);

                //create new violation here
                Violation violation = new Violation(violationnum, arr2[1], s);
                violationsList.add(violation);
            }
        }
    }

    public List<Violation> getViolationsList() {
        return violationsList;
    }

    public Date getFormatDate() {
        return formatDate;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public String getInspectionDate() {
        return inspectionDate;
    }

    public String getInspectionType() {
        return inspectionType;
    }

    public String getHazardRating() {
        return hazardRating;
    }

    public String getViolations() {
        return violations;
    }

    public int getNumCritical() {
        return numCritical;
    }

    public int getNumNonCritical() {
        return numNonCritical;
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


    @Override
    public int compareTo(RestaurantInspection o) {
        if (getInspectionDate() == null || o.getInspectionDate() == null) {
            return 0;
        }
        return getFormatDate().compareTo(o.getFormatDate());
    }
}
