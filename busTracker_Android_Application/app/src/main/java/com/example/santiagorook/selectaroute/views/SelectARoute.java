package com.example.santiagorook.selectaroute.views;

import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.santiagorook.selectaroute.R;
import com.example.santiagorook.selectaroute.database.AccessDB;
import com.example.santiagorook.selectaroute.entities.User;

import java.io.IOException;
import java.util.ArrayList;

public class SelectARoute extends AppCompatActivity {

    // A single route selected by the user
    public final ArrayList route = new ArrayList();
    ArrayList routeList;
    User existingUser;
    AccessDB BusTrackerDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_aroute);

        populateList();
    }

    public void populateList()
    {
        AccessDB busTrackerDB = new AccessDB(this);

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

         //Populate List with Routes
        routeList = new ArrayList(busTrackerDB.getRoutes());

        final ListView routeListView = (ListView) findViewById(R.id.routeListActivity);
        final ArrayAdapter<String> populateRouteListView = new ArrayAdapter<String>(routeListView.getContext(), android.R.layout.simple_list_item_1, routeList);
        routeListView.setAdapter(populateRouteListView);

        routeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                String selectedRoute = (String) parent.getItemAtPosition(position);
                // Get the route the user selected (only 1. The arraylist is the only way i could get it to work)
                route.add(selectedRoute);
                //Move to Next Activity - Select A Stop
                goToSelectAStopActivity(view);
            }
        });
    }

    public void goToSelectAStopActivity(View view){
        Intent intent = new Intent(this, SelectAStop.class);

        // Pass route selected by user to SelectAStop
        Bundle bundle = new Bundle();
        bundle.putString("route", route.get(0).toString());
        intent.putExtras(bundle);

        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        Intent getUsername = getIntent();
        String username = getUsername.getStringExtra("username");

        if(username == null || !username.equals("admin")) {
            Intent intent = new Intent(SelectARoute.this, UserHomeActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        }
        else {
            Intent intent = new Intent(SelectARoute.this, AdminHomeActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        }
    }
}
