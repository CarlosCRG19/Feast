package com.example.fbu_app.fragments.DetailsFragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.fbu_app.R;
import com.example.fbu_app.activities.MainActivity;
import com.example.fbu_app.adapters.HoursAdapter;
import com.example.fbu_app.fragments.ConfirmationFragment;
import com.example.fbu_app.helpers.YelpClient;
import com.example.fbu_app.models.Business;
import com.example.fbu_app.models.Hour;
import com.example.fbu_app.models.Visit;
import com.example.fbu_app.models.VisitViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import okhttp3.Headers;

public class DetailsFragmentBase extends Fragment {

    public static final String VISIT_TAG = "visit"; // Simple tag to save objects in Parse

    protected Business business;

    // Views
    ImageView ivBusinessImage;
    TextView tvName, tvRating, tvPrice, tvTelephone;

    HoursAdapter adapter;
    List<Hour> hours;
    RecyclerView rvHours;

    YelpClient yelpClient;

    public DetailsFragmentBase(){}

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
       return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        yelpClient = new YelpClient();

        business = getArguments().getParcelable("business");

        ivBusinessImage = view.findViewById(R.id.ivBusinessImage);
        tvName = view.findViewById(R.id.tvName);
        tvRating = view.findViewById(R.id.tvRating);
        tvPrice =view.findViewById(R.id.tvPrice);
        tvTelephone = view.findViewById(R.id.tvTelephone);

        // Set TVs with info from the business
        tvName.setText(business.getName());
        tvPrice.setText("Price: " + business.getPrice());
        tvRating.setText("Rating: " + business.getRating() + "/5");
        tvTelephone.setText("Telephone: " + business.getTelephone());

        Glide.with(getContext())
                .load(business.getImageUrl())
                .centerCrop()
                .into(ivBusinessImage);

        hours = new ArrayList<>();
        adapter = new HoursAdapter(getContext(), hours);

        rvHours = view.findViewById(R.id.rvHours);
        rvHours.setAdapter(adapter);
        rvHours.setLayoutManager(new LinearLayoutManager(getContext()));

        yelpClient.getBusinessDetails(business.getYelpId(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                try {

                    JSONObject hoursJSON = json.jsonObject.getJSONArray("hours").getJSONObject(0);
                    hours = Hour.fromJsonArray(hoursJSON.getJSONArray("open"), business);
                    adapter.addAll(hours);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.i("DetailsFragmentBase", "Failure getting details: " + response, throwable);
            }
        });

    }

    // Checks if local business is already in database, if it is, the value of business is changed to match that one
    // after that, it creates a new visit
    protected void verifyBusinessExistsAndCreateVisit(Date visitDate, String visitDateStr) {
        // Specify type of query
        ParseQuery<Business> query = ParseQuery.getQuery(Business.class);
        // Search for business in database based on yelpId
        query.whereEqualTo("yelpId", business.getYelpId());
        // Use getFirstInBackground to finish the search if it has found one matching business
        query.getFirstInBackground(new GetCallback<Business>() {
            @Override
            public void done(Business object, ParseException e) {
                if(e != null) {
                    Log.i("ParseSave", "Search for user", e);
                    return;
                }
                // if the business exists, change value of member variable and create the new visit
                if (object != null) {
                    business = object;
                }
                createVisit(visitDate, visitDateStr);
            }
        });

    }

    // Creates a visit with the specified business and visit date
    protected void createVisit(Date visitDate, String visitDateStr) {
        // Create new visit
        Visit newVisit = new Visit();
        newVisit.setBusiness(business);
        // Add fields
        newVisit.setUser(ParseUser.getCurrentUser());
        newVisit.addAttendee(ParseUser.getCurrentUser());
        newVisit.setDate(visitDate);
        newVisit.setDateStr(visitDateStr);
        // Save visit using background thread
        newVisit.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null) {
                    Log.i("ParseSave", "Failed to save visit", e);
                    return;
                }
                // Display success message
                Toast.makeText(getContext(), "Succesfully created visit!", Toast.LENGTH_SHORT).show();
                // Create bundle to pass busines as argument
                Bundle bundle = new Bundle();
                bundle.putParcelable(VISIT_TAG, newVisit);
                // Transaction to new fragment
                ConfirmationFragment confirmationFragment = new ConfirmationFragment();
                confirmationFragment.setArguments(bundle);
                // Make fragment transaction
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.flContainer, confirmationFragment)
                        .commit();

            }
        });
    }

}



