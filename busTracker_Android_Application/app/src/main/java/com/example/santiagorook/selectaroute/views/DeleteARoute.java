package com.example.santiagorook.selectaroute.views;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import com.example.santiagorook.selectaroute.R;
import com.example.santiagorook.selectaroute.database.AccessDB;

import java.io.IOException;
import java.util.ArrayList;

public class DeleteARoute extends AppCompatActivity {

    // A single route selected by the user
    ArrayList route = new ArrayList();
    ArrayList routeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_aroute);

        populateList();
    }

    public void populateList()
    {
        final AccessDB busTrackerDB = new AccessDB(this);

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
        //s1
        routeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                String selectedRoute = (String) parent.getItemAtPosition(position);
                populateRouteListView.remove(selectedRoute);
                busTrackerDB.deleteRoute(selectedRoute);

                AlertDialog.Builder message = new AlertDialog.Builder(DeleteARoute.this);

                message.setTitle("Route Deletion");
                message.setMessage("Route Deletion Successful!");
                message.setCancelable(false);
                message.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = message.create();
                alert.show();
            }
        });
    }
}

