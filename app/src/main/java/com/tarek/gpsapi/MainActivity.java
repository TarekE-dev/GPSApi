package com.tarek.gpsapi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<User> users;
    private MapViewFragment mapViewFragment;
    private RecyclerViewFragment recyclerViewFragment;

    private Handler apiHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            String jsonResponse = (String) msg.obj;
            users = getUsers(jsonResponse);

            //setUp();
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        callGetUsersAPI(getResources().getString(R.string.getUsersAPI));


    }

    private void setUp(){
        for(User user : users) {
            System.out.println(user.toString());
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


}
