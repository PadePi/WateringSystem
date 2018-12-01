package com.example.peti.wateringsystem;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashSet;
import java.util.Set;

public class SettingsFragment extends PreferenceFragment {

    public static final String BASIC_BEHAVIOUR="basic_behaviour";
    public static final String MINIMAL_MOISTURE="minimal_moisture";
    public static final String SCHEDULED_DAYS="scheduled_days";
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    private RequestQueue mRequestQueue;
    private StringRequest stringRequest;
    private String baseUrl="http://192.168.0.35/";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        final SharedPreferences sharedPref=this.getPreferenceManager().getSharedPreferences();
        mRequestQueue= Volley.newRequestQueue(getContext());

        disabeOptions(sharedPref);


        preferenceChangeListener=new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if(key.equals(BASIC_BEHAVIOUR)) {
                    disabeOptions(sharedPref);
                    if (sharedPref.getString(BASIC_BEHAVIOUR, "").equals("Minimal water moisture")) {
                        stringRequest = new StringRequest(Request.Method.GET, baseUrl + "minimalWater", new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        });
                        mRequestQueue.add(stringRequest);
                    }else
                    {
                        stringRequest = new StringRequest(Request.Method.GET, baseUrl + "scheduledDays", new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        });
                        mRequestQueue.add(stringRequest);
                    }
                }
                if(key.equals(MINIMAL_MOISTURE)) {
                    if (sharedPref.getString(MINIMAL_MOISTURE, "").equals("Moisture under 40%")) {
                        stringRequest = new StringRequest(Request.Method.GET, baseUrl + "under40", new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        });

                        mRequestQueue.add(stringRequest);
                    }
                    if (sharedPref.getString(MINIMAL_MOISTURE, "").equals("Moisture under 50%")) {
                        stringRequest = new StringRequest(Request.Method.GET, baseUrl + "under50", new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        });

                        mRequestQueue.add(stringRequest);
                    }
                    if (sharedPref.getString(MINIMAL_MOISTURE, "").equals("Moisture under 60%")) {
                        stringRequest = new StringRequest(Request.Method.GET, baseUrl + "under60", new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        });

                        mRequestQueue.add(stringRequest);
                    }
                }
                if(key.equals(SCHEDULED_DAYS))
                {
                    Set<String> days = sharedPref.getStringSet(SCHEDULED_DAYS, new HashSet<String>());
                    String days_joined = String.join("", days);
                    if(days_joined==""){
                        days_joined="noDaySelected";
                    }
                    stringRequest = new StringRequest(Request.Method.GET, baseUrl + days_joined, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    });

                    mRequestQueue.add(stringRequest);

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
