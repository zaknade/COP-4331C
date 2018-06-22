package com.example.santiagorook.selectaroute.views;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.santiagorook.selectaroute.R;
import com.example.santiagorook.selectaroute.database.AccessDB;
import com.example.santiagorook.selectaroute.entities.Schedule;

import java.io.IOException;
import java.util.ArrayList;

public class ViewRouteSchedule extends AppCompatActivity {

    AccessDB busTrackerDB = new AccessDB(this);
    ArrayList scheduleForRouteList = new ArrayList();
    ListView scheduleListView;

    // Variables To Store bundle info
    String selectedRoute;
    int selectedStopId;
    int selectedDirection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_select_stop_schedule);
        showBusRouteSchedule();
    }

    public void showBusRouteSchedule(){
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
        if(stopBundle != null && !stopBundle.isEmpty())
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
            }else{
                // Error a bundle key is missing
            }
        }else{
            // print and error
        }

        // Set Phone Screen Orientation to Landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        Schedule routeSchedule = busTrackerDB.getSchedule(selectedRoute,selectedDirection);
        scheduleForRouteList = new ArrayList(routeSchedule.getScheduleList());
        scheduleListView = (ListView) findViewById(R.id.routeSchedule);
        ListView routeListView = (ListView) findViewById(R.id.routeSchedule);
        ArrayAdapter<String> populateRouteListView = new ArrayAdapter<String>(routeListView.getContext(), android.R.layout.simple_list_item_1, scheduleForRouteList);
        routeListView.setAdapter(populateRouteListView);
    }
}