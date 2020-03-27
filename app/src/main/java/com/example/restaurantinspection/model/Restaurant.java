package com.example.restaurantinspection.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Restaurant class models the information about a
 * restaurant. Data includes tracking number, name
 * address, city, facility type, latitude and longitude,
 * number of critical and non-critical issues, and a list
 * of associated inspections.
 */

public class Restaurant {

    private List<RestaurantInspection> restaurantInspectionList = new ArrayList<>();

    private String trackingNumber;
    private String name;
    private String address;
    private String city;
    private String facilityType;
    private String latitude;
    private String longitude;

    public Restaurant(String trackingNumber, String name, String address, String city,
                      String facilityType, String latitude, String longitude) {
        this.trackingNumber = trackingNumber;
        this.name = name;
        this.address = address;
        this.city = city;
        this.facilityType = facilityType;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public List<RestaurantInspection> getRestaurantInspectionList() {
        return restaurantInspectionList;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public boolean containsInspection(String inspectionDate){
        boolean shouldAddInspection = true;
        for(RestaurantInspection restaurantInspection : restaurantInspectionList){
            if (restaurantInspection.getInspectionDate().equals(inspectionDate)) {
                shouldAddInspection = false;
                break;
            }
        }
        return shouldAddInspection;
    }

    @Override
    public String toString() {
        return "Restaurant{" +
                "trackingNumber='" + trackingNumber + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                ", facilityType='" + facilityType + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                '}';
    }
}
