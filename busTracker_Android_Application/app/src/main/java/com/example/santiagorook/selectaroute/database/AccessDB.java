package com.example.santiagorook.selectaroute.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.santiagorook.selectaroute.entities.Schedule;
import com.example.santiagorook.selectaroute.entities.User;
import com.google.android.gms.maps.model.LatLng;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class AccessDB extends SQLiteOpenHelper{

    private  static String DB_Path = "/data/data/com.example.santiagorook.selectaroute/databases/";

    private  static  String DB_Name = "bustracker.db";

    private SQLiteDatabase  BusTrackerDB;

    private final Context  BusTackerContext;

    public AccessDB(Context context) {
        super(context, DB_Name, null, 1);
        this.BusTackerContext = context;
    }

    @Override
    public void onUpgrade(SQLiteDatabase BusTrackerDB, int oldVersion, int newVersion) {
        if(newVersion>oldVersion)
            try {
                createNewDB();
            }catch (IOException e) {
                throw new Error("DB Copy Failed");
            }
    }

    // Create a database if necessary
    // It only needs to be created once off the existing db file
    public void createDatabase() throws IOException{

        boolean dbDoesExist = checkDatabase();
        if(!dbDoesExist)
        {
            // Create or open db if db is not connected
            this.getReadableDatabase();
            try{
                createNewDB();
            }catch (IOException e){
                throw new Error("DB Copy Failed");
           }
        }
    }

    public boolean checkDatabase(){
        SQLiteDatabase busTrackerDatabase = null;
        boolean dbExists = false;
        try{
            String path = DB_Path + DB_Name;
            busTrackerDatabase = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
        }catch (SQLiteException e)
        {
            //db dosent exists
        }

        if(busTrackerDatabase != null)
        {
            busTrackerDatabase.close();
            dbExists = true;
        }

        return dbExists;
    }

    public  void createNewDB() throws IOException{

        // Read DB file
        InputStream dbFile = BusTackerContext.getAssets().open(DB_Name);

        String dbPath = DB_Path + DB_Name;
        OutputStream dbOutput = new FileOutputStream(dbPath);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = dbFile.read(buffer))>0){
            dbOutput.write(buffer, 0, length);
        }

        dbOutput.flush();
        dbOutput.close();
        dbFile.close();
    }

    public void openDatabase() throws SQLException{
        String path = DB_Path + DB_Name;
        BusTrackerDB = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
    }

    // Get A List Of Routes and returns them as an arraylist
    public ArrayList getRoutes(){
        ArrayList routeDataList = new ArrayList();
        Cursor getData = getReadableDatabase().rawQuery("select _id, route_long_name from routes", null);
        // Make sure to begin at the first element of the returned data
        getData.moveToFirst();
        while (getData.moveToNext()){
            routeDataList.add(getData.getString(1));
        }

        getData.close();

        return  routeDataList;
    }

    // Gets all the bus stops given a route and returns them as an arraylist
    public ArrayList getStops(String route, int direction){
        // Will hold all the stops returned from query
        ArrayList stopDataList = new ArrayList();
        Cursor getData = getReadableDatabase().rawQuery("select _id from routes where route_long_name = '" + route + "'", null);
        // Make sure to begin at the first element of the returned data
        getData.moveToFirst();
        int route_id = getData.getInt(0);
        final String query = "select distinct Stops.Stop_Name " +
                "from Trips join " +
                "Calendar on Trips.Service_Id=Calendar._Id join " +
                "Routes on Trips.Route_Id=Routes._Id  join " +
                "Stop_Times on Trips._Id=Stop_Times.Trip_Id JOIN " +
                "Stops on Stop_Times.Stop_Id=Stops._Id " +
                "where Calendar._Id in " + getDayOfTheWeek() + " " +
                "AND Trips.Route_Id= " + route_id + " " +
                "AND Stop_Times.Arrival_Time != '' " +
                "AND direction_id = " + direction;
        getData.moveToFirst();
        getData = getReadableDatabase().rawQuery(query, null);
        // the data count is 0 when a route is not in service on a particular day
        if(getData.getCount() == 0){
            stopDataList.add("No Stops Available Today For This Route");
            return stopDataList;
        }else{
            while (getData.moveToNext()){
                stopDataList.add(getData.getString(0));
            }
        }

        getData.close();

        return  stopDataList;
    }

    // Gets the id of a bus stop based on the bus stop name
    public int getStopId(String stop){
        int stop_id;
        Cursor getData = getReadableDatabase().rawQuery("select _id from stops where stop_name = '"+ stop +"'", null);
        // Make sure to begin at the first element of the returned data
        getData.moveToFirst();
        stop_id = getData.getInt(0);

        getData.close();

        return stop_id;
    }

    // Fetch the schedule for given route and direction for a speicifc day of the week (determine automatically)
    public Schedule getSchedule(String route, int direction){
        // A schedule to hold all the data returned from the query
        Schedule weekdayZero = new Schedule();
        // Fetch the route id given the route
        Cursor getData = getReadableDatabase().rawQuery("select _id from routes where route_long_name = '" + route + "'", null);
        // Make sure to begin at the first element of the returned data
        getData.moveToFirst();
        // Store the Route Id
        int route_id = getData.getInt(0);
        // Calendar id: (90400,90448) - Monday To Friday
        final String query = "select Routes.Route_long_Name, " +
                "Calendar._Id, Stops.Stop_Name, Stop_Times.Stop_Sequence, Stop_Times.Arrival_Time, routes.route_short_name " +
                "from Trips join Calendar on Trips.Service_Id=Calendar._Id " +
                "join Routes on Trips.Route_Id=Routes._Id " +
                "join Stop_Times on Trips._Id=Stop_Times.Trip_Id " +
                "join Stops on Stop_Times.Stop_Id=Stops._Id " +
                "where Calendar._Id in " + getDayOfTheWeek() + " " +
                "AND Trips.Route_Id = " + route_id + " " +
                "AND Trips.Direction_Id = " + direction + " " +
                "AND Stop_Times.Arrival_Time != '' " +
                "order by arrival_time asc";
        getData.moveToFirst();
        getData = getReadableDatabase().rawQuery(query, null);
        while (getData.moveToNext()){
            weekdayZero.insertData(getData.getString(0), getData.getInt(1), getData.getString(2), getData.getInt(3)
                    , getData.getString(4), getData.getString(5));
        }
        return weekdayZero;
    }

    public Schedule getScheduleForSelectedStopAndRoute(String route, int direction, int stopId){
        // A schedule to hold all the data returned from the query
        Schedule selectedStopSchedule = new Schedule();
        // Fetch the route id given the route
        Cursor getData = getReadableDatabase().rawQuery("select _id from routes where route_long_name = '" + route + "'", null);
        // Make sure to begin at the first element of the returned data
        getData.moveToFirst();
        // Store the Route Id
        int route_id = getData.getInt(0);
        // Calendar id: (90400,90448) - Monday To Friday
        final String query = "select Routes.Route_long_Name, " +
                "Calendar._Id, Stops.Stop_Name, Stop_Times.Stop_Sequence, Stop_Times.Arrival_Time, routes.route_short_name " +
                "from Trips join Calendar on Trips.Service_Id=Calendar._Id " +
                "join Routes on Trips.Route_Id=Routes._Id " +
                "join Stop_Times on Trips._Id=Stop_Times.Trip_Id " +
                "join Stops on Stop_Times.Stop_Id=Stops._Id " +
                "where Calendar._Id in " + getDayOfTheWeek() + " " +
                "AND Trips.Route_Id = " + route_id + " " +
                "AND Trips.Direction_Id = " + direction + " " +
                "AND Stop_Times.Arrival_Time != '' " +
                "AND stops._id = " + stopId + " " +
                "order by arrival_time asc";
        getData.moveToFirst();
        getData = getReadableDatabase().rawQuery(query, null);
        while (getData.moveToNext()){
            selectedStopSchedule.insertData(getData.getString(0), getData.getInt(1), getData.getString(2), getData.getInt(3)
                    , getData.getString(4), getData.getString(5));
        }
        return selectedStopSchedule;
    }

    // Get Stop Name Given A Stop ID
    public String getStopName(int stopId){
        String stopName;
        Cursor getData = getReadableDatabase().rawQuery("select distinct stop_name from stops where _id = " + stopId, null);
        // Make sure to begin at the first element of the returned data
        getData.moveToFirst();
        stopName = getData.getString(0);
        return  stopName;
    }

    // Get Stop Longitude
    public Double getStopLon(int stopId){
        Double stopLon;
        Cursor getData = getReadableDatabase().rawQuery("select distinct stop_lon, stop_lat from stops where _id = " + stopId, null);
        // Make sure to begin at the first element of the returned data
        getData.moveToFirst();
        stopLon = getData.getDouble(0);
        return  stopLon;
    }

    // Get Stop Latitude
    public Double getStopLat(int stopId){
        Double stopLat;
        Cursor getData = getReadableDatabase().rawQuery("select distinct stop_lat, stop_lat from stops where _id = " + stopId, null);
        // Make sure to begin at the first element of the returned data
        getData.moveToFirst();
        stopLat = getData.getDouble(0);
        return  stopLat;
    }

    public String getArrivalTime(int tripId){
        String arrivalTime;
        Cursor getData = getReadableDatabase().rawQuery("select arrival_time\n" +
                "from stop_times \n" +
                "WHERE arrival_time IS NOT '' AND trip_id = tripId", null);
        // Make sure to begin at the first element of the returned data
        getData.moveToFirst();
        arrivalTime = getData.getString(0);
        return  arrivalTime;
    }

    // I stored this here so its accessible to all classes that use the db
    // It fetches the calendar id based on the days of the week
    public String getDayOfTheWeek(){
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        String calendar_Id = null;
        switch (day) {
            case Calendar.SUNDAY:
                calendar_Id ="(90300)";
                break;
            case Calendar.MONDAY:
                calendar_Id ="(90400,90448)";
                break;
            case Calendar.TUESDAY:
                calendar_Id ="(90400,90448)";
                break;
            case Calendar.WEDNESDAY:
                calendar_Id ="(90400,90448)";
                break;
            case Calendar.THURSDAY:
                calendar_Id ="(90400,90448)";
                break;
            case Calendar.FRIDAY:
                calendar_Id ="(90400,90448)";
                break;
            case Calendar.SATURDAY:
                calendar_Id ="(90200,90238)";
                break;
        }

        return calendar_Id;
    }

    public void deleteRoute(String route){

        try {
            BusTrackerDB = this.getWritableDatabase();

            final String selectRouteQuery = "select _id from routes " +
                    "where route_long_name = '" + route + "'";

            Cursor getRoutes = getReadableDatabase().rawQuery(selectRouteQuery, null);

            getRoutes.moveToFirst();
            BusTrackerDB.delete("routes", "_id=?", new String[]{getRoutes.getString(0)});

            final String selectShapesQuery = "select distinct shape_id from trips " +
                    "where route_id = " + getRoutes.getString(0);
            final String selectTripsQuery = "select distinct _id from trips " +
                    "where route_id = " + getRoutes.getString(0);

            Cursor getShapes = getWritableDatabase().rawQuery(selectShapesQuery, null);
            Cursor getTrips = getWritableDatabase().rawQuery(selectTripsQuery, null);

            getShapes.moveToFirst();
            getTrips.moveToFirst();

            do {
                BusTrackerDB.delete("shapes", "shape_id=?", new String[]{getShapes.getString(0)});
            } while (getShapes.moveToNext());

            do {
                BusTrackerDB.delete("stop_times", "trip_id=?", new String[]{getTrips.getString(0)});
            } while (getTrips.moveToNext());

            getShapes.close();
            getTrips.close();

            BusTrackerDB.delete("trips", "route_id=?", new String[]{getRoutes.getString(0)});
            getRoutes.close();

        } catch (SQLiteException e) { };

    }

    // Get the Shape ID
    public Integer getShapeId(String routeName, int direction, int stopId) {
        Integer routeId, shapeID;
        Cursor getData = getReadableDatabase().rawQuery("select _id  from routes where route_long_name = '" + routeName + "'", null);
        getData.moveToFirst();
        // store the route ID base on the route name
        routeId = getData.getInt(0);
        // only get the shape ID
        final String query = "select trips.shape_id " +
                "from trips " +
                "join calendar on trips.service_id = calendar._id " +
                "join routes on trips.route_id = routes._id " +
                "join stop_times on trips._id = stop_times.trip_id " +
                "join stops on stop_times.stop_id = stops._id " +
                "where calendar._id in " + getDayOfTheWeek() +
                " AND routes._id = " + routeId +
                " AND trips.direction_id = " + direction +
                " AND stops._id = " + stopId + " ";
        getData = getReadableDatabase().rawQuery(query, null);
        getData.moveToFirst();
        shapeID = getData.getInt(0);

        return shapeID;
    }

    // Only store the key and set the list to null
    public Map<Integer, ArrayList<LatLng>> shapeIdList () {
        Map<Integer, ArrayList<LatLng>> newList = new HashMap<>();
        Cursor getData = getReadableDatabase().rawQuery("select shape_id from shapes", null);
        getData.moveToFirst();
        while (getData.moveToNext()) {
            newList.put(getData.getInt(0), null);
        }
        return newList;
    }

    // Take in the list that only contain the key. Base off the key add the list into it
    public Map<Integer, ArrayList<LatLng>> shapeCoordinates (Map<Integer, ArrayList<LatLng>> shapeIdList) {
        Cursor getData = getReadableDatabase().rawQuery("select shape_id, shape_pt_lat, shape_pt_lon from shapes", null);
        int count = 1;
        // Make sure start off the first position of the table
        getData.moveToFirst();
        // Store the index of end of the table
        int end = getData.getCount();
        // Loop through all the key
        for(Integer key : shapeIdList.keySet()) {
            // Create a new Arraylist
            ArrayList<LatLng> shapeCoordinates = new ArrayList<LatLng>();
            while (key == getData.getInt(0)) {
                // Add coordinate into the list
                shapeCoordinates.add(new LatLng(getData.getDouble(1), getData.getDouble(2)));
                count++;
                if (count == end) {
                    break;
                }
                // Move to next table
                getData.moveToPosition(count);
            }
            // store the key and list into the HashMap
            shapeIdList.put(key, shapeCoordinates);
        }

        return shapeIdList;
    }

    // Retrieves a user record from the user table
    // returns null if user does not exist
    public User getUser(String username) {

        User existingUser = new User();
        String usernameResult;
        String passwordResult;

        try {
            // Get access to DB and set cursor at first row returned
            Cursor getData = getReadableDatabase().rawQuery("SELECT * FROM user WHERE username = '" + username + "'", null);
            getData.moveToFirst();

            // Get fields from DB
            usernameResult = getData.getString(1);
            passwordResult = getData.getString(2);

            // Set fields in user object
            existingUser.setUsername(usernameResult);
            existingUser.setPassword(passwordResult);

            getData.close();
        } catch (IndexOutOfBoundsException e) { existingUser = null; }

        return existingUser;
    }

    // Inserts a new user record into the user table
    public int insertUser(User newUser) {

        try {
            BusTrackerDB = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("username", newUser.getUsername());
            values.put("password", newUser.getPassword());

            BusTrackerDB.insert("user", null, values);
        } catch (SQLiteException e) { return 0; }

        BusTrackerDB.close();

        return 1;
    }

    @Override
    public synchronized  void close(){
        if (BusTrackerDB != null)
                BusTrackerDB.close();

        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }
}
