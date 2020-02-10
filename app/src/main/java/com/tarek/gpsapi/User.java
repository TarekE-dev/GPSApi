package com.tarek.gpsapi;

import android.location.Location;

public class User {

    private String name;
    private Location location;

    public User(String name, Location location){
        this.name = name;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public Location getLocation(){
        return location;
    }

    public double getLongitude(){
        return location.getLongitude();
    }

    public double getLatitude(){
        return location.getLatitude();
    }
}
