package com.example.santiagorook.selectaroute.views;

import android.content.Intent;
import android.database.SQLException;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.santiagorook.selectaroute.R;
import com.example.santiagorook.selectaroute.database.AccessDB;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;


public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback, GoogleMap.OnPolygonClickListener, GoogleMap.OnPolylineClickListener{


    private GoogleMap mMap;

    Double stopLon, stopLat;
    String stopName;
    private int selectedStopId, selectedDirection;
    private int shapeId;
    private String selectedRoute;

    AccessDB busTrackerDB = new AccessDB(this);

    private Map<Integer, ArrayList<LatLng>> shapeList = new HashMap<>();
    private Map<Integer, ArrayList<LatLng>> tmpList = new HashMap<>();
    private ArrayList<LatLng> coordinateList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

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

        try {
            busTrackerDB.createDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            busTrackerDB.openDatabase();
        } catch (SQLException sqle) {
            throw sqle;
        }

        // Make sure get the bundle that pass into this activity
        Intent getRouteFromLastActivity = getIntent();
        Bundle stopBundle = getRouteFromLastActivity.getExtras();

        if (!stopBundle.isEmpty()) {

            // Verify the bundle contains the data we need -> s3 route, stop_id, direction
            boolean hasRoute = stopBundle.containsKey("route");
            boolean hasStop = stopBundle.containsKey("stop_id");
            boolean hasDirection = stopBundle.containsKey("direction");
            if (hasRoute && hasStop && hasDirection) {
                selectedRoute = stopBundle.getString("route");
                selectedStopId = stopBundle.getInt("stop_id");
                selectedDirection = stopBundle.getInt("direction");

                // Get the shape Id base off route name, direction, and stop ID
                shapeId = new Integer(busTrackerDB.getShapeId(selectedRoute, selectedDirection, selectedStopId));

            } else {
                ;
            }
        } else {

        }

        // Get the Hashmap that only containt the key
        tmpList = busTrackerDB.shapeIdList();

        // Get the Hashmap that has list of coordinates
        shapeList = busTrackerDB.shapeCoordinates(tmpList);

        // Loop through the key
        for (Integer i : shapeList.keySet()) {

            // Get the list of coordinates that only match with the shape ID
            if (i == shapeId) {
                coordinateList.addAll(shapeList.get(i));
            }
        }
        // Just make sure there are actual coordinate in the list
//        for (LatLng value : coordinateList) {
//            System.out.println(value);
//        }

        // Create polyline object
        PolylineOptions polylineOptions = new PolylineOptions();

        // Define the polyline width and color
        polylineOptions.width(20).color(Color.RED).geodesic(true);

        // Add all the coordinate into the polyline object
        polylineOptions.addAll(coordinateList);

        // Make sure the map is clean then add the polyline into the map
        mMap.clear();
        mMap.addPolyline(polylineOptions);

        // Build a object of coordinate list
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : coordinateList) {
            builder.include(latLng);
        }

        final LatLngBounds bounds = builder.build();

        // Zoom into the polyline object only
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 0);
        mMap.animateCamera(cu);
    }

    @Override
    public void onPolygonClick(Polygon polygon) {

    }

    @Override
    public void onPolylineClick(Polyline polyline) {

    }
}
