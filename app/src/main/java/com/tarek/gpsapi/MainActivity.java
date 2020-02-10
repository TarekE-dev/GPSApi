package com.tarek.gpsapi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<User> users;

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
    }

    private void callGetUsersAPI(final String apiURL){
        new Thread() {
            @Override
            public void run(){

            }
        }.start();
    }

    private ArrayList<User> getUsers(String jsonResponse) {
        ArrayList<User> users = new ArrayList<User>();

        return users;
    }
}
