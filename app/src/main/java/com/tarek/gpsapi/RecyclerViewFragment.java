package com.tarek.gpsapi;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link recyclerFragmentInterface} interface
 * to handle interaction events.
 * Use the {@link RecyclerViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecyclerViewFragment extends Fragment {

    private static final String SELF_PARAM = "SELF_PARAM";
    private static final String USERS_PARAM = "USERS_PARAM";

    private User privateUser;
    private ArrayList<User> users;

    RecyclerView recyclerView;
    RecyclerViewAdapter rva;

    private recyclerFragmentInterface mainActivity;

    public RecyclerViewFragment() {}

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param selfUser the user of the application
     * @param allUsers all the users from the API
     * @return A new instance of fragment RecyclerViewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RecyclerViewFragment newInstance(User selfUser, ArrayList<User> allUsers) {
        RecyclerViewFragment fragment = new RecyclerViewFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        if(users != null) {
            recyclerView = view.findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            rva = new RecyclerViewAdapter(users);
            recyclerView.setAdapter(rva);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
        } else {
            Log.d("USERS", "users are empty");
        }
        return view;
    }

    public void onUsersUpdated(ArrayList<User> users){
        this.users = users;
        Collections.sort(this.users);
        if(rva != null)
            rva.updateUsers(this.users);
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
        if (context instanceof recyclerFragmentInterface) {
            mainActivity = (recyclerFragmentInterface) context;
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
    public interface recyclerFragmentInterface {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
