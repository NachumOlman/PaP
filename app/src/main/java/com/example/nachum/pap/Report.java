package com.example.nachum.pap;

import java.util.Date;

public class Report {
    private String activists;
    private String report;
    private String date;

    public Report (){}

    public Report (String activists, String report, String date){
        this.activists = activists;
        this.report = report;
        this.date = date;
    }

    public String getActivists() {
        return activists;
    }

    public String getReport() {
        return report;
    }

    public String getDate() {
        return date;
    }

    public void setActivists(String activists) {
        this.activists = activists;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
