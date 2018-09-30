package com.example.peti.wateringsystem;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

@Database(entities = {Measurement.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class MeasurementRoomDatabase extends RoomDatabase {

    public abstract MeasurementDAO measurementDao();

    private static volatile MeasurementRoomDatabase INSTANCE;

    static MeasurementRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (MeasurementRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            MeasurementRoomDatabase.class, "word_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
