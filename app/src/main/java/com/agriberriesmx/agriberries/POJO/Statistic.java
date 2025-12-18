package com.agriberriesmx.agriberries.POJO;

import java.util.Date;
import java.util.List;

public class Statistic {
    private String id;
    private String state;
    private String crop;
    private String management;
    private String soil;
    private List<String> plagues;
    private Date date;

    public Statistic() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCrop() {
        return crop;
    }

    public void setCrop(String crop) {
        this.crop = crop;
    }

    public String getManagement() {
        return management;
    }

    public void setManagement(String management) {
        this.management = management;
    }

    public String getSoil() {
        return soil;
    }

    public void setSoil(String soil) {
        this.soil = soil;
    }

    public List<String> getPlagues() {
        return plagues;
    }

    public void setPlagues(List<String> plagues) {
        this.plagues = plagues;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

}
