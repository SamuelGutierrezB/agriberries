package com.agriberriesmx.agriberries.POJO;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.List;

public class Treatment implements Parcelable {
    private String id;
    private String name;
    private String group;
    private String ingredient;
    private int harvest;
    private int maxApplications;
    private String unit;
    private String unitPresentation;
    private double quantityPresentation;
    private double minAmount;
    private double maxAmount;
    private double price;
    private List<String> plagues;
    private Date deleted;

    public Treatment() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getIngredient() {
        return ingredient;
    }

    public void setIngredient(String ingredient) {
        this.ingredient = ingredient;
    }

    public int getHarvest() {
        return harvest;
    }

    public void setHarvest(int harvest) {
        this.harvest = harvest;
    }

    public int getMaxApplications() {
        return maxApplications;
    }

    public void setMaxApplications(int maxApplications) {
        this.maxApplications = maxApplications;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getUnitPresentation() {
        return unitPresentation;
    }

    public void setUnitPresentation(String unitPresentation) {
        this.unitPresentation = unitPresentation;
    }

    public double getQuantityPresentation() {
        return quantityPresentation;
    }

    public void setQuantityPresentation(double quantityPresentation) {
        this.quantityPresentation = quantityPresentation;
    }

    public double getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(double minAmount) {
        this.minAmount = minAmount;
    }

    public double getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(double maxAmount) {
        this.maxAmount = maxAmount;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public List<String> getPlagues() {
        return plagues;
    }

    public void setPlagues(List<String> plagues) {
        this.plagues = plagues;
    }

    public Date getDeleted() {
        return deleted;
    }

    public void setDeleted(Date deleted) {
        this.deleted = deleted;
    }

    protected Treatment(Parcel in) {
        id = in.readString();
        name = in.readString();
        group = in.readString();
        ingredient = in.readString();
        harvest = in.readInt();
        maxApplications = in.readInt();
        unit = in.readString();
        unitPresentation = in.readString();
        quantityPresentation = in.readDouble();
        minAmount = in.readDouble();
        maxAmount = in.readDouble();
        price = in.readDouble();
        plagues = in.createStringArrayList();
        long tmpDeleted = in.readLong();
        deleted = tmpDeleted != -1 ? new Date(tmpDeleted) : null;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(group);
        dest.writeString(ingredient);
        dest.writeInt(harvest);
        dest.writeInt(maxApplications);
        dest.writeString(unit);
        dest.writeString(unitPresentation);
        dest.writeDouble(quantityPresentation);
        dest.writeDouble(minAmount);
        dest.writeDouble(maxAmount);
        dest.writeDouble(price);
        dest.writeStringList(plagues);
        dest.writeLong(deleted != null ? deleted.getTime() : -1);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Treatment> CREATOR = new Creator<Treatment>() {
        @Override
        public Treatment createFromParcel(Parcel in) {
            return new Treatment(in);
        }

        @Override
        public Treatment[] newArray(int size) {
            return new Treatment[size];
        }
    };

}
