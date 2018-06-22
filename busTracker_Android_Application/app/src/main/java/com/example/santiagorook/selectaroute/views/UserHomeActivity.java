package com.example.santiagorook.selectaroute.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.example.santiagorook.selectaroute.*;

public class UserHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*
        // Functionality for View Route Button
        Button viewRouteButton = (Button) findViewById(R.id.viewRouteButton);
        viewRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewArrivalOnClick(view);
            }
        });
        */

        // Functionality for View Arrival Times Button
        Button viewArrivalTimeButton = (Button)findViewById(R.id.viewArrivalTimeButton);
        viewArrivalTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewArrivalOnClick(view);
            }
        });

        // Functionality for sign out Button
        final Button signOutButton = (Button) findViewById(R.id.signOutButton);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOutOnClick(view);
            }
        });
    }

    public void signOutOnClick(View v)
    {
        Intent signOut = new Intent(this, HomeLogin.class);
        startActivity(signOut);
    }

    public void viewMapOnClick(View v)
    {
        Intent viewMap = new Intent(this, StopMap.class);
        startActivity(viewMap);
    }

    public void viewArrivalOnClick(View v)
    {
        Intent getUsername = getIntent();
        String username = getUsername.getStringExtra("username");

        Intent viewArrival = new Intent(this, SelectARoute.class);
        viewArrival.putExtra("username", username);
        startActivity(viewArrival);
    }



}