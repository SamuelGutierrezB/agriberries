package com.agriberriesmx.agriberries.POJO;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Unit implements Parcelable {
    private String id;
    private String name;
    private String state;
    private String location;
    private double hectares;
    private String crop;
    private String soil;
    private String management;
    private String modality;
    private double altitude;
    private double latitude;
    private double longitude;
    private Date deleted;

    public Unit() {
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getHectares() {
        return hectares;
    }

    public void setHectares(double hectares) {
        this.hectares = hectares;
    }

    public String getCrop() {
        return crop;
    }

    public void setCrop(String crop) {
        this.crop = crop;
    }

    public String getSoil() {
        return soil;
    }

    public void setSoil(String soil) {
        this.soil = soil;
    }

    public String getManagement() {
        return management;
    }

    public void setManagement(String management) {
        this.management = management;
    }

    public String getModality() {
        return modality;
    }

    public void setModality(String modality) {
        this.modality = modality;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
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

    public Date getDeleted() {
        return deleted;
    }

    public void setDeleted(Date deleted) {
        this.deleted = deleted;
    }

    protected Unit(Parcel in) {
        id = in.readString();
        name = in.readString();
        state = in.readString();
        location = in.readString();
        hectares = in.readDouble();
        crop = in.readString();
        soil = in.readString();
        management = in.readString();
        altitude = in.readDouble();
        latitude = in.readDouble();
        longitude = in.readDouble();
        modality = in.readString();
        long tmpDeleted = in.readLong();
        deleted = tmpDeleted != -1 ? new Date(tmpDeleted) : null;
    }

    public static final Creator<Unit> CREATOR = new Creator<Unit>() {
        @Override
        public Unit createFromParcel(Parcel in) {
            return new Unit(in);
        }

        @Override
        public Unit[] newArray(int size) {
            return new Unit[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(state);
        dest.writeString(location);
        dest.writeDouble(hectares);
        dest.writeString(crop);
        dest.writeString(soil);
        dest.writeString(management);
        dest.writeDouble(altitude);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(modality);
        dest.writeLong(deleted != null ? deleted.getTime() : -1);
    }

}
