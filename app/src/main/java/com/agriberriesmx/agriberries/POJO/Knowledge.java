package com.agriberriesmx.agriberries.POJO;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.List;

public class Knowledge implements Parcelable {
    private String id;
    private String title;
    private String link;
    private List<String> crops;
    private Date deleted;

    public Knowledge() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
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

    protected Knowledge(Parcel in) {
        id = in.readString();
        title = in.readString();
        link = in.readString();
        crops = in.createStringArrayList();
        long deletedTime = in.readLong();
        deleted = (deletedTime != -1) ? new Date(deletedTime) : null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(link);
        dest.writeStringList(crops);
        dest.writeLong(deleted != null ? deleted.getTime() : -1);
    }

    public static final Creator<Knowledge> CREATOR = new Creator<Knowledge>() {
        @Override
        public Knowledge createFromParcel(Parcel in) {
            return new Knowledge(in);
        }

        @Override
        public Knowledge[] newArray(int size) {
            return new Knowledge[size];
        }
    };

}
