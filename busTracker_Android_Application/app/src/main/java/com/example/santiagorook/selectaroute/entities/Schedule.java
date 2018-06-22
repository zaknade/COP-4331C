package com.example.santiagorook.selectaroute.entities;

import java.util.ArrayList;

public class Schedule {

    private ArrayList<String> route_long_name = new ArrayList<String>() ;
    private ArrayList<Integer>  calendar_id = new ArrayList<Integer> ();
    private ArrayList<String>  stop_name = new ArrayList<String> ();
    private ArrayList<Integer>  stop_sequence = new ArrayList<Integer> ();
    private ArrayList<String>  arrival_time = new ArrayList<String> ();
    private ArrayList<String>  route_short_name = new ArrayList<String> ();

    // Insert schedule data
    public void insertData(String r_long_name, int cal_id, String name_of_stop, int stop_seq, String a_time, String r_short_name){
        route_long_name.add(r_long_name);
        calendar_id.add(cal_id);
        stop_name.add(name_of_stop);
        stop_sequence.add(stop_seq);
        arrival_time.add(a_time);
        route_short_name.add(r_short_name);
    }

    // returns a schedule in the form of an arraylist with some of the elements concat for list view
    public ArrayList getScheduleList(){
        ArrayList<String> scheduleList = new ArrayList<String>();
        scheduleList.add("Format: Route Number | Stop Name | Arrival Time");
        for (int i = 0; i < route_short_name.size(); i++){
            scheduleList.add(getRShortNameList().get(i) + " "
                    + "\t| " + stop_name.get(i) + "\t| " + getArrivalTimesList().get(i));
        }
        return scheduleList;
    }

    public ArrayList getRLongNameList()
    {
        return route_long_name;
    }

    public ArrayList getCalendarIdList()
    {
        return calendar_id;
    }

    public ArrayList getStopNameList()
    {
        return stop_name;
    }

    public ArrayList getStopSequenceList()
    {
        return stop_sequence;
    }

    public ArrayList getArrivalTimesList()
    {
        return arrival_time;
    }

    public ArrayList getRShortNameList()
    {
        return route_short_name;
    }
}
