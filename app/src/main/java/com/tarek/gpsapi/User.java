package com.tarek.gpsapi;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

    private String name;
    private double lat;
    private double lon;
    private Location userLocation = new Location("");

    protected User(Parcel in) {
        name = in.readString();
        lat = in.readDouble();
        lon = in.readDouble();
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
    }

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

    public void setName(String name) { this.name = name; }

    public double getLongitude(){
        return lat;
    }

    public double getLatitude(){
        return lon;
    }

    public void updateLocation(Location location){
        this.lat = location.getLatitude();
        this.lon = location.getLongitude();
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
