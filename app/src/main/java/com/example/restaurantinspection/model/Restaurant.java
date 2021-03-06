package com.example.restaurantinspection.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    private boolean favourite = false;

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

    public Restaurant(LatLng latLng, String title, String snippet,boolean isFavourite) {
        this.mPosition = latLng;
        this.mTitle = title;
        this.mSnippet = snippet;
        this.favourite = isFavourite;
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

    public int getTotalViolationsWithinYear(){
        if(restaurantInspectionList.size() == 0){
            return 0;
        }
        int count = 0;
        for(RestaurantInspection inspection : restaurantInspectionList){
            Date date = DateManager.dateCreate(inspection.getInspectionDate());
            boolean contributes_ToCounter = DateManager.check_ifOver_AYear(date);
            if(! contributes_ToCounter) {
                break;
            }
            count += inspection.getNumCritical();
        }
        return count;
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
        return mTitle;
    }

    @Override
    public String getSnippet() {
        return mSnippet;
    }

    public void setFavourite(boolean Boolean){favourite = Boolean;}

    public boolean getFavourite(){return favourite;}
}
