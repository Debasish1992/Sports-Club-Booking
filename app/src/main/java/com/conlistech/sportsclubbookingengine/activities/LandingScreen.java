package com.conlistech.sportsclubbookingengine.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.adapters.ItemAdapter;
import com.conlistech.sportsclubbookingengine.adapters.VenueAdapter;
import com.conlistech.sportsclubbookingengine.models.UserModel;
import com.conlistech.sportsclubbookingengine.models.VenueInfoModel;
import com.conlistech.sportsclubbookingengine.models.VenueModel;
import com.conlistech.sportsclubbookingengine.utils.Constants;
import com.conlistech.sportsclubbookingengine.utils.GetAddress;
import com.conlistech.sportsclubbookingengine.utils.LoaderUtils;
import com.conlistech.sportsclubbookingengine.utils.LocationTracker;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LandingScreen extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, VenueAdapter.ItemClickListener {

    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    Toolbar toolbar;

    @BindView(R.id.venues_recycler_view)
    RecyclerView venueRecyclerView;
    TextView tvFullName;
    TextView tvEmail;
    View header;
    SharedPreferences pref;
    ArrayList<VenueInfoModel> venueInfoModels;
    VenueAdapter venueAdapter;
    String venueId = null;

    @OnClick(R.id.toolbar)
    void getLocation() {
        setUpLocationPicker();
    }

    // Initializing the views
    private void initViews() {
        venueRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        venueRecyclerView.setLayoutManager(layoutManager);
    }

    // Initializing the navigation bar views
    public void initializeNabViews() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        header = navigationView.getHeaderView(0);
        tvFullName = (TextView) header.findViewById(R.id.tvFullname);
        tvEmail = (TextView) header.findViewById(R.id.tvEmail);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_screen);
        ButterKnife.bind(this);
        initializeNabViews();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initViews();

        pref = getSharedPreferences("MyPref", MODE_PRIVATE);

        // Setting User Data
        setUserData();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        LocationTracker locationTracker = new LocationTracker(LandingScreen.this);

        if (locationTracker.canGetLocation()) {
            Double latitude = locationTracker.getLatitude();
            Double longitude = locationTracker.getLongitude();
            List<Address> address = GetAddress.getAddress(LandingScreen.this, latitude, longitude);
            String locAddress = address.get(0).getAddressLine(0);
            String city = address.get(0).getLocality();
            Log.d("Address", address.toString());
            toolbar.setTitle(locAddress);
            toolbar.setSubtitle(city);
        } else {
            System.out.println("Unable to locate the position");
        }

        // Getting all the venues
        getAllVenues();
    }


    // Fetching and Setting User Data
    public void setUserData() {
        tvFullName.setText(pref.getString(Constants.USER_FULL_NAME, null));
        tvEmail.setText(pref.getString(Constants.USER_EMAIL, null));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.landing_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Toast.makeText(this, "in Home", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_noti) {

        } else if (id == R.id.nav_upcoming_games) {

        } else if (id == R.id.nav_profile) {
            TeammatesScreen.userId = pref.getString(Constants.USER_ID, null);
            Intent intent = new Intent(LandingScreen.this, ProfileScreen.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(LandingScreen.this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_teammates) {
            Intent intent = new Intent(LandingScreen.this, TeammatesScreen.class);
            startActivity(intent);
        } else if (id == R.id.nav_payments) {
            Intent intent = new Intent(LandingScreen.this, PaymentCardsScreen.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {
            shareApplication();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
            //Toast.makeText(this, ex.getMessage().toString(), Toast.LENGTH_SHORT).show();
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
                String placeAddress = place.getAddress().toString();
                toolbar.setTitle(placeName);
                toolbar.setSubtitle(placeAddress);
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

    public void shareApplication() {
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "Conlis Sports Android App");
            String sAux = "\n This application is awesome to schedule and play pick up games with your friends. Let me recommend you this application\n\n";
            sAux = sAux + "https://play.google.com/store/apps/details?id=com.conlistech.qrattendanceuser \n\n";
            i.putExtra(Intent.EXTRA_TEXT, sAux);
            startActivity(Intent.createChooser(i, "Choose One"));
        } catch (Exception e) {
            //e.toString();
            e.printStackTrace();
        }
    }


    // Getting all the venues
    public void getAllVenues() {
        LoaderUtils.showProgressBar(LandingScreen.this,
                "Please wait while fetching the venues..");
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("venues");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                venueInfoModels = new ArrayList<>();
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    VenueInfoModel venues = noteDataSnapshot.getValue(VenueInfoModel.class);
                    venues.setVenueId(noteDataSnapshot.getKey());
                    venueInfoModels.add(venues);
                }
                setUpAdapter();
                LoaderUtils.dismissProgress();

            }

            @Override
            public void onCancelled(DatabaseError error) {
                LoaderUtils.dismissProgress();
                // Failed to read value
            }
        });
    }


    // Setting up the adapter
    public void setUpAdapter() {
        if (venueInfoModels.size() > 0) {
            venueAdapter = new VenueAdapter(LandingScreen.this, venueInfoModels);
            venueRecyclerView.setAdapter(venueAdapter);
            venueAdapter.setClickListener(this);
        } else {
            Toast.makeText(LandingScreen.this, "No Teammates Found", Toast.LENGTH_LONG).show();
            venueAdapter = new VenueAdapter(LandingScreen.this, venueInfoModels);
            venueRecyclerView.setAdapter(venueAdapter);
            venueAdapter.setClickListener(this);
        }
    }


    @Override
    public void onClick(View view, int position) {
        /*final VenueInfoModel venues = VenueAdapter.mArrayList.get(position);
        //venueId = venues.get
        Intent i = new Intent(this, ProfileScreen.class);
        startActivity(i);*/
    }
}