package com.example.peti.wateringsystem;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

public class MeasurementViewModel extends AndroidViewModel {

    private MeasurementRepository mRepository;
    private LiveData<List<Measurement>> mAllMeasurements;

    public MeasurementViewModel (Application application) {
        super(application);
        mRepository = new MeasurementRepository(application);
        mAllMeasurements = mRepository.getmAllMeasurements();
    }

    LiveData<List<Measurement>> getAllMeasurements() { return mAllMeasurements; }

    public void insert(Measurement measurement) { mRepository.insert(measurement); }
    public void deleteAll(){mRepository.deleteAll();}

}
