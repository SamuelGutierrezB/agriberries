package com.agriberriesmx.agriberries.POJO;

import java.util.ArrayList;
import java.util.List;

public class Nutrient {
    private String id;
    private List<Integer> nitrate;
    private List<Integer> calcium;
    private List<Integer> sodium;
    private List<Integer> potassium;
    private List<Double> ph;
    private List<Double> conductivity;
    private int row;
    private int col;

    public Nutrient() {
        // Create lists
        nitrate = new ArrayList<>();
        calcium = new ArrayList<>();
        sodium = new ArrayList<>();
        potassium = new ArrayList<>();
        ph = new ArrayList<>();
        conductivity = new ArrayList<>();

        // Initialize values
        for (int counter = 0; counter < 4; counter++) {
            nitrate.add(0);
            calcium.add(0);
            sodium.add(0);
            potassium.add(0);
            ph.add(0.0);
            conductivity.add(0.0);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Integer> getNitrate() {
        return nitrate;
    }

    public void setNitrate(List<Integer> nitrate) {
        this.nitrate = nitrate;
    }

    public List<Integer> getCalcium() {
        return calcium;
    }

    public void setCalcium(List<Integer> calcium) {
        this.calcium = calcium;
    }

    public List<Integer> getSodium() {
        return sodium;
    }

    public void setSodium(List<Integer> sodium) {
        this.sodium = sodium;
    }

    public List<Integer> getPotassium() {
        return potassium;
    }

    public void setPotassium(List<Integer> potassium) {
        this.potassium = potassium;
    }

    public List<Double> getPh() {
        return ph;
    }

    public void setPh(List<Double> ph) {
        this.ph = ph;
    }

    public List<Double> getConductivity() {
        return conductivity;
    }

    public void setConductivity(List<Double> conductivity) {
        this.conductivity = conductivity;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

}
