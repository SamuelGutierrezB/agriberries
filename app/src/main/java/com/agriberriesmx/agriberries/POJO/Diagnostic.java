package com.agriberriesmx.agriberries.POJO;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Diagnostic implements Parcelable {
    private String id;
    private String unit;
    private String consultant;
    private String unitName;
    private String observations;
    private String tendency;
    private String recommendation;
    private int development;
    private int sanity;
    private int management;
    private int executionApplications;
    private int executionFertilizers;
    private Date creation;
    private boolean finished;

    public Diagnostic() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getConsultant() {
        return consultant;
    }

    public void setConsultant(String consultant) {
        this.consultant = consultant;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public String getTendency() {
        return tendency;
    }

    public void setTendency(String tendency) {
        this.tendency = tendency;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public int getDevelopment() {
        return development;
    }

    public void setDevelopment(int development) {
        this.development = development;
    }

    public int getSanity() {
        return sanity;
    }

    public void setSanity(int sanity) {
        this.sanity = sanity;
    }

    public int getManagement() {
        return management;
    }

    public void setManagement(int management) {
        this.management = management;
    }

    public int getExecutionApplications() {
        return executionApplications;
    }

    public void setExecutionApplications(int executionApplications) {
        this.executionApplications = executionApplications;
    }

    public int getExecutionFertilizers() {
        return executionFertilizers;
    }

    public void setExecutionFertilizers(int executionFertilizers) {
        this.executionFertilizers = executionFertilizers;
    }

    public Date getCreation() {
        return creation;
    }

    public void setCreation(Date creation) {
        this.creation = creation;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    protected Diagnostic(Parcel in) {
        id = in.readString();
        unit = in.readString();
        consultant = in.readString();
        unitName = in.readString();
        observations = in.readString();
        tendency = in.readString();
        recommendation = in.readString();
        development = in.readInt();
        sanity = in.readInt();
        management = in.readInt();
        executionApplications = in.readInt();
        executionFertilizers = in.readInt();
        long tmpCreation = in.readLong();
        creation = tmpCreation != -1 ? new Date(tmpCreation) : null;
        finished = in.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(unit);
        dest.writeString(consultant);
        dest.writeString(unitName);
        dest.writeString(observations);
        dest.writeString(tendency);
        dest.writeString(recommendation);
        dest.writeInt(development);
        dest.writeInt(sanity);
        dest.writeInt(management);
        dest.writeInt(executionApplications);
        dest.writeInt(executionFertilizers);
        dest.writeLong(creation != null ? creation.getTime() : -1);
        dest.writeByte((byte) (finished ? 1 : 0));
    }

    public static final Creator<Diagnostic> CREATOR = new Creator<Diagnostic>() {
        @Override
        public Diagnostic createFromParcel(Parcel in) {
            return new Diagnostic(in);
        }

        @Override
        public Diagnostic[] newArray(int size) {
            return new Diagnostic[size];
        }
    };

}
