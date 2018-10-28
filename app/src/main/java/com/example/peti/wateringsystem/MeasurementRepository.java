package com.example.peti.wateringsystem;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

public class MeasurementRepository {

    private MeasurementDAO mMeasurementDao;
    private LiveData<List<Measurement>> mAllMeasurements;

    MeasurementRepository(Application application) {
        MeasurementRoomDatabase db = MeasurementRoomDatabase.getDatabase(application);
        mMeasurementDao = db.measurementDao();
        mAllMeasurements = mMeasurementDao.getAllMeasurement();
    }

    LiveData<List<Measurement>> getmAllMeasurements() {
        return mAllMeasurements;
    }


    public void insert (Measurement measurement) {
        new insertAsyncTask(mMeasurementDao).execute(measurement);
    }

    public void deleteAll(){
        new deleteAsyncTask(mMeasurementDao).execute();
    }



    private static class insertAsyncTask extends AsyncTask<Measurement, Void, Void> {

        private MeasurementDAO mAsyncTaskDao;

        insertAsyncTask(MeasurementDAO dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Measurement... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class deleteAsyncTask extends AsyncTask<Measurement, Void, Void> {

        private MeasurementDAO mAsyncTaskDao;

        deleteAsyncTask(MeasurementDAO dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Measurement... params) {
            mAsyncTaskDao.deleteAll();
            return null;
        }
    }

}
