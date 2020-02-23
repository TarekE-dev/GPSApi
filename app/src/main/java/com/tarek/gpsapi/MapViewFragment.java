package com.tarek.gpsapi;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link mapViewInterface} interface
 * to handle interaction events.
 * Use the {@link MapViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapViewFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String SELF_PARAM = "SELF_PARAM";
    private static final String USERS_PARAM = "USERS_PARAM";
    private User privateUser;
    private ArrayList<User> users;

    private Marker privateMarker;
    private Map<User, Marker> userMarkerMap;

    MapView mapView;
    GoogleMap map;

    private mapViewInterface mainActivity;

    public MapViewFragment() {}


    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param selfUser the user of the application
     * @param allUsers all the users from the API
     * @return A new instance of fragment MapViewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MapViewFragment newInstance(User selfUser, ArrayList<User> allUsers) {
        MapViewFragment fragment = new MapViewFragment();
        Bundle args = new Bundle();
        args.putParcelable(SELF_PARAM, selfUser);
        args.putParcelableArrayList(USERS_PARAM, allUsers);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            privateUser = getArguments().getParcelable(SELF_PARAM);
            users = getArguments().getParcelableArrayList(USERS_PARAM);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_view, container, false);
        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        userMarkerMap = new HashMap<>();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                map.setMyLocationEnabled(true);
                if(privateUser == null) return;
                LatLng latLng = new LatLng(privateUser.getLatitude(), privateUser.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
                map.animateCamera(cameraUpdate);
                privateMarker = map.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                if(users != null) {
                    for(User user:users) {
                        LatLng latLng2 = new LatLng(user.getLatitude(), user.getLongitude());
                        Marker newMarker = map.addMarker(new MarkerOptions().position(latLng2));
                        userMarkerMap.put(user, newMarker);
                    }
                }
            }
        });
        return view;
    }

    public void onUsersUpdated(ArrayList<User> users) {
        this.users = users;
        if(users == null) return;
        if(map == null) return;
        if(userMarkerMap == null) userMarkerMap = new HashMap<>();
        for(User user:this.users) {
            LatLng latLng = new LatLng(user.getLatitude(), user.getLongitude());
            if(userMarkerMap.containsKey(user)){
                userMarkerMap.get(user).setPosition(latLng);
            } else {
                Marker newMarker = map.addMarker(new MarkerOptions().position(latLng));
                userMarkerMap.put(user, newMarker);
            }
        }
        for(User user:userMarkerMap.keySet()){
            if(!this.users.contains(user)){
                userMarkerMap.get(user).remove();
            }
        }
    }

    public void onUserUpdated(User selfUpdated) {
        this.privateUser = selfUpdated;
        if(map == null) return;
        LatLng latLng = new LatLng(privateUser.getLatitude(), privateUser.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
        map.animateCamera(cameraUpdate);
        if(privateMarker == null) {
            privateMarker = map.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        } else {
            privateMarker.setPosition(latLng);
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mainActivity != null) {
            mainActivity.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof mapViewInterface) {
            mainActivity = (mapViewInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement recyclerFragmentInterface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivity = null;

    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface mapViewInterface {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
