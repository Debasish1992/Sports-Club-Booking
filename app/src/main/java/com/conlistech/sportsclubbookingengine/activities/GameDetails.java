package com.conlistech.sportsclubbookingengine.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import com.afollestad.materialdialogs.MaterialDialog;
import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.models.GamePlayersModel;
import com.conlistech.sportsclubbookingengine.utils.Constants;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class GameDetails extends AppCompatActivity implements OnMapReadyCallback {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.ivVenueImage)
    ImageView ivVenueImage;
    @BindView(R.id.tvGameName)
    TextView tvGameName;
    @BindView(R.id.tvGamePrice)
    TextView tvGamePrice;
    @BindView(R.id.tvAddress)
    TextView tvVenueAddress;
    @BindView(R.id.tvEventDate)
    TextView tvGameDate;
    @BindView(R.id.tvTeamMates)
    TextView gamePlayers;
    @BindView(R.id.layWebsite)
    LinearLayout layWebsite;
    @BindView(R.id.layCall)
    LinearLayout layCall;
    @BindView(R.id.layDirections)
    LinearLayout layDirection;
    @BindView(R.id.btnJoinGame)
    Button btnJoinGame;
    @BindView(R.id.svMain)
    ScrollView scMain;
    @BindView(R.id.tvGameLay)
    RelativeLayout layGameMain;
    double venueLatitude, venueLongitude;
    ArrayList<GamePlayersModel> getTotalGamePlayers;
    String venuePhoneNumber, venueWebsite, venueAddress;
    final String GOOGLE_NAV_PARAM = "google.navigation:q=";


    @OnClick(R.id.layWebsite)
    void redirectToWebsite() {
        loadURLInBrowser(venueWebsite);
    }

    @OnClick(R.id.layCall)
    void callVenue() {
        // Making the phone call
        if (isPermissionGranted()) {
            call_action();
        }

    }

    @OnClick(R.id.layDirections)
    void showNavigations() {
        navigateUserToGoogleMap();
    }

    @OnClick(R.id.tvTeamMates)
    void showTeammates() {
        ArrayList<String> gameTeamates = new ArrayList<>();
        for (int i = 0; i < getTotalGamePlayers.size(); i++) {
            gameTeamates.add(getTotalGamePlayers.get(i).getUserName() + " | " + getTotalGamePlayers.get(i).getUserRole());
        }
        // Displaying Accepted Teammates
        setUpTeammatesDialog(gameTeamates);
    }

    /**
     * Setting up Teamates list
     *
     * @param gameTeamates Teamates List
     */
    public void setUpTeammatesDialog(ArrayList<String> gameTeamates) {
        new MaterialDialog.Builder(GameDetails.this)
                .title("Players List")
                .items(gameTeamates)
                .cancelable(true)
                .canceledOnTouchOutside(true)
                .negativeText("OK")
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_details);
        ButterKnife.bind(this);

        // Setting up Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Refreshing the UI
        refreshUi();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        GameDetails.this.finish();
    }


    // Refreshing the Ui
    public void refreshUi() {
        if (UpcomingGamesScreen.gameModel != null) {
            venueLatitude = Double.parseDouble(UpcomingGamesScreen.gameModel.getVenueInfoModel().getLocationModel().getLatitude());
            venueLongitude = Double.parseDouble(UpcomingGamesScreen.gameModel.getVenueInfoModel().getLocationModel().getLongitude());
            tvGameName.setText(UpcomingGamesScreen.gameModel.getGameName());
            toolbar.setTitle(UpcomingGamesScreen.gameModel.getGameName());
            toolbar.setSubtitle(UpcomingGamesScreen.gameModel.getGameSport());
            tvGamePrice.setText("$" + UpcomingGamesScreen.gameModel.getVenueInfoModel().getPrice() + " | " +
                    UpcomingGamesScreen.gameModel.getGameSport());
            String gameTimeSlot = UpcomingGamesScreen.gameModel.getTimeSlot();
            String[] timeSlots = gameTimeSlot.split(",");

            if (timeSlots.length == 2) {
                tvGameDate.setText(UpcomingGamesScreen.gameModel.getGameCreatorUserName() + " has scheduled the game for " +
                        GameInfoScreen.getDate(Long.parseLong(UpcomingGamesScreen.gameModel.getGameDate())) + " | " + timeSlots[1]);
            } else {
                List<String> timeSlotList = new ArrayList<String>(Arrays.asList(timeSlots));
                timeSlotList.remove(0);
                String timeSlotsStr = null;
                int lastPosition = timeSlotList.size() - 1;
                for (int i = 0; i < timeSlotList.size(); i++) {
                    if (i == 0) {
                        timeSlotsStr = timeSlotList.get(i);
                    } else if (i == lastPosition) {
                        timeSlotsStr = timeSlotsStr + " & " + timeSlotList.get(i);
                    } else {
                        timeSlotsStr = timeSlotsStr + "," + timeSlotList.get(i);
                    }
                }

                tvGameDate.setText(UpcomingGamesScreen.gameModel.getGameCreatorUserName() + " has scheduled the game for " +
                        GameInfoScreen.getDate(Long.parseLong(UpcomingGamesScreen.gameModel.getGameDate())) + " | " + timeSlotsStr);
            }

            tvVenueAddress.setText(UpcomingGamesScreen.gameModel.getVenueInfoModel().getLocationModel().getAddress());
            getTotalGamePlayers = UpcomingGamesScreen.gameModel.getGamePlayers();
            venueAddress = UpcomingGamesScreen.gameModel.getVenueInfoModel().getLocationModel().getAddress();

            venueWebsite = UpcomingGamesScreen.gameModel.getVenueInfoModel().getVenue_website();

            if (getTotalGamePlayers.size() == 1) {
                gamePlayers.setText(String.valueOf(getTotalGamePlayers.size()) + " player is playing this game");
            } else {
                gamePlayers.setText(String.valueOf(getTotalGamePlayers.size()) + " players are playing this game");
            }

            venuePhoneNumber = UpcomingGamesScreen.gameModel.getVenueInfoModel().getVenue_phone();

            // Getting the venue image
            String getVenueImage = UpcomingGamesScreen.gameModel.getVenueInfoModel().getVenue_image();

            // Setting the venue image
            if (getVenueImage != null) {
                Picasso.get()
                        .load(getVenueImage)
                        .placeholder(R.drawable.default_loading)
                        .into(ivVenueImage);
            }

            checkUserExistanceInTheGame();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        float zoomLevel = 18.0f;
        LatLng venueLocation = new LatLng(venueLatitude, venueLongitude);
        googleMap.addMarker(new MarkerOptions()
                .position(venueLocation)
                .flat(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .title(UpcomingGamesScreen.gameModel.getVenueInfoModel().getAddress()));
        googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        googleMap.setTrafficEnabled(true);
        googleMap.setIndoorEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(venueLocation, zoomLevel));
    }

    // Checking for the permission
    public boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.CALL_PHONE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("TAG", "Permission is granted");
                return true;
            } else {

                Log.v("TAG", "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("TAG", "Permission is granted");
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {

            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    call_action();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }


    // Function responsible for making the phone call
    public void call_action() {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + venuePhoneNumber));
        startActivity(callIntent);
    }

    // Function responsible for navigating the user to google map
    public void navigateUserToGoogleMap() {
        Uri gmmIntentUri = Uri.parse(GOOGLE_NAV_PARAM + venueAddress);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    /**
     * Function responsible for loading the venue website
     *
     * @param url
     */
    public void loadURLInBrowser(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://"))
            url = "http://" + url;

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    // Checking for the game teammates
    public void checkUserExistanceInTheGame(){
        ArrayList<String> gameTeammates = new ArrayList<>();
        String gameCreatorUserId = UpcomingGamesScreen.gameModel.getGameCreatorUserId();
        gameTeammates.add(gameCreatorUserId);
        ArrayList<GamePlayersModel> acceptedTeamates = UpcomingGamesScreen.gameModel.getGamePlayers();
        for(int i = 0; i < acceptedTeamates.size(); i++){
            String userId = acceptedTeamates.get(i).getUserId();
            gameTeammates.add(userId);
        }

        if(gameTeammates.contains(getCurrentUserId())){
            btnJoinGame.setVisibility(View.GONE);

            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) scMain
                    .getLayoutParams();
            layoutParams.setMargins(0, 0, 0, 0);
            scMain.setLayoutParams(layoutParams);
        }
    }


    // Getting current User id
    public String getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("MyPref", MODE_PRIVATE);
        return prefs.getString(Constants.USER_ID, null);
    }
}
