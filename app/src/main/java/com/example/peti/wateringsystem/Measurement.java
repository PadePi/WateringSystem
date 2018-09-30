package com.example.peti.wateringsystem;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Date;

@Entity(tableName = "measurements")
public class Measurement {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name = "date")
    private Date date;

    @NonNull
    @ColumnInfo(name = "water_percentage")
    private int waterPercentage;

    public Measurement(@NonNull int waterPercentage) {

        this.date=new Date();
        this.waterPercentage = waterPercentage;

    }

    public int getId() {
        return id;
    }

    @NonNull
    public Date getDate() {
        return date;
    }

    @NonNull
    public int getWaterPercentage() {
        return waterPercentage;
    }

    public void setDate(@NonNull Date date) {
        this.date = date;
    }

    public void setWaterPercentage(@NonNull int waterPercentage) {
        this.waterPercentage = waterPercentage;
    }
}
