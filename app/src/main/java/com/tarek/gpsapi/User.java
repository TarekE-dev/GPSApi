package com.tarek.gpsapi;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable, Comparable<User> {

    private String name;
    private double lat;
    private double lon;
    private Location selfLocation;
    private Location userLocation = new Location("");

    protected User(Parcel in) {
        name = in.readString();
        lat = in.readDouble();
        lon = in.readDouble();
        selfLocation = in.readParcelable(Location.class.getClassLoader());
        userLocation.setLatitude(lat);
        userLocation.setLongitude(lon);
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel parcel) {
            return new User(parcel);
        }

        @Override
        public User[] newArray(int i) {
            return new User[i];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(name);
        parcel.writeDouble(lat);
        parcel.writeDouble(lon);
        parcel.writeParcelable(selfLocation, flags);
    }

    public User(String name, double lat, double lon, Location selfLocation){
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.selfLocation = selfLocation;
        userLocation.setLatitude(lat);
        userLocation.setLongitude(lon);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name; }

    public double getLongitude(){
        return lon;
    }

    public double getLatitude(){
        return lat;
    }

    public void updateLocation(Location location){
        this.lat = location.getLatitude();
        this.lon = location.getLongitude();
        userLocation.setLatitude(lat);
        userLocation.setLongitude(lon);
    }

    public void setSelfLocation(Location selfLocation) {
        this.selfLocation = selfLocation;
    }

    public double getDistanceToSelf() {
        return selfLocation.distanceTo(userLocation);
    }

    @Override
    public int compareTo(User o) {
        double thisDistance = this.getDistanceToSelf();
        double otherDistance = o.getDistanceToSelf();
        if(thisDistance < otherDistance) {
            return -1;
        } else if(thisDistance == otherDistance) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public String toString() {
        return String.format("username: %s, latitude: %f, longitude: %f", name, lat, lon);
    }
}
