package com.example.restaurantinspection.model;

public class Restaurant {

    private InspectionManager inspectionManager;

    private String trackingNumber;
    private String name;
    private String address;
    private String city;
    private String facilityType;
    private String latitude;
    private String longitude;

    private int num_critical;
    private int num_noncritical;

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

    public InspectionManager getInspectionManager(){
        return inspectionManager;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getFacilityType() {
        return facilityType;
    }

    public void setFacilityType(String facilityType) {
        this.facilityType = facilityType;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public int getNum_critical() {
        return num_critical;
    }

    public void addNum_critical(int num_critical) {
        this.num_critical += num_critical;
    }

    public int getNum_noncritical() {
        return num_noncritical;
    }

    public void addNum_noncritical(int num_noncritical) {
        this.num_noncritical += num_noncritical;
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
