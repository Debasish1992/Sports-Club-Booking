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
import android.text.TextUtils;
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
import com.conlistech.sportsclubbookingengine.models.GameModel;
import com.conlistech.sportsclubbookingengine.models.GamePlayersModel;
import com.conlistech.sportsclubbookingengine.models.UserModel;
import com.conlistech.sportsclubbookingengine.utils.CommonUtils;
import com.conlistech.sportsclubbookingengine.utils.Constants;
import com.conlistech.sportsclubbookingengine.utils.LoaderUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
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
    int totalPlayerCount = 0;
    String venuePhoneNumber, venueWebsite, venueAddress;
    final String GOOGLE_NAV_PARAM = "google.navigation:q=";
    ArrayList<String> gameTeamates = new ArrayList<>();
    GameModel gameModel;


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

    @OnClick(R.id.btnJoinGame)
    void joinGame() {
        doGameJoin();
    }

    // Adding the new player to the model array
    public GamePlayersModel addNewPlayer() {
        GamePlayersModel gamePlayersModel = new GamePlayersModel();
        gamePlayersModel.setUserRole("Player");
        gamePlayersModel.setUserId(getCurrentUserId());
        gamePlayersModel.setUserName(getCurrentUserName());
        return gamePlayersModel;
    }

    // Building game players array
    public ArrayList<GamePlayersModel> buildGamePlayers() {
        ArrayList<GamePlayersModel> gamePlayers = UpcomingGamesScreen.gameModel.getGamePlayers();
        GamePlayersModel gamePlayersModel = new GamePlayersModel();
        gamePlayersModel.setUserRole("Player");
        gamePlayersModel.setUserId(getCurrentUserId());
        gamePlayersModel.setUserName(getCurrentUserName());
        gamePlayers.add(gamePlayersModel);
        return gamePlayers;
    }

    // Building the pending game players array
    public ArrayList<GamePlayersModel> buildPendingPlayers() {
        ArrayList<GamePlayersModel> pendingPlayers =
                UpcomingGamesScreen.gameModel.getPendingGameInvitations();

        for (int i = 0; i < pendingPlayers.size(); i++) {
            String getUserId = pendingPlayers.get(i).getUserId();

            if (getCurrentUserId().equalsIgnoreCase(getUserId)) {
                pendingPlayers.remove(i);
                break;
            }
        }
        return pendingPlayers;
    }


    @OnClick(R.id.tvTeamMates)
    void showTeammates() {
        for (int i = 0; i < getTotalGamePlayers.size(); i++) {
            gameTeamates.add(getTotalGamePlayers.get(i).getUserName() + " | " + getTotalGamePlayers.get(i).getUserRole());
        }
        // Displaying Accepted Teammates
        setUpTeammatesDialog(gameTeamates);
    }

    /**
     * Setting up Teammates list
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


    // Function responsible for making a user join the game
    public void doGameJoin() {
        DatabaseReference mDatabase = FirebaseDatabase
                .getInstance()
                .getReference("game_invites");

        mDatabase.child(getCurrentUserId())
                .child(UpcomingGamesScreen.gameModel.getGameId())
                .removeValue();

        // Pushing the Game Players Data
        DatabaseReference mDatabase_games = FirebaseDatabase.getInstance().getReference("games");
        mDatabase_games
                .child(UpcomingGamesScreen.gameModel.getGameId())
                .child("gamePlayers")
                .setValue(buildGamePlayers());

        mDatabase_games
                .child(UpcomingGamesScreen.gameModel.getGameId())
                .child("gameInvitations")
                .setValue(buildPendingPlayers());

        UpcomingGamesScreen.isInvitationAccepted = true;

        GameInvitations.gameInvitationsCount = GameInvitations.gameInvitationsCount - 1;

        Toast.makeText(this, "You have successfully joined the game.", Toast.LENGTH_SHORT).show();
        // Making the join button GONE
        refreshScrollViewLayout();
        // Adding the Player data into the model array
        gameTeamates.add(getCurrentUserName() + "|" + Constants.GAME_ROLE_PLAYER);
        // Refreshing the Player Count
        refreshGameTotalPlayerCount(totalPlayerCount + 1);
        // Adding new Player to the Model Array
        setGamePlayerIntoArray();
    }

    // Function responsible for adding the new player into the array
    public void setGamePlayerIntoArray() {
        ArrayList<GamePlayersModel> gamePlayersModels =
                UpcomingGamesScreen.gameModel.getGamePlayers();
        gamePlayersModels.add(addNewPlayer());
        UpcomingGamesScreen.gameModel.setGamePlayers(gamePlayersModels);
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
        CommonUtils.changeToolbarFont(toolbar, this);


        // Checking the condition for refreshing the UI
        if (!TextUtils.isEmpty(Constants.pushNotificationGameId)) {
            //Fetching the Game Details
            fetchGameDetails();
        } else {
            // Refreshing the UI
            refreshUi();
            // initializing the Map
            initMap();
        }
    }

    public void initMap() {
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
        Constants.pushNotificationGameId = null;
        UpcomingGamesScreen.gameModel = null;
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
            totalPlayerCount = getTotalGamePlayers.size();
            venueAddress = UpcomingGamesScreen.gameModel.getVenueInfoModel().getLocationModel().getAddress();

            venueWebsite = UpcomingGamesScreen.gameModel.getVenueInfoModel().getVenue_website();

            // Refreshing Game total game player count
            refreshGameTotalPlayerCount(totalPlayerCount);

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

    public void refreshGameTotalPlayerCount(int playerCount) {
        if (playerCount == 1) {
            gamePlayers.setText(String.valueOf(playerCount) + " player is playing this game");
        } else {
            gamePlayers.setText(String.valueOf(playerCount) + " players are playing this game");
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
    public void checkUserExistanceInTheGame() {
        ArrayList<String> gameTeammates = new ArrayList<>();
        String gameCreatorUserId = UpcomingGamesScreen.gameModel.getGameCreatorUserId();
        gameTeammates.add(gameCreatorUserId);
        ArrayList<GamePlayersModel> acceptedTeamates = UpcomingGamesScreen.gameModel.getGamePlayers();
        for (int i = 0; i < acceptedTeamates.size(); i++) {
            String userId = acceptedTeamates.get(i).getUserId();
            gameTeammates.add(userId);
        }

        if (gameTeammates.contains(getCurrentUserId())) {
            refreshScrollViewLayout();
        }
    }

    // Refreshing the Scroll View Layout
    public void refreshScrollViewLayout() {
        btnJoinGame.setVisibility(View.GONE);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) scMain
                .getLayoutParams();
        layoutParams.setMargins(0, 0, 0, 0);
        scMain.setLayoutParams(layoutParams);
    }


    // Getting current User id
    public String getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("MyPref", MODE_PRIVATE);
        return prefs.getString(Constants.USER_ID, null);
    }

    // Getting current User id
    public String getCurrentUserName() {
        SharedPreferences prefs = getSharedPreferences("MyPref", MODE_PRIVATE);
        return prefs.getString(Constants.USER_FULL_NAME, null);
    }

    /**
     * Function Responsible for getting Game Details
     */
    public void fetchGameDetails() {
        LoaderUtils.showProgressBar(GameDetails.this, "Please wait while loading...");
        gameModel = new GameModel();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("games").child(Constants.pushNotificationGameId);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                UpcomingGamesScreen.gameModel = dataSnapshot.getValue(GameModel.class);
                // Refreshing the UI
                refreshUi();

                // Initializing the Google Map
                initMap();

                // Stopping the loader
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
