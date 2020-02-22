package com.tarek.gpsapi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MapViewFragment.mapViewInterface {

    private final String SAVED_USERS = "SAVED_USERS";
    private final String SELF = "SELF";
    private final String USERS = "USERS";
    private final String MAP_FRAGMENT = "MAP_FRAGMENT";
    private final String LIST_FRAGMENT = "LIST_FRAGMENT";

    private Button nameButton;
    private EditText nameEdit;

    private User self;
    private ArrayList<User> users;

    private FragmentManager fm;
    private MapViewFragment mapViewFragment;
    private RecyclerViewFragment recyclerViewFragment;

    private LocationManager lm;
    private LocationListener ll;
    private Location currentLocation;

    private Gson gson = new Gson();

    private Handler apiHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            String jsonResponse = (String) msg.obj;
            users = getUsers(jsonResponse);
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            lm = getSystemService(LocationManager.class);
        }
        fm = getSupportFragmentManager();
        initListener();
        setButtonFunction();
        self = loadUser();
        users = loadAllUsers();
        if(users == null) {
            callGetUsersAPI(getResources().getString(R.string.getUsersAPI));
        }
        mapViewFragment = (MapViewFragment) fm.findFragmentByTag(MAP_FRAGMENT);
        recyclerViewFragment = (RecyclerViewFragment) fm.findFragmentByTag(LIST_FRAGMENT);
        initMapFragment();

    }

    private void initMapFragment(){
        if(mapViewFragment == null) {
            mapViewFragment = MapViewFragment.newInstance(self, users);
            fm.beginTransaction().add(R.id.mapFragmentContainer, mapViewFragment, MAP_FRAGMENT).commit();
        } else {
            fm.beginTransaction().remove(mapViewFragment);
            mapViewFragment = MapViewFragment.newInstance(self, users);
            fm.beginTransaction().add(R.id.mapFragmentContainer, mapViewFragment, MAP_FRAGMENT).commit();
        }
    }

    private void setButtonFunction() {
        nameButton = findViewById(R.id.nameButton);
        nameEdit = findViewById(R.id.nameEdit);
        nameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEdit.getText().toString();
                if(self == null) {
                    self = new User(name, currentLocation.getLatitude(), currentLocation.getLongitude());
                } else {
                    self.setName(name);
                }
                saveUser();
            }
        });
    }

    private void initListener() {
        ll = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(self != null){
                    self.updateLocation(location);
                    saveUser();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };
        if(currentLocation == null && checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            currentLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
    }

    private ArrayList<User> loadAllUsers() {
        SharedPreferences sp = getSharedPreferences(SAVED_USERS, MODE_PRIVATE);
        String jsonUsers = sp.getString(USERS, null);
        if(jsonUsers == null){
            return null;
        }
        return allUsersFromJson(jsonUsers);
    }

    private void saveAllUsers() {
        if(users == null){
            return;
        }
        SharedPreferences sp = getSharedPreferences(SAVED_USERS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(USERS, allUsersToJson(users));
        editor.commit();
    }

    private String allUsersToJson(ArrayList<User> listUsers){
        return gson.toJson(listUsers);
    }

    private ArrayList<User> allUsersFromJson(String json){
        return gson.fromJson(json, ArrayList.class);
    }

    private User loadUser() {
        SharedPreferences sp = getSharedPreferences(SAVED_USERS, MODE_PRIVATE);
        String jsonUser = sp.getString(SELF, null);
        if(jsonUser == null){
            return null;
        }
        return userFromJson(jsonUser);
    }

    private User userFromJson(String json){
        return gson.fromJson(json, User.class);
    }

    private void saveUser() {
        if(self == null) {
            return;
        }
        SharedPreferences sp = getSharedPreferences(SAVED_USERS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(SELF, userToJson(self));
        editor.commit();
    }

    private String userToJson(User user){
        return gson.toJson(user);
    }


    private void setUp(){
        for(User user : users) {
            System.out.println(user.toString());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            System.exit(-1);
        }
    }

    private void callGetUsersAPI(final String apiURL){
        new Thread() {
            @Override
            public void run(){
                try {
                    URL apiToCall = new URL(apiURL);
                    URLConnection connection = apiToCall.openConnection();
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String currentLine;
                    while (true){
                        currentLine = in.readLine();
                        if(currentLine == null)
                            break;
                        sb.append(currentLine);
                    }
                    in.close();
                    Message msg = Message.obtain();
                    msg.obj = sb.toString();
                    apiHandler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private ArrayList<User> getUsers(String jsonResponse) {
        ArrayList<User> tempUsers = new ArrayList<User>();
        JSONArray reader = null;
        try {
            reader = new JSONArray(jsonResponse);
        } catch (JSONException e){
            e.printStackTrace();
        }
        for(int user = 0; user < reader.length(); user++) {
            JSONObject obj = null;
            try {
                obj = reader.getJSONObject(user);
            } catch (JSONException e){
                e.printStackTrace();
            }
            String name = null;
            double lat = 0;
            double lon = 0;
            try {
                name = obj.getString("username");
                lat = obj.getDouble("latitude");
                lon = obj.getDouble("longitude");
            } catch (Exception e){
                e.printStackTrace();
            }
            tempUsers.add(new User(name, lat, lon));
        }
        return tempUsers;
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
