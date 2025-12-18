package com.agriberriesmx.agriberries.POJO;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Notification implements Parcelable {
    private String id;
    private String title;
    private String text;
    private String link;
    private List<String> consultantsSeen;
    private Date begin;
    private Date end;

    public Notification() {
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public List<String> getConsultantsSeen() {
        return consultantsSeen;
    }

    public void setConsultantsSeen(List<String> consultantsSeen) {
        this.consultantsSeen = consultantsSeen;
    }

    public Date getBegin() {
        return begin;
    }

    public void setBegin(Date begin) {
        this.begin = begin;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    protected Notification(Parcel in) {
        id = in.readString();
        title = in.readString();
        text = in.readString();
        link = in.readString();
        consultantsSeen = new ArrayList<>();
        in.readStringList(consultantsSeen);
        long tmpBegin = in.readLong();
        begin = tmpBegin != -1 ? new Date(tmpBegin) : null;
        long tmpEnd = in.readLong();
        end = tmpEnd != -1 ? new Date(tmpEnd) : null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(text);
        dest.writeString(link);
        dest.writeStringList(consultantsSeen);
        dest.writeLong(begin != null ? begin.getTime() : -1);
        dest.writeLong(end != null ? end.getTime() : -1);
    }

    public static final Creator<Notification> CREATOR = new Creator<Notification>() {
        @Override
        public Notification createFromParcel(Parcel in) {
            return new Notification(in);
        }

        @Override
        public Notification[] newArray(int size) {
            return new Notification[size];
        }
    };

}
