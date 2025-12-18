package com.agriberriesmx.agriberries.POJO;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Item implements Parcelable {
    private String id;
    private String phenology;
    private String fruitSize;
    private String note;
    private List<String> plagues;
    private List<String> deficiencies;
    private List<String> contingencies;
    private List<String> activities;
    private List<String> tasks;
    private double height;
    private double longitude;
    private double weeklyGrowing;
    private double gramPerFruit;
    private int fruitPerPlantMeter;
    private int shrinkagePercentage;
    private int row;
    private int col;

    public Item() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhenology() {
        return phenology;
    }

    public void setPhenology(String phenology) {
        this.phenology = phenology;
    }

    public String getFruitSize() {
        return fruitSize;
    }

    public void setFruitSize(String fruitSize) {
        this.fruitSize = fruitSize;
    }

    public String getNote() {
        return note != null ? note : "";
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<String> getPlagues() {
        return plagues;
    }

    public void setPlagues(List<String> plagues) {
        this.plagues = plagues;
    }

    public List<String> getDeficiencies() {
        return deficiencies;
    }

    public void setDeficiencies(List<String> deficiencies) {
        this.deficiencies = deficiencies;
    }

    public List<String> getContingencies() {
        return contingencies;
    }

    public void setContingencies(List<String> contingencies) {
        this.contingencies = contingencies;
    }

    public List<String> getActivities() {
        return activities;
    }

    public void setActivities(List<String> activities) {
        this.activities = activities;
    }

    public List<String> getTasks() {
        return tasks;
    }

    public void setTasks(List<String> tasks) {
        this.tasks = tasks;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getWeeklyGrowing() {
        return weeklyGrowing;
    }

    public void setWeeklyGrowing(double weeklyGrowing) {
        this.weeklyGrowing = weeklyGrowing;
    }

    public double getGramPerFruit() {
        return gramPerFruit;
    }

    public void setGramPerFruit(double gramPerFruit) {
        this.gramPerFruit = gramPerFruit;
    }

    public int getFruitPerPlantMeter() {
        return fruitPerPlantMeter;
    }

    public void setFruitPerPlantMeter(int fruitPerPlantMeter) {
        this.fruitPerPlantMeter = fruitPerPlantMeter;
    }

    public int getShrinkagePercentage() {
        return shrinkagePercentage;
    }

    public void setShrinkagePercentage(int shrinkagePercentage) {
        this.shrinkagePercentage = shrinkagePercentage;
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

    protected Item(Parcel in) {
        id = in.readString();
        phenology = in.readString();
        fruitSize = in.readString();
        note = in.readString();
        plagues = new ArrayList<>();
        in.readList(plagues, String.class.getClassLoader());
        deficiencies = new ArrayList<>();
        in.readList(deficiencies, String.class.getClassLoader());
        contingencies = new ArrayList<>();
        in.readList(contingencies, String.class.getClassLoader());
        activities = new ArrayList<>();
        in.readList(activities, String.class.getClassLoader());
        tasks = new ArrayList<>();
        in.readList(tasks, String.class.getClassLoader());
        height = in.readDouble();
        longitude = in.readDouble();
        weeklyGrowing = in.readDouble();
        gramPerFruit = in.readDouble();
        fruitPerPlantMeter = in.readInt();
        shrinkagePercentage = in.readInt();
        row = in.readInt();
        col = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(phenology);
        dest.writeString(fruitSize);
        dest.writeString(note);
        dest.writeList(plagues);
        dest.writeList(deficiencies);
        dest.writeList(contingencies);
        dest.writeList(activities);
        dest.writeList(tasks);
        dest.writeDouble(height);
        dest.writeDouble(longitude);
        dest.writeDouble(weeklyGrowing);
        dest.writeDouble(gramPerFruit);
        dest.writeInt(fruitPerPlantMeter);
        dest.writeInt(shrinkagePercentage);
        dest.writeInt(row);
        dest.writeInt(col);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

}
