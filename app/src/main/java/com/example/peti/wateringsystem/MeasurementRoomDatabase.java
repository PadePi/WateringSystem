package com.example.peti.wateringsystem;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

@Database(entities = {Measurement.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class MeasurementRoomDatabase extends RoomDatabase {

    public abstract MeasurementDAO measurementDao();

    private static volatile MeasurementRoomDatabase INSTANCE;

    private static RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback(){

                @Override
                public void onOpen (@NonNull SupportSQLiteDatabase db){
                    super.onOpen(db);
                }
            };

    static MeasurementRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (MeasurementRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            MeasurementRoomDatabase.class, "word_database")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
