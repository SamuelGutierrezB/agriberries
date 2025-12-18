package com.agriberriesmx.agriberries.POJO;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.List;

public class Client implements Parcelable {
    private String id;
    private String name;
    private String manager;
    private String business;
    private String phone;
    private String email;
    private int frequency;
    private List<String> consultants;
    private Date registration;
    private boolean blocked;
    private boolean prospect;

    public Client() {
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

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public String getBusiness() {
        return business;
    }

    public void setBusiness(String business) {
        this.business = business;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public List<String> getConsultants() {
        return consultants;
    }

    public void setConsultants(List<String> consultants) {
        this.consultants = consultants;
    }

    public Date getRegistration() {
        return registration;
    }

    public void setRegistration(Date registration) {
        this.registration = registration;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public boolean isProspect() {
        return prospect;
    }

    public void setProspect(boolean prospect) {
        this.prospect = prospect;
    }

    protected Client(Parcel in) {
        id = in.readString();
        name = in.readString();
        manager = in.readString();
        business = in.readString();
        phone = in.readString();
        email = in.readString();
        frequency = in.readInt();
        consultants = in.createStringArrayList();
        long registrationTime = in.readLong();
        registration = (registrationTime != -1) ? new Date(registrationTime) : null;
        blocked = in.readByte() != 0;
        prospect = in.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(manager);
        dest.writeString(business);
        dest.writeString(phone);
        dest.writeString(email);
        dest.writeInt(frequency);
        dest.writeStringList(consultants);
        dest.writeLong(registration != null ? registration.getTime() : -1);
        dest.writeByte((byte) (blocked ? 1 : 0));
        dest.writeByte((byte) (prospect ? 1 : 0));
    }

    public static final Creator<Client> CREATOR = new Creator<Client>() {
        @Override
        public Client createFromParcel(Parcel in) {
            return new Client(in);
        }

        @Override
        public Client[] newArray(int size) {
            return new Client[size];
        }
    };

}
