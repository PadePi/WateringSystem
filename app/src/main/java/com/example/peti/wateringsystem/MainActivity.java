package com.example.peti.wateringsystem;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashSet;
import java.util.Set;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG=MainActivity.class.getName();
    private RequestQueue mRequestQueue;
    private StringRequest stringRequest;
    private String baseUrl="http://192.168.0.35/";
    private String startPumpUrl ="startPump";
    private String stopPumpUrl ="stopPump";
    private String getSoilMoistureUrl ="measureMoisture";
    private String getWaterLevelUrl ="waterLevel";
    private final int criticalWaterLevel = 20;

    private TextView curentMoistureText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        if(!isNetworkAvailable(this)) {
            Toast.makeText(this,"No Internet connection",Toast.LENGTH_LONG).show();
            finish();
        }
        mRequestQueue= Volley.newRequestQueue(this);

        curentMoistureText=(TextView) findViewById(R.id.textView);
        initializeSoilMoisture();
        syncSettings();

        getWaterLevel();

        final Button button = findViewById(R.id.button);


        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendWateringRequestIfEnoughWater();
            }
        });

    }

    private void sendWateringRequestIfEnoughWater()
    {
        stringRequest=new StringRequest(Request.Method.GET, baseUrl + getWaterLevelUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                response=response.replaceAll("(\\r|\\n)", "");
                int actualResponse= Integer.parseInt(response);
                if(actualResponse>criticalWaterLevel)
                {
                    showLowWaterAlert();
                }else
                {
                    sendWateringRequest();
                    new stopWateringTask().execute();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Context context = getApplicationContext();
                CharSequence text = "Arduino offline, check connection!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        });

        mRequestQueue.add(stringRequest);
    }

    private void sendWateringRequest() {
        stringRequest=new StringRequest(Request.Method.POST, baseUrl + startPumpUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Context context = getApplicationContext();
                CharSequence text = "Watering started";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Context context = getApplicationContext();
                CharSequence text = "Arduino offline, check connection!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        });

        mRequestQueue.add(stringRequest);
    }

    private class stopWateringTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... param) {

            try {
                sleep(10000);
                stringRequest = new StringRequest(Request.Method.POST, baseUrl + stopPumpUrl, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Context context = getApplicationContext();
                        CharSequence text = "Watering stopped";
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Context context = getApplicationContext();
                        CharSequence text = "Arduino offline, check connection!";
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }
                });
                mRequestQueue.add(stringRequest);
                return null;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        }
    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_stat) {
            openStatActivity();

        } else if (id == R.id.nav_settings) {
            openSettingsActivity();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void openStatActivity(){
        Intent intent = new Intent(this,StatActivity.class);
        startActivity(intent);
    }

    public void openSettingsActivity(){
        Intent intent = new Intent(this,SettingsActivity.class);
        startActivity(intent);
    }

    private void initializeSoilMoisture() {
        stringRequest=new StringRequest(Request.Method.GET, baseUrl + getSoilMoistureUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                response=response.replaceAll("(\\r|\\n)", "");
                int actualResponse= Integer.parseInt(response);
                int convertedValue=calculatePercentage(actualResponse);
                curentMoistureText.setText(curentMoistureText.getText() + " " + String.valueOf(convertedValue) + "%");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Context context = getApplicationContext();
                CharSequence text = "Arduino offline, check connection!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        });

        mRequestQueue.add(stringRequest);
    }

    private void syncSettings() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        final String BASIC_BEHAVIOUR="basic_behaviour";
        final String MINIMAL_MOISTURE="minimal_moisture";
        final String SCHEDULED_DAYS="scheduled_days";
        if (sharedPref.getString(BASIC_BEHAVIOUR, "").equals("Minimal water moisture")) {
            stringRequest = new StringRequest(Request.Method.POST, baseUrl + "minimalWater", new Response.Listener<String>() {
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
            stringRequest = new StringRequest(Request.Method.POST, baseUrl + "scheduledDays", new Response.Listener<String>() {
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

        if (sharedPref.getString(MINIMAL_MOISTURE, "").equals("Moisture under 40%")) {
            stringRequest = new StringRequest(Request.Method.POST, baseUrl + "under40", new Response.Listener<String>() {
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
            stringRequest = new StringRequest(Request.Method.POST, baseUrl + "under50", new Response.Listener<String>() {
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
            stringRequest = new StringRequest(Request.Method.POST, baseUrl + "under60", new Response.Listener<String>() {
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

        Set<String> days = sharedPref.getStringSet(SCHEDULED_DAYS, new HashSet<String>());
        String days_joined = String.join("", days);
        if(days_joined==""){
            days_joined="noDaySelected";
        }
        stringRequest = new StringRequest(Request.Method.POST, baseUrl + days_joined, new Response.Listener<String>() {
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

    private int calculatePercentage(int analogInput)
    {
        return (int)((((double)analogInput/4095)*100)-100)*-1;
    }

    private void getWaterLevel()
    {
        stringRequest=new StringRequest(Request.Method.GET, baseUrl + getWaterLevelUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                response=response.replaceAll("(\\r|\\n)", "");
                int actualResponse= Integer.parseInt(response);
                if(actualResponse>criticalWaterLevel)
                {
                    showLowWaterAlert();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Context context = getApplicationContext();
                CharSequence text = "Arduino offline, check connection!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        });

        mRequestQueue.add(stringRequest);
    }

    public void showLowWaterAlert()
    {
        AlertDialog alertDialog=new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("Water level is low, please refill the tank");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(conMan.getActiveNetworkInfo() != null && conMan.getActiveNetworkInfo().isConnected())
            return true;
        else
            return false;
    }

}
