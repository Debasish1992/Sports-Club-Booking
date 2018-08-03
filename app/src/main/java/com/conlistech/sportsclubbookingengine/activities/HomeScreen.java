/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.conlistech.sportsclubbookingengine.activities;

import android.app.Notification;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.aurelhubert.ahbottomnavigation.notification.AHNotification;
import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.models.VenueModel;
import com.conlistech.sportsclubbookingengine.utils.CommonUtils;
import com.conlistech.sportsclubbookingengine.utils.LoaderUtils;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class HomeScreen extends AppCompatActivity {
    AHBottomNavigation bottomNavigation = null;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    @BindView(R.id.toolbar)
    Toolbar tbBar;
    ArrayList<VenueModel> venueModels;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen2);
        ButterKnife.bind(this);
        setSupportActionBar(tbBar);
        CommonUtils.changeToolbarFont(tbBar, this);
        setUpTabBar();

        fetchAllVenues();
    }

    public void setUpTabBar() {
        bottomNavigation =
                (AHBottomNavigation) findViewById(R.id.bottom_navigation);

        // Create items
        AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.title_home,
                R.drawable.ic_home_black_24dp,
                R.color.md_indigo_400);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.title_dashboard,
                R.drawable.ic_home_black_24dp,
                R.color.md_indigo_400);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.title_notifications,
                R.drawable.ic_home_black_24dp,
                R.color.md_indigo_400);

        AHBottomNavigationItem item4 = new AHBottomNavigationItem(R.string.title_profile,
                R.drawable.ic_home_black_24dp,
                R.color.md_indigo_400);

        // Add items
        bottomNavigation.addItem(item1);
        bottomNavigation.addItem(item2);
        bottomNavigation.addItem(item3);
        bottomNavigation.addItem(item4);

        // Set background color
        bottomNavigation.setDefaultBackgroundColor
                (Color.parseColor("#FFFFFF"));

        // Disable the translation inside the CoordinatorLayout
        bottomNavigation.setBehaviorTranslationEnabled(false);

        // Change colors
        bottomNavigation.setAccentColor(Color.parseColor("#F63D2B"));
        bottomNavigation.setInactiveColor(Color.parseColor("#747474"));

        // Force to tint the drawable (useful for font with icon for example)
        bottomNavigation.setForceTint(true);

        // Display color under navigation bar (API 21+)
        // Don't forget these lines in your style-v21
        // <item name="android:windowTranslucentNavigation">true</item>
        // <item name="android:fitsSystemWindows">true</item>
        bottomNavigation.setTranslucentNavigationEnabled(true);

        // Manage titles
        //bottomNavigation.setTitleState(AHBottomNavigation.TitleState.SHOW_WHEN_ACTIVE);
        //bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_HIDE);

        // Use colored navigation with circle reveal effect
        bottomNavigation.setColored(true);

        // Set current item programmatically
        bottomNavigation.setCurrentItem(0);

        // Customize notification (title, background, typeface)
        //bottomNavigation.setNotificationBackgroundColor(Color.parseColor("#F63D2B"));

        // Add or remove notification for each item
        bottomNavigation.setNotification("1", 3);
        bottomNavigation.setNotification("100", 1);

        /*// OR
        AHNotification notification = new AHNotification.Builder()
                .setText("1")
                .setBackgroundColor(ContextCompat.getColor(HomeScreen.this,
                        R.color.md_deep_purple_400))
                .setTextColor(ContextCompat.getColor(HomeScreen.this,
                        R.color.white))
                .build();
        bottomNavigation.setNotification(notification, 1);*/

        // Enable / disable item & set disable color
        bottomNavigation.enableItemAtPosition(2);
        //bottomNavigation.disableItemAtPosition(2);
        bottomNavigation.setItemDisableColor(Color.parseColor("#3A000000"));

        // Set listeners
        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                // Do something cool here...
                return true;
            }
        });
        bottomNavigation.setOnNavigationPositionListener(new AHBottomNavigation.OnNavigationPositionListener() {
            @Override
            public void onPositionChange(int y) {
                // Manage the new y position
            }
        });
    }

    public void setUpLocationPicker() {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder
                            (PlaceAutocomplete.MODE_FULLSCREEN)
                            .setFilter(setUpFilter())
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
            Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public AutocompleteFilter setUpFilter() {
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setCountry("IN")
                .build();
        return typeFilter;
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                String placeName = place.getName().toString();
                tbBar.setTitle(placeName);
                double placeLat = place.getLatLng().latitude;
                double placeLng = place.getLatLng().longitude;
                Log.i("Place is", "Place: " + place.getName());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i("Place is", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
                Toast.makeText(this, "User has not selected any location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void fetchAllVenues(){
        LoaderUtils.showProgressBar(HomeScreen.this, "Please wait while loading...");
        venueModels = new ArrayList<>();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("venues");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                VenueModel value = dataSnapshot.getValue(VenueModel.class);
                Log.d("HomeScreen", "Value is: " + value.toString());
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    VenueModel guitars = noteDataSnapshot.getValue(VenueModel.class);
                    venueModels.add(guitars);
                }
                LoaderUtils.dismissProgress();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                LoaderUtils.dismissProgress();
                // Failed to read value
                Log.w("HomeScreen", "Failed to read value.", error.toException());
            }
        });
    }


}
