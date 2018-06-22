package com.example.santiagorook.selectaroute.views;

import android.content.Intent;
import android.database.SQLException;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.santiagorook.selectaroute.R;
import com.example.santiagorook.selectaroute.database.AccessDB;
import com.example.santiagorook.selectaroute.entities.Schedule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ShowStopAndBusInfo extends AppCompatActivity {

    AccessDB busTrackerDB = new AccessDB(this);
    Schedule weekdayZero = new Schedule();
    private CountDownTimer busArrivaltimer;

    // duration of the timer
    int duration;

    // duration in milliseconds
    int getdurationInMilliseconds;

    //schedule
    Schedule busArrivalStopSchedule;

    // Save the time at which the next store will arrive
    String nextBusArrivalTime;

    // The count down text
    private TextView timeRemaining;
    // Stop Info Text
    private TextView stopName;
    // Route Info Text
    private TextView routeName;
    // Route No Buses Text
    private TextView noMoreBuses;
    // Bus Arrival Time Text
    private TextView busArrivalTime;
    // Button For Schedule
    Button scheduleButton, stopMapButton, btnMaps;

    // Variables To Store bundle info
    String selectedRoute;
    int selectedStopId;
    int selectedDirection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_stop_and_bus_info);
        displayStopInformation();
    }


    public void displayStopInformation(){
        // Initalize The Text view
        timeRemaining = (TextView) findViewById(R.id.countDown);
        stopName = (TextView) findViewById(R.id.stopNameText);
        routeName = (TextView) findViewById(R.id.routeNameText);
        busArrivalTime = (TextView) findViewById(R.id.busArrivalTime);
        noMoreBuses = (TextView) findViewById(R.id.noBuses);
        noMoreBuses.setVisibility(View.GONE);

        // Initialize the button
        scheduleButton= (Button) findViewById(R.id.viwRouteSchedule);
        stopMapButton= (Button) findViewById(R.id.viewStopMap);
        btnMaps = (Button) (findViewById(R.id.btnMapsView));

        // Create DB if neccessary ( based on an existing db file )
        try{
            busTrackerDB.createDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Open DB
        try {
            busTrackerDB.openDatabase();
        }catch (SQLException sqle){
            throw sqle;
        }

        // Fetch the intent and bundle from SelectAStop
        Intent getRouteFromLastActivity = getIntent();
        Bundle stopBundle = getRouteFromLastActivity.getExtras();
        if(!stopBundle.isEmpty())
        {
            // Verify the bundle contains the data we need -> route, stop_id, direction
            boolean hasRoute = stopBundle.containsKey("route");
            boolean hasStop = stopBundle.containsKey("stop_id");
            boolean hasDirection = stopBundle.containsKey("direction");
            if(hasRoute && hasStop && hasDirection)
            {
                selectedRoute = stopBundle.getString("route");
                selectedStopId = stopBundle.getInt("stop_id");
                selectedDirection = stopBundle.getInt("direction");

                // Set information text
                stopName.setText("Stop: " + busTrackerDB.getStopName(selectedStopId));
                routeName.setText("Route: " + selectedRoute);
            }else{
                // Error a bundle key is missing
            }
        }else{
            // print and error
        }

        getdurationInMilliseconds = returnBusArrivlatime(selectedStopId, selectedDirection, selectedRoute);
        if(getdurationInMilliseconds != -1){
            duration = getdurationInMilliseconds;
            busArrivaltimer = new CountDownTimer(duration,1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    String text = String.format(Locale.getDefault(), "%02d:%02d:%02d ",
                            TimeUnit.MILLISECONDS.toHours(millisUntilFinished) % 60,
                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60,
                            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60);
                    timeRemaining.setText(text);
                }

                @Override
                public void onFinish() {

                }

            };

            busArrivalTime.setText("The Next Bus Will Arrive At: " + nextBusArrivalTime);
            // Start the Timer
            busArrivaltimer.start();
        }else{
            timeRemaining.setVisibility(View.GONE);
            noMoreBuses.setVisibility(View.VISIBLE);
            scheduleButton.setVisibility(View.GONE);
            btnMaps.setVisibility(View.GONE);
        }

        // Button for moving to the next activity  -> viewing the route schedule
        scheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToViewRouteSchedule(v);
            }
        });

        // Button for moving to the next activity  -> viewing the route schedule
        stopMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToStopMap(v);
            }
        });

        btnMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToMapsView(view);
            }
        });

    }

    public int returnBusArrivlatime(int stopId, int direction, String  route)
    {
        int timeInMillSeconds;
        // Get the current time in hours:minutes:seconds
        Calendar currentTime = Calendar.getInstance();
        int hours = currentTime.get(Calendar.HOUR_OF_DAY);
        int minutes = currentTime.get(Calendar.MINUTE);
        int seconds = currentTime.get(Calendar.SECOND);
        int time = (hours * 1000 * 60 * 60) + (minutes * 1000 * 60) + (seconds * 1000);
        int savedHours = 0;
        int savedMinutes = 0;
        int savedSeconds = 0;
        // Fetch the schedule from the database
        busArrivalStopSchedule = busTrackerDB.getScheduleForSelectedStopAndRoute(route, direction, stopId);
        // Store the times from the schedule for a bus stop
        ArrayList times = busArrivalStopSchedule.getArrivalTimesList();
        // Iterate through the schedule
        Iterator iter = times.iterator();
        while (iter.hasNext()){
            // Where the parsed time string from db will be stored
            String splitTime[];
            String timeString = iter.next().toString();
            // Parse the time string
            splitTime = timeString.split(":");
            int splitHour = Integer.parseInt(splitTime[0]);
            int splitMinute = Integer.parseInt(splitTime[1]);
            int splitSecond= Integer.parseInt(splitTime[2]);

            savedHours = splitHour;
            savedMinutes = splitMinute;
            savedSeconds = splitSecond;
            int savedTime = (savedHours * 1000 * 60 * 60) + (savedMinutes * 1000 * 60) + (savedSeconds * 1000);
            // Find the difference between the current time and the next bus arrival time in current
            timeInMillSeconds =  time - savedTime;
            if(timeInMillSeconds < 0 || timeInMillSeconds == 0)
            {
                nextBusArrivalTime = timeString;
                return Math.abs(timeInMillSeconds);
            }
        }

        // returns -1 bus arrival times found past our current time
        return -1;
    }

    // Go to the ViewRouteSchedule Activity
    public void goToViewRouteSchedule(View view){
        Intent intent = new Intent(this, ViewRouteSchedule.class);

        // Pass stop id and route name to show ShowStopAndBusInfo
        Bundle bundle = new Bundle();
        bundle.putInt("direction", selectedDirection);
        bundle.putInt("stop_id", selectedStopId);
        bundle.putString("route", selectedRoute);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    // Go to the next StopMap Activity
    public void goToStopMap(View view){
        Intent intent = new Intent(this, StopMap.class);

        // Pass stop id and route name to show ShowStopAndBusInfo
        Bundle bundle = new Bundle();
        bundle.putInt("direction", selectedDirection);
        bundle.putInt("stop_id", selectedStopId);
        bundle.putString("route", selectedRoute);
        bundle.putString("stop_name", stopName.toString());
        bundle.putString("time_remaining", timeRemaining.toString());
        intent.putExtras(bundle);
        startActivity(intent);
    }

    // When user click on "Maps View" button it will start the activity and pass the bundle to that activity
    public void goToMapsView(View view){
        Intent intent = new Intent(this, MapsActivity.class);

        // Pass stop id and route name to show ShowStopAndBusInfo
        Bundle bundle = new Bundle();
        bundle.putInt("direction", selectedDirection);
        bundle.putInt("stop_id", selectedStopId);
        bundle.putString("route", selectedRoute);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, SelectARoute.class));
    }
}
