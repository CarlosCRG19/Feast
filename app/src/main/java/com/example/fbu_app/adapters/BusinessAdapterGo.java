package com.example.fbu_app.adapters;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.fbu_app.R;
import com.example.fbu_app.activities.MainActivity;
import com.example.fbu_app.fragments.DetailsFragments.DetailsFragmentCreate;
import com.example.fbu_app.fragments.DetailsFragments.DetailsFragmentGo;
import com.example.fbu_app.fragments.NextVisitsFragment;
import com.example.fbu_app.models.Business;
import com.example.fbu_app.models.Visit;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;

// Adapter for businesses in the Compare Screen (includes a Go! button to instantly create a visit)
public class BusinessAdapterGo extends BusinessAdapter{

    // FIELDS
    private String visitDateStr; // Date values for visit creation
    private Date visitDate;

    // CONSTRUCTOR. User super class constructor but includes new fields for visit creation
    public BusinessAdapterGo(Context context, List<Business> businesses, String visitDateStr, Date visitDate) {
        super(context, businesses);
        this.visitDateStr = visitDateStr; // visit info required to create new visit
        this.visitDate = visitDate;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        // Change layout used by super class
        View view = LayoutInflater.from(context).inflate(R.layout.businesses_compare_layout, parent, false);
        return new ViewHolder(view);
    }

    // Override inner ViewHolder class
    public class ViewHolder extends BusinessAdapter.ViewHolder {

        // VIEWS
        private Button btnGo; // new view for visit creation


        public ViewHolder(@NonNull @NotNull View itemView) {
            // Use super class method and assign button value
            super(itemView);
            btnGo = itemView.findViewById(R.id.btnGo);

        }

        @Override
        public void bind(Business businessToBind) {
            // Call super class bind method
            super.bind(businessToBind);
            // Add listener to btnGo for visit creation
            btnGo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Create new visit
                    Visit newVisit = new Visit();
                    newVisit.setBusiness(business);
                    // Add fields
                    newVisit.setUser(ParseUser.getCurrentUser());
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
                            Toast.makeText(context, "Succesfully created visit!", Toast.LENGTH_SHORT).show();
                            // Transaction to new fragment
                            NextVisitsFragment nextVisitsFragment = new NextVisitsFragment();
                            ((MainActivity) context).getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.flContainer, nextVisitsFragment)
                                    .commit();
                            // Change selected item in bottom nav bar
                            BottomNavigationView bottomNavigationView = ((MainActivity) context).findViewById(R.id.bottomNavigation);
                            bottomNavigationView.setSelectedItemId(R.id.action_history);
                        }
                    });
                }
            });
        }

        // Overrides super class click listener to open a different details view
        @Override
        public void onClick(View v) {
            // Create bundle to pass busines as argument
            Bundle bundle = new Bundle();
            bundle.putParcelable(BUSINESS_TAG, business);

            // Create new detailsFragmentGo instance
            DetailsFragmentGo detailsFragmentGo = new DetailsFragmentGo();
            detailsFragmentGo.setArguments(bundle);

            // Make fragment transaction adding to back stack to return when back clicked
            ((MainActivity) context).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flContainer, detailsFragmentGo)
                    .addToBackStack(null)
                    .commit();
        }
    }
}