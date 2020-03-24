package com.example.restaurantinspection.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Restaurant class models the information about a
 * restaurant. Data includes tracking number, name
 * address, city, facility type, latitude and longitude,
 * number of critical and non-critical issues, and a list
 * of associated inspections.
 */

public class Restaurant implements ClusterItem {

    private List<RestaurantInspection> restaurantInspectionList = new ArrayList<>();

    private String trackingNumber;
    private String name;
    private String address;
    private String city;
    private String facilityType;
    private String latitude;
    private String longitude;
    private LatLng mPosition;
    private String mTitle;
    private String mSnippet;

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

    public Restaurant(double lat, double lon, String title, String snippet){
        mPosition = new LatLng(lat, lon);
        mTitle = title;
        mSnippet = snippet;
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

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return getName();
    }

    @Override
    public String getSnippet() {
        if(this.getRestaurantInspectionList().size() != 0) {
            return address + "\n" + "Hazard Rating: " + this.getRestaurantInspectionList().get(0).getHazardRating();
        }else{
            return null;
        }
    }
}
