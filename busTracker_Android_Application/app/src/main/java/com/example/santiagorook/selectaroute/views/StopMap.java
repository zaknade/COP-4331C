package com.example.santiagorook.selectaroute.views;

import android.content.Intent;
import android.database.SQLException;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.example.santiagorook.selectaroute.R;
import com.example.santiagorook.selectaroute.database.AccessDB;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;


public class StopMap extends FragmentActivity implements OnMapReadyCallback {

    Double stopLon, stopLat;
    String stopName;
    int selectedStopId;


    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

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

        Intent getRouteFromLastActivity = getIntent();
        Bundle stopBundle = getRouteFromLastActivity.getExtras();

        selectedStopId = stopBundle.getInt("stop_id");

        stopLat = new Double(busTrackerDB.getStopLat(selectedStopId));
        stopLon = new Double(busTrackerDB.getStopLon(selectedStopId));
        stopName = new String(busTrackerDB.getStopName(selectedStopId));

        LatLng location = new LatLng(stopLat, stopLon);
        mMap.addMarker(new MarkerOptions().position(location).title(stopName));
        float zoomLevel = 16.0f; //This goes up to 21
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoomLevel));
    }
}