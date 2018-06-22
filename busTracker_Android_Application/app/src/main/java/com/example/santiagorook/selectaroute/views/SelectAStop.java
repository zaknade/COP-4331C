package com.example.santiagorook.selectaroute.views;

import android.content.Intent;
import android.database.SQLException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.santiagorook.selectaroute.R;
import com.example.santiagorook.selectaroute.database.AccessDB;

import java.io.IOException;
import java.util.ArrayList;

public class SelectAStop extends AppCompatActivity {

    // A single stop selected by the user
    ArrayList stop = new ArrayList();

    // Will store the id of the user's chosen bus stop
    ArrayList<Integer> stopID = new ArrayList<Integer>();

    // The direction the bus moves on the route ( selected by user )
    int direction;

    String selectedRoute = null;
    ArrayList stopList = null;
    AccessDB busTrackerDB = new AccessDB(this);
    ArrayAdapter<String> populateRouteListView;
    ListView stopListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectastop);

        populateBusStopList();
    }

    public void populateBusStopList()
    {
        try{
            busTrackerDB.createDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            busTrackerDB.openDatabase();
        }catch (SQLException sqle){
            throw sqle;
        }

        // Fetch the intent and bundle from SelectARoute
        Intent getRouteFromLastActivity = getIntent();
        Bundle routeBundle = getRouteFromLastActivity.getExtras();
        if(!routeBundle.isEmpty())
        {
            // Verify the bundle contains the data we need -> route
            boolean hasString = routeBundle.containsKey("route");
            if(hasString)
                selectedRoute = routeBundle.getString("route");
        }else{
            // print and error
        }

        // Set Defaults On List
        //Populate List with Routes
        stopList = new ArrayList(busTrackerDB.getStops(selectedRoute, 1));
        //Set Route a final string so you can transfer it to the next activity when the user click on a stop
        stopListView = (ListView) findViewById(R.id.stopListActivity);
        populateRouteListView = new ArrayAdapter<String>(stopListView.getContext(), android.R.layout.simple_list_item_1, stopList);
        stopListView.setAdapter(populateRouteListView);


        // Button For Schedule A
        Button scheduleAButton = (Button) findViewById(R.id.scheduleAButton);
        scheduleAButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set Direction To 1
                direction = 1;

                stopList = new ArrayList(busTrackerDB.getStops(selectedRoute, direction));
                //Set Route a final string so you can transfer it to the next activity when the user click on a stop
                populateRouteListView = new ArrayAdapter<String>(stopListView.getContext(), android.R.layout.simple_list_item_1, stopList);
                stopListView.setAdapter(populateRouteListView);
            }
        });

        // Button For Schedule B
        Button scheduleBButton = (Button) findViewById(R.id.scheduleBButton);
        scheduleBButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set Direction To 0
                direction = 0;
                stopList = new ArrayList(busTrackerDB.getStops(selectedRoute, direction));
                //Set Route a final string so you can transfer it to the next activity when the user click on a stop
                populateRouteListView = new ArrayAdapter<String>(stopListView.getContext(), android.R.layout.simple_list_item_1, stopList);
                stopListView.setAdapter(populateRouteListView);
            }
        });

        stopListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                String selectedStop = (String) parent.getItemAtPosition(position);
                // Save The Selected Stop
                stop.add(selectedStop);
                // Get the ID for the selected stop and store it
                stopID.add(busTrackerDB.getStopId(selectedStop));
                // Move Onto the Next Activity and Transfer data
                goToShowStopAndBusInfo(view, selectedRoute);
            }
        });
    }

    // Go to the next activity
    public void goToShowStopAndBusInfo(View view, String selectedRoute){
        Intent intent = new Intent(this, ShowStopAndBusInfo.class);

        // Pass stop id and route name to show ShowStopAndBusInfo
        Bundle bundle = new Bundle();
        bundle.putInt("direction", direction);
        bundle.putInt("stop_id", stopID.get(0));
        bundle.putString("route", selectedRoute);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, SelectARoute.class));
    }
}
