package com.example.peti.wateringsystem;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface MeasurementDAO {

    @Insert
    void insert(Measurement measurement);

    @Query("DELETE FROM measurements")
    void deleteAll();

    @Query("SELECT * from measurements ORDER BY date DESC")
    List<Measurement> getAllMeasurement();

}
