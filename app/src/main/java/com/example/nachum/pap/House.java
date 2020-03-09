package com.example.nachum.pap;

import java.util.ArrayList;
import java.util.List;

public class House {

    private String name;
    private String address;
    private String location;
    private String description;
    private String status;
    private String activists;
    private Double lat;
    private Double lng;
    private String pic;
    private String markerId;
    private String latlng;
    private String latestReport;
    private String type;
    private String apartment;
    private List<String> report = new ArrayList<>();

    public House (){}

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getActivists() {return activists; }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    public String getPic() {
        return pic;
    }

    public String getMarkerId() { return  markerId; }

    public String getLatlng() {
        return latlng;
    }

    public String getLatestReport() { return latestReport; }

    public String getType() { return type; }

    public String getApartment() {
        return apartment;
    }

    public List<String> getReports() {
        return report;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setDescription(String desciption) {
        this.description = desciption;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setActivists (String activists) { this.activists = activists; }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public void setMarkerId(String markerId) {
        this.markerId = markerId;
    }

    public void setLatlng(String latlng) {
        this.latlng = latlng;
    }

    public void setLatestReport(String latestReport) { this.latestReport = latestReport; }

    public void setType(String type) { this.type = type; }

    public void setApartment(String apartment) {
        this.apartment = apartment;
    }

    public void addReport(String report){
        this.report.add(report);
    }

    public void setReport(List<String> report){
        this.report = report;
    }

    //todo fix
//    public void removeReport(int num){
//        this.report[num-1] = null;
//    }

    public void emptyHouse () {
        this.name = "";
        this.address = "";
        this.location = "";
        this.description = "";
        this.status = "";
        this.activists = "";
        this.lat = 0.0;
        this.lng = 0.0;
        this.pic = "";
        this.markerId = "";
        this.latestReport = "";
        this.type = "";
        this.report = new ArrayList<>();
    }
}
