package com.example.restaurantinspection.model;

import java.util.Comparator;

public class InspectionComparator implements Comparator<RestaurantInspection> {

    @Override
    public int compare(RestaurantInspection inspection, RestaurantInspection otherInspection) {
        return otherInspection.getInspectionDate().compareTo(inspection.getInspectionDate());
    }
}
