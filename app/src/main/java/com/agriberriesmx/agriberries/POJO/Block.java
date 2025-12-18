package com.agriberriesmx.agriberries.POJO;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Block implements Parcelable {
    private String id;
    private String name;
    private String type;
    private double furrowDistance;
    private double plantDistance;
    private int row;
    private int col;
    private Date plantationDate;
    private Date harvestDate;

    public Block() {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getFurrowDistance() {
        return furrowDistance;
    }

    public void setFurrowDistance(double furrowDistance) {
        this.furrowDistance = furrowDistance;
    }

    public double getPlantDistance() {
        return plantDistance;
    }

    public void setPlantDistance(double plantDistance) {
        this.plantDistance = plantDistance;
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

    public Date getPlantationDate() {
        return plantationDate;
    }

    public void setPlantationDate(Date plantationDate) {
        this.plantationDate = plantationDate;
    }

    public Date getHarvestDate() {
        return harvestDate;
    }

    public void setHarvestDate(Date harvestDate) {
        this.harvestDate = harvestDate;
    }

    protected Block(Parcel in) {
        id = in.readString();
        name = in.readString();
        type = in.readString();
        furrowDistance = in.readDouble();
        plantDistance = in.readDouble();
        row = in.readInt();
        col = in.readInt();
        long plantationTime = in.readLong();
        plantationDate = (plantationTime != -1) ? new Date(plantationTime) : null;
        long harvestTime = in.readLong();
        harvestDate = (harvestTime != -1) ? new Date(harvestTime) : null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(type);
        dest.writeDouble(furrowDistance);
        dest.writeDouble(plantDistance);
        dest.writeInt(row);
        dest.writeInt(col);
        dest.writeLong(plantationDate != null ? plantationDate.getTime() : -1);
        dest.writeLong(harvestDate != null ? harvestDate.getTime() : -1);
    }

    public static final Creator<Block> CREATOR = new Creator<Block>() {
        @Override
        public Block createFromParcel(Parcel in) {
            return new Block(in);
        }

        @Override
        public Block[] newArray(int size) {
            return new Block[size];
        }
    };

}
