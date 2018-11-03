package com.example.peti.wateringsystem;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

public class StatActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private MeasurementViewModel mMeasurementViewModel;
    private static final String TAG=StatActivity.class.getName();
    private RequestQueue mRequestQueue;
    private StringRequest stringRequest;
    private String url="http://192.168.0.35/H";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stat);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        final MeasurementListAdapter adapter = new MeasurementListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mMeasurementViewModel = ViewModelProviders.of(this).get(MeasurementViewModel.class);

        mMeasurementViewModel.getAllMeasurements().observe(this, new Observer<List<Measurement>>() {
            @Override
            public void onChanged(@Nullable final List<Measurement> measurements) {
                // Update the cached copy of the words in the adapter.
                adapter.setMeasurements(measurements);
            }
        });



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMeasuringRequest();
            }
        });

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.import_sample_data) {
            mMeasurementViewModel.deleteAll();
            List<Measurement> measurements=new ArrayList<>();
            Calendar calendar= Calendar.getInstance();
            Random random=new Random();
            for(int i=0;i<10;i++)
            {
                Measurement tmp=new Measurement(random.nextInt(100));
                tmp.setDate(calendar.getTime());
                calendar.add(calendar.DATE,-1);
                mMeasurementViewModel.insert(tmp);
            }

        }else if (id == R.id.delete_sample_data){
            mMeasurementViewModel.deleteAll();
        }else if (id == R.id.graph){
            if(countDifferentDaysOfMeasurements()>1)
            {
                openGraphActivity();
            }else{
                Context context = getApplicationContext();
                CharSequence text = "Need at least 2 measurements";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }

        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_stat) {
            openStatActivity();

        } else if (id == R.id.nav_settings) {
            openScheduleActivity();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void openStatActivity(){
        Intent intent = new Intent(this,StatActivity.class);
        startActivity(intent);
    }

    public void openScheduleActivity(){
        Intent intent = new Intent(this,SettingsActivity.class);
        startActivity(intent);
    }

    public void openGraphActivity(){
        Intent intent = new Intent(this,GraphActivity.class);
        startActivity(intent);
    }

    public int countDifferentDaysOfMeasurements()
    {
        List<Measurement> measurements=mMeasurementViewModel.getAllMeasurementsStatic();
        Map<String,Integer> differentDaysOfMeasurements = new HashMap<String,Integer>();
        for(Measurement measurement:measurements)
        {
            differentDaysOfMeasurements.putIfAbsent(new SimpleDateFormat("yyyy-MM-dd").format(measurement.getDate()),measurement.getWaterPercentage());
        }
        return differentDaysOfMeasurements.size();
    }

    private void sendMeasuringRequest() {
        mRequestQueue= Volley.newRequestQueue(this);
        stringRequest=new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                response=response.replaceAll("(\\r|\\n)", "");
                int actualResponse= Integer.parseInt(response);
                int convertedValue=calculatePercentage(actualResponse);
                Measurement measurement=new Measurement(convertedValue);
                mMeasurementViewModel.insert(measurement);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG,"error: " + error.toString());
            }
        });

        mRequestQueue.add(stringRequest);
    }
    //Analog reader provides a value between 0-4095.
    //4095 means dry soil.
    //I want to show the water percentage of the soil therefor i have to invert to usual percentage calculation
        //with calcualtedPercenage-100*-1
    //For example if I measured a value of 1000 and I round the maximum value to 4000
        //then I measured 25%, but I actually want to return 75% because the soil is not dry
    private int calculatePercentage(int analogInput)
    {
        return (int)((((double)analogInput/4095)*100)-100)*-1;
    }
}
