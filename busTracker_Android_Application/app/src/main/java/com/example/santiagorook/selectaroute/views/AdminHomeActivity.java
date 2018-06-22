package com.example.santiagorook.selectaroute.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.view.View;

import com.example.santiagorook.selectaroute.R;

public class AdminHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        Button viewArrivalButton = (Button) findViewById(R.id.viewArrivalTimes);
        viewArrivalButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                viewArrivalOnClick(view);
            }
        });

        Button deleteRouteButton = (Button) findViewById(R.id.deleteRoute);
        deleteRouteButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                deleteRouteOnClick(view);
            }
        });

        Button signOutButton = (Button) findViewById(R.id.signOut);
        signOutButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                signOutOnClick(view);
            }
        });
    }

    // view arrival times of all routes
    public void viewArrivalOnClick(View v)
    {
        Intent getUsername = getIntent();
        String username = getUsername.getStringExtra("username");

        //placeholder
        Intent viewArrival = new Intent(this, SelectARoute.class);
        viewArrival.putExtra("username", username);
        startActivity(viewArrival);
    }

    // delete routes
    public void deleteRouteOnClick(View v)
    {
        //placeholder
        Intent deleteRoutes = new Intent(this, DeleteARoute.class);
        startActivity(deleteRoutes);
    }

    // sign out
    public void signOutOnClick(View v)
    {
        Intent signOut = new Intent(this, HomeLogin.class);
        //requires signout code
        startActivity(signOut);
    }
}