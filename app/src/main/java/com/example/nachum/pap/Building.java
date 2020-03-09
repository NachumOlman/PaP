package com.example.nachum.pap;

import java.util.ArrayList;
import java.util.List;

public class Building {

    private Double lat;
    private Double lng;
    private String markerId;
    private String latlng;
    private String type;
    private String location;
    private String pic;
    private String address;
    private List<String> houses = new ArrayList<>();

    public Building (){}

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    public String getMarkerId() {
        return markerId;
    }

    public String getLatlng() {
        return latlng;
    }

    public String getType() {
        return type;
    }

    public String getLocation() {
        return location;
    }

    public String getPic() { return pic; }

    public String getAddress() {
        return address;
    }

    public List<String> getHouses() {
        return houses;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public void setMarkerId(String markerId) {
        this.markerId = markerId;
    }

    public void setLatlng(String latlng) {
        this.latlng = latlng;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setHouses(List<String> houses) {
        this.houses = houses;
    }

    public void addHouse(String house) {
        this.houses.add(house);
    }

    public void emptyBuilding () {

        houses.clear();

    }

}
