package com.jebstern.lahinotto;


import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MarkerBean implements ClusterItem {

    String model;
    String number;
    String address;
    String postalCode;
    String postOffice;
    String type;
    String locationPlace;
    String openingHours;
    String extraDetails;
    double latitude;
    double longitude;
    private final LatLng position;

    public MarkerBean(String model, String number, String address, String postalCode, String postOffice, String type, String locationPlace, String openingHours, String extraDetails, double latitude, double longitude) {
        this.model = model;
        this.number = number;
        this.address = address;
        this.postalCode = postalCode;
        this.postOffice = postOffice;
        this.type = type;
        this.locationPlace = locationPlace;
        this.openingHours = openingHours;
        this.extraDetails = extraDetails;
        this.latitude = latitude;
        this.longitude = longitude;
        position = new LatLng(latitude, longitude);
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getPostOffice() {
        return postOffice;
    }

    public void setPostOffice(String postOffice) {
        this.postOffice = postOffice;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocationPlace() {
        return locationPlace;
    }

    public void setLocationPlace(String locationPlace) {
        this.locationPlace = locationPlace;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }

    public String getExtraDetails() {
        return extraDetails;
    }

    public void setExtraDetails(String extraDetails) {
        this.extraDetails = extraDetails;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
