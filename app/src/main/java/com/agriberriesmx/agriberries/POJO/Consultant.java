package com.agriberriesmx.agriberries.POJO;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.agriberriesmx.agriberries.R;

import java.util.Date;

public class Consultant implements Parcelable {
    private String id;
    private String username;
    private int category;
    private String name;
    private String phone;
    private boolean blocked;
    private Date birth;
    private Date deleted;

    public Consultant() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getCategory() {
        return category;
    }

    public String getTextCategory(Context context) {
        // Get string array from resources
        String[] categories = context.getResources().getStringArray(R.array.categories);

        return categories[category];
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public Date getBirth() {
        return birth;
    }

    public void setBirth(Date birth) {
        this.birth = birth;
    }

    public Date getDeleted() {
        return deleted;
    }

    public void setDeleted(Date deleted) {
        this.deleted = deleted;
    }

    protected Consultant(Parcel in) {
        id = in.readString();
        username = in.readString();
        category = in.readInt();
        name = in.readString();
        phone = in.readString();
        blocked = in.readByte() != 0;
        long birthTime = in.readLong();
        birth = (birthTime != -1) ? new Date(birthTime) : null;
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
        dest.writeString(username);
        dest.writeInt(category);
        dest.writeString(name);
        dest.writeString(phone);
        dest.writeByte((byte) (blocked ? 1 : 0));
        dest.writeLong(birth != null ? birth.getTime() : -1);
        dest.writeLong(deleted != null ? deleted.getTime() : -1);
    }

    public static final Creator<Consultant> CREATOR = new Creator<Consultant>() {
        @Override
        public Consultant createFromParcel(Parcel in) {
            return new Consultant(in);
        }

        @Override
        public Consultant[] newArray(int size) {
            return new Consultant[size];
        }
    };

}
