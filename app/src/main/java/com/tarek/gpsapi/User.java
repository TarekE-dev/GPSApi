package com.tarek.gpsapi;

public class User {

    private String name;
    private double lat;
    private double lon;

    public User(String name, double lat, double lon){
        this.name = name;
        this.lat = lat;
        this.lon = lon;
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
    }

    @Override
    public String toString() {
        return String.format("username: %s, latitude: %f, longitude: %f", name, lat, lon);
    }
}
