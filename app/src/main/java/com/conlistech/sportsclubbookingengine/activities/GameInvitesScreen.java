package com.conlistech.sportsclubbookingengine.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.adapters.InviteFriendList;
import com.conlistech.sportsclubbookingengine.adapters.ItemAdapter;
import com.conlistech.sportsclubbookingengine.adapters.UpcomingGameAdapter;
import com.conlistech.sportsclubbookingengine.models.GamePlayersModel;
import com.conlistech.sportsclubbookingengine.models.UserModel;
import com.conlistech.sportsclubbookingengine.utils.CommonUtils;
import com.conlistech.sportsclubbookingengine.utils.Constants;
import com.conlistech.sportsclubbookingengine.utils.LoaderUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GameInvitesScreen extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rcv_time_slots)
    RecyclerView rcvInviteFriendList;
    @BindView(R.id.layFriendNotFound)
    RelativeLayout layNoFriendsFound;
    @BindView(R.id.btnGoShare)
    Button btnShareGame;
    TextView inviteText;
    ArrayList<UserModel> userArray;
    public static ArrayList<String> gameInvitedUserId = new ArrayList<>();
    public static ArrayList<String> gameInvitedUserNames = new ArrayList<>();
    InviteFriendList inviteAdapter;
    public static GameInvitesScreen gameInvitesScreen;

    @OnClick(R.id.btnGoShare)
    void GoToShareScreen() {
        startActivity(new Intent(GameInvitesScreen.this, ShareGameScreen.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_invites_screen);
        ButterKnife.bind(this);
        layNoFriendsFound.setVisibility(RelativeLayout.GONE);
        inviteText = (TextView) toolbar.findViewById(R.id.toolbar_inviteText);
        gameInvitesScreen = this;
        btnShareGame.setVisibility(View.VISIBLE);

        initViews();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        CommonUtils.changeToolbarFont(toolbar, this);

        inviteText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Saving Game Invites Data
                pushInviteData();
            }
        });

        getAllFriends();
    }

    // Initializing the views
    private void initViews() {
        rcvInviteFriendList.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rcvInviteFriendList.setLayoutManager(layoutManager);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        ShareGameScreen.releaseAllValues();
        return true;
    }

    public void getAllFriends() {
        LoaderUtils.showProgressBar(GameInvitesScreen.this, "Please wait while loading...");
        userArray = new ArrayList<>();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("teammates")
                .child("my_teamates").child(getCurrentUserId());
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    UserModel users = noteDataSnapshot.getValue(UserModel.class);
                    String userId = users.getUserId();
                    String userName = users.getUserFullName();
                    userArray.add(users);
                }

                setUpAdapter();
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

    //
    public String getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("MyPref", MODE_PRIVATE);
        return prefs.getString(Constants.USER_ID, null);
    }

    // Setting up the adapter
    public void setUpAdapter() {
        if (userArray.size() > 0) {
            inviteAdapter = new InviteFriendList(GameInvitesScreen.this, userArray);
            rcvInviteFriendList.setAdapter(inviteAdapter);
        } else {
            Toast.makeText(GameInvitesScreen.this, "No Teammates Found", Toast.LENGTH_LONG).show();
            inviteText.setVisibility(TextView.GONE);
            rcvInviteFriendList.setVisibility(RecyclerView.GONE);
            layNoFriendsFound.setVisibility(RelativeLayout.VISIBLE);
            //inviteAdapter = new InviteFriendList(GameInvitesScreen.this, userArray);
            //rcvInviteFriendList.setAdapter(inviteAdapter);
        }
    }

    // Pushing game invites Data
    public void pushInviteData() {
        LoaderUtils.showProgressBar(GameInvitesScreen.this, "Please Wait..");
        DatabaseReference mDatabase = null;

        // Getting the pending invitation array
        ArrayList<GamePlayersModel> gamePlayersModelArrayList = new ArrayList<>();

        // Getting the Game Id
        String getGameId = GameInfoScreen.gameModel.getGameId();

        // Initializing the games Database Referance
        DatabaseReference mDatabaseGames = FirebaseDatabase
                .getInstance()
                .getReference("games")
                .child(getGameId);

        for (int i = 0; i < gameInvitedUserId.size(); i++) {
            // Saving Game invitations
            mDatabase = FirebaseDatabase
                    .getInstance()
                    .getReference("game_invites")
                    .child(gameInvitedUserId.get(i))
                    .child(GameInfoScreen.gameId);

            // pushing user to 'game_invites' node using the userId
            mDatabase.setValue(GameInfoScreen.gameModel);

            // Saving Pending Invitations
            GamePlayersModel gamePlayersModel = new GamePlayersModel();
            gamePlayersModel.setUserId(gameInvitedUserId.get(i));
            gamePlayersModel.setUserName(gameInvitedUserNames.get(i));
            gamePlayersModel.setUserRole(Constants.GAME_ROLE_PLAYER);
            gamePlayersModelArrayList.add(gamePlayersModel);
        }

        mDatabaseGames.child("gameInvitations").setValue(gamePlayersModelArrayList);

        DatabaseReference mDatabaseGameInvites = FirebaseDatabase
                .getInstance()
                .getReference("game_invites");

        for (int i = 0; i < gameInvitedUserId.size(); i++) {
            mDatabaseGameInvites
                    .child(gameInvitedUserId.get(i))
                    .child(GameInfoScreen.gameId)
                    .child("gameInvitations")
                    .setValue(gamePlayersModelArrayList);
        }


        LoaderUtils.dismissProgress();

        // Redirecting the user to share the game
        redirectUserToShareGame();

        Toast.makeText(this, "Game Invitation Sent Successfully.", Toast.LENGTH_SHORT).show();
    }

    public void redirectUserToShareGame() {
        startActivity(new Intent(GameInvitesScreen.this, ShareGameScreen.class));
    }

    // Finishing all the activities
    public static void finishAllScreens() {
        if (DetailsScreen.detailsScreen != null) {
            DetailsScreen.detailsScreen.finish();
        }

        if (TimeSlotSelector.timeSlotSelector != null) {
            TimeSlotSelector.timeSlotSelector.finish();
        }

        if (SelectTimeSlot.selectTimeSlot != null) {
            SelectTimeSlot.selectTimeSlot.finish();
        }

        if (GameInfoScreen.gameInfoScreen != null) {
            GameInfoScreen.gameInfoScreen.finish();
        }

        if (gameInvitesScreen != null) {
            gameInvitesScreen.finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ShareGameScreen.releaseAllValues();
    }


}
