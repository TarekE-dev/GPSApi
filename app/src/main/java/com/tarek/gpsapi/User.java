package com.tarek.gpsapi;

import android.location.Location;

public class User {

    private String name;
    private double lat;
    private double lon;
    private Location userLocation = new Location("");

    public User(String name, double lat, double lon){
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        userLocation.setLatitude(lat);
        userLocation.setLongitude(lon);
    }

    public String getName() {
        return name;
    }

    public double getLongitude(){
        return lat;
    }

    public double getLatitude(){
        return lon;
    }

    public void updateLocation(double lat, double lon){
        this.lat = lat;
        this.lon = lon;
        userLocation.setLatitude(lat);
        userLocation.setLongitude(lon);
    }

    public Location getUserLocation(){
        return userLocation;
    }

    public double distanceToUser(User other){
        return userLocation.distanceTo(other.userLocation);
    }

    @Override
    public String toString() {
        return String.format("username: %s, latitude: %f, longitude: %f", name, lat, lon);
    }
}
