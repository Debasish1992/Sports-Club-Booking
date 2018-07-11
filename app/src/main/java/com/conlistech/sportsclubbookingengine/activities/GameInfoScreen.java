package com.conlistech.sportsclubbookingengine.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.adapters.TimeSlotAdapter;
import com.conlistech.sportsclubbookingengine.database.SqliteHelper;
import com.conlistech.sportsclubbookingengine.models.GameModel;
import com.conlistech.sportsclubbookingengine.models.GamePlayersModel;
import com.conlistech.sportsclubbookingengine.models.PaymentCardModel;
import com.conlistech.sportsclubbookingengine.models.VenueInfoModel;
import com.conlistech.sportsclubbookingengine.utils.Constants;
import com.conlistech.sportsclubbookingengine.utils.LoaderUtils;
import com.conlistech.sportsclubbookingengine.utils.RandomNumberGenerator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GameInfoScreen extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.input_name)
    EditText etGameName;
    @BindView(R.id.input_playerCount)
    EditText etPlayerCount;
    @BindView(R.id.input_gameNote)
    EditText etGameNotes;
    @BindView(R.id.btnSubmit)
    Button btnCreateGame;
    String gameName, gamePlayerCount, gameNote;
    public static ArrayList<Integer> indicesArray = new ArrayList<>();
    public static String gameId = null;
    public static GameModel gameModel = null;
    public static GameInfoScreen gameInfoScreen;
    SqliteHelper sqliteHelper;
    public static String primaryCardNo = null;
    SharedPreferences prefs;


    @OnClick(R.id.btnSubmit)
    void createGame() {
        boolean isValid = validateUserEntries();
        if (isValid) {
            Constants.gameName = gameName;
            Constants.maximumNoPlayers = gamePlayerCount;
            Constants.gameNotes = gameNote;
            Log.d("Array", indicesArray.toString());

            if (sqliteHelper.getPaymentCardCount() > 0) {
                primaryCardNo = sqliteHelper.getPrimaryCardNo();
                buildGameData();
            } else {
                showNoCardFoundDialog();
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_info_screen);
        ButterKnife.bind(this);

        gameInfoScreen = this;

        sqliteHelper = new SqliteHelper(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    // Showing the dialog if no card found in the account
    public void showNoCardFoundDialog() {
        new MaterialDialog.Builder(this)
                .title("Payment Info")
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .content("We did not found any payment card information, associated with your account. Please add payment information in the payment section to proceed.")
                .positiveText("OK")
                .negativeText("Cancel")
                .show();
    }

    // Validating user inputs
    public boolean validateUserEntries() {
        boolean valid = false;
        gameName = etGameName.getText().toString().trim();
        gamePlayerCount = etPlayerCount.getText().toString().trim();
        gameNote = etGameNotes.getText().toString().trim();

        if (TextUtils.isEmpty(gameName)) {
            Toast.makeText(this, "Please enter name of your game.", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(gamePlayerCount)) {
            Toast.makeText(this, "Please enter the player count", Toast.LENGTH_SHORT).show();
        } else if (Integer.parseInt(gamePlayerCount) == 0) {
            Toast.makeText(this, "Please enter a valid player count", Toast.LENGTH_SHORT).show();
        } else {
            valid = true;
        }
        return valid;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ShareGameScreen.releaseAllValues();
        finish();
    }


    // Building Game Model
    public void buildGameData() {
        gameId = String.valueOf(RandomNumberGenerator.getRandomNumber());
        gameModel = new GameModel();
        gameModel.setGameName(gameName);
        gameModel.setGameTotalNoOfplayers(gamePlayerCount);
        gameModel.setGameTotalAmount(Constants.venueTotalBookingPrice);
        gameModel.setGameNote(gameNote);
        gameModel.setVenueId(LandingScreen.venueId);
        gameModel.setGameTotalAmount(Constants.venueTotalBookingPrice);
        gameModel.setTimeSlot(getGameBookingTimeSlots());
        gameModel.setGameDate(String.valueOf(convertDateToMillis(Constants.gameScheduledDate)));
        gameModel.setGameSport(Constants.gameSport);
        gameModel.setGameCreatorUserId(getCurrentUserId());
        gameModel.setGameCreatorUserName(getCurrentUserName());
        gameModel.setVenueInfoModel(LandingScreen.venueInfoModel);
        gameModel.setGameId(gameId);

        // Adding Game Players
        GamePlayersModel gamePlayersModel = new GamePlayersModel();
        gamePlayersModel.setUserId(getCurrentUserId());
        gamePlayersModel.setUserName(getCurrentUserName());
        gamePlayersModel.setUserRole(Constants.GAME_ROLE_CREATOR);
        ArrayList<GamePlayersModel> gamePlayersModelArray = new ArrayList<>();
        gamePlayersModelArray.add(gamePlayersModel);
        gameModel.setGamePlayers(gamePlayersModelArray);

        // Pushing the pending invitations
        /*GamePlayersModel pendingGameInvitationModel = new GamePlayersModel();
        pendingGameInvitationModel.setUserRole(null);
        pendingGameInvitationModel.setUserName(null);
        pendingGameInvitationModel.setUserId(null);
        ArrayList<GamePlayersModel> pendingGameInvitationArray = new ArrayList<>();
        pendingGameInvitationArray.add(pendingGameInvitationModel);
        gameModel.setPendingGameInvitations(pendingGameInvitationArray);*/


        // saving Game Data
        pushGameInfo(gameId, gameModel);
    }

    // Pushing Game details
    public void pushGameInfo(String gameId, GameModel gameModel) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("games");
        // pushing user to 'users' node using the userId
        mDatabase.child(gameId).setValue(gameModel);
        Toast.makeText(this, "Game Created Successfully.", Toast.LENGTH_SHORT).show();

        // Removing the booked time slots
        // Refreshing the time slot Availability
        updateTimeSlotBookings();
    }

    // Getting the time Slots
    public String getGameBookingTimeSlots() {
        String timeSlots = null;
        for (int i = 0; i < Constants.bookingTimeSlots.size(); i++) {
            timeSlots = timeSlots + ", " + Constants.bookingTimeSlots.get(i);
        }
        return timeSlots;
    }

    // Redirect user to invite screen
    public void redirectUserToInvites() {
        startActivity(new Intent(GameInfoScreen.this, GameInvitesScreen.class));
    }

    // Updating the timeSlot Bookings
    public void updateTimeSlotBookings() {
        DatabaseReference mDatabase;
        for (int i = 0; i < Constants.bookingTimeSlots.size(); i++) {
            String timeSlot = Constants.bookingTimeSlots.get(i);
            mDatabase = FirebaseDatabase
                    .getInstance()
                    .getReference("venues")
                    .child(LandingScreen.venueId)
                    .child("time_slots")
                    .child(Constants.gameScheduledDate)
                    .child(timeSlot);
            mDatabase.removeValue();
        }
        // Redirect user to invites
        redirectUserToInvites();
    }

    // Removing the booked time slots
    public void removeBookedTimeSlots() {
        for (int i = 0; i < indicesArray.size(); i++) {
            int index = indicesArray.get(i);
            TimeSlotAdapter.refreshedTimeSlotArray.remove(indicesArray.get(i));
        }
    }

    public String getCurrentUserId() {
        prefs = getSharedPreferences("MyPref", MODE_PRIVATE);
        return prefs.getString(Constants.USER_ID, null);
    }

    public String getCurrentUserName() {
        prefs = getSharedPreferences("MyPref", MODE_PRIVATE);
        return prefs.getString(Constants.USER_FULL_NAME, null);
    }

    public static long convertDateToMillis(String date) {
        long millis = 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("d-MM-yyyy");
            Date dateFormatted = sdf.parse(date);
            millis = dateFormatted.getTime();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return millis;
    }

    /**
     * Return date in specified format.
     *
     * @param milliSeconds Date in milliseconds
     * @return String representing date in specified format
     */
    public static String getDate(long milliSeconds) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat("EEE dd, MMM");

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
}
