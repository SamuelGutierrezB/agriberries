package com.agriberriesmx.agriberries.POJO;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.List;

public class Plague implements Parcelable {
    private String id;
    private String name;
    private List<String> crops;
    private Date deleted;

    public Plague() {
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

    public List<String> getCrops() {
        return crops;
    }

    public void setCrops(List<String> crops) {
        this.crops = crops;
    }

    public Date getDeleted() {
        return deleted;
    }

    public void setDeleted(Date deleted) {
        this.deleted = deleted;
    }

    protected Plague(Parcel in) {
        id = in.readString();
        name = in.readString();
        crops = in.createStringArrayList();
        long deletedTime = in.readLong();
        deleted = (deletedTime != -1) ? new Date(deletedTime) : null;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeStringList(crops);
        dest.writeLong(deleted != null ? deleted.getTime() : -1);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Plague> CREATOR = new Creator<Plague>() {
        @Override
        public Plague createFromParcel(Parcel in) {
            return new Plague(in);
        }

        @Override
        public Plague[] newArray(int size) {
            return new Plague[size];
        }
    };

}
