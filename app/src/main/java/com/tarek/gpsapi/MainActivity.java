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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements MapViewFragment.mapViewInterface, RecyclerViewFragment.recyclerFragmentInterface {

    private final String SAVED_USERS = "SAVED_USERS";
    private final String SELF = "SELF";
    private final String MAP_FRAGMENT = "MAP_FRAGMENT";
    private final String LIST_FRAGMENT = "LIST_FRAGMENT";

    private Button nameButton;
    private EditText nameEdit;

    private User self;
    private ArrayList<User> users;

    private FragmentManager fm;
    private MapViewFragment mapViewFragment;
    private RecyclerViewFragment recyclerViewFragment;
    private boolean initialized = false;

    private LocationManager lm;
    private LocationListener ll;
    private Location currentLocation;

    private Gson gson = new Gson();

    private Handler apiHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            String jsonResponse = (String) msg.obj;
            users = getUsers(jsonResponse);
            Collections.sort(users);
            if(!initialized) {
                initMapFragment();
                initRecyclerViewFragment();
                initialized = true;
            }
            onUserUpdated();
            onUsersUpdated();
            return false;
        }
    });

    private Handler apiCaller = new Handler();
    private int delay = 30 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        apiCaller.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("apiCaller", "Calling GET Request");
                callGetUsersAPI(getResources().getString(R.string.getUsersAPI));
                apiCaller.postDelayed(this, delay);
            }
        }, delay);
        if (checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            lm = getSystemService(LocationManager.class);
        }
        fm = getSupportFragmentManager();
        initListener();
        setButtonFunction();
        self = loadUser();
        if(self != null) setTitle("Welcome, " + self.getName());
        callGetUsersAPI(getResources().getString(R.string.getUsersAPI));
        mapViewFragment = (MapViewFragment) fm.findFragmentByTag(MAP_FRAGMENT);
        recyclerViewFragment = (RecyclerViewFragment) fm.findFragmentByTag(LIST_FRAGMENT);
    }

    private void initMapFragment(){
        if(mapViewFragment == null) {
            mapViewFragment = MapViewFragment.newInstance(self, users);
            fm.beginTransaction().add(R.id.mapFragmentContainer, mapViewFragment, MAP_FRAGMENT).commit();
        } else {
            fm.beginTransaction().remove(mapViewFragment).commit();
            mapViewFragment = MapViewFragment.newInstance(self, users);
            fm.beginTransaction().add(R.id.mapFragmentContainer, mapViewFragment, MAP_FRAGMENT).commit();
        }
    }

    private void initRecyclerViewFragment() {
        if(recyclerViewFragment == null) {
            recyclerViewFragment = RecyclerViewFragment.newInstance(self, users);
            fm.beginTransaction().add(R.id.listFragmentContainer, recyclerViewFragment, LIST_FRAGMENT).commit();
        } else {
            fm.beginTransaction().remove(recyclerViewFragment).commit();
            recyclerViewFragment = RecyclerViewFragment.newInstance(self, users);
            fm.beginTransaction().add(R.id.listFragmentContainer, recyclerViewFragment, LIST_FRAGMENT).commit();
        }
    }

    private void setButtonFunction() {
        nameButton = findViewById(R.id.nameButton);
        nameEdit = findViewById(R.id.nameEdit);
        nameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEdit.getText().toString();
                self = new User(name, currentLocation.getLatitude(), currentLocation.getLongitude(), currentLocation);
                setTitle("Welcome, " + self.getName());
                saveUser();
                addUserAPI(getResources().getString(R.string.postUserAPI));
            }
        });
    }

    private void onUsersUpdated() {
        if(users == null) return;
        for(User user:users) {
            user.setSelfLocation(currentLocation);
        }
        System.out.println("User: " + self.toString());

        Collections.sort(users);
        if(recyclerViewFragment != null) {
            recyclerViewFragment.onUsersUpdated(users);
        }
        if(mapViewFragment != null) {
            mapViewFragment.onUsersUpdated(users);
        }
    }

    private void initListener() {
        ll = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("LOCATION", "Location changed");
                currentLocation = location;
                if(self != null){
                    self.updateLocation(currentLocation);
                    saveUser();
                    addUserAPI(getResources().getString(R.string.postUserAPI));
                }
                if(initialized) {
                    onUserUpdated();
                    onUsersUpdated();
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
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, ll);
    }

    private void onUserUpdated() {
        mapViewFragment.onUserUpdated(self);
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
        if(self == null) return;
        SharedPreferences sp = getSharedPreferences(SAVED_USERS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(SELF, userToJson(self));
        editor.commit();
    }

    private String userToJson(User user){
        return gson.toJson(user);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            System.exit(-1);
        }
    }

    private void callGetUsersAPI(final String apiURL){
        new Thread(() -> {
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
        }).start();
    }

    private void addUserAPI(String apiURL) {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest postRequest = new StringRequest(Request.Method.POST, apiURL, response ->
            {Log.d("API Response:", response); callGetUsersAPI(getResources().getString(R.string.getUsersAPI));},
                error -> Log.d("API Error:", error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user", self.getName());
                params.put("latitude", String.valueOf(self.getLatitude()));
                params.put("longitude", String.valueOf(self.getLongitude()));
                return params;
            }
        };
        requestQueue.add(postRequest);
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
                if(self.getName().equals(name)) continue;
                lat = obj.getDouble("latitude");
                lon = obj.getDouble("longitude");
            } catch (Exception e){
                e.printStackTrace();
            }
            tempUsers.add(new User(name, lat, lon, currentLocation));
        }
        return tempUsers;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        lm.removeUpdates(ll);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
