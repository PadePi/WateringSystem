package com.example.peti.wateringsystem;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

public class SettingsFragment extends PreferenceFragment {

    public static final String BASIC_BEHAVIOUR="basic_behaviour";
    public static final String MINIMAL_MOISTURE="minimal_moisture";
    public static final String SCHEDULED_DAYS="scheduled_days";
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        final SharedPreferences sharedPref=this.getPreferenceManager().getSharedPreferences();

        disabeOptions(sharedPref);


        preferenceChangeListener=new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if(key.equals(BASIC_BEHAVIOUR))
                {
                    disabeOptions(sharedPref);
                }
            }
        };
    }

    private void disabeOptions(SharedPreferences sharedPref) {
        if(sharedPref.getString(BASIC_BEHAVIOUR,"").equals("Minimal water moisture"))
        {
            findPreference(MINIMAL_MOISTURE).setEnabled(true);
            findPreference(SCHEDULED_DAYS).setEnabled(false);
        }
        else if(sharedPref.getString(BASIC_BEHAVIOUR,"").equals("Scheduled days"))
        {
            findPreference(SCHEDULED_DAYS).setEnabled(true);
            findPreference(MINIMAL_MOISTURE).setEnabled(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }
}
