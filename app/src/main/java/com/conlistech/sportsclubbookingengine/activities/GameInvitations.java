package com.conlistech.sportsclubbookingengine.activities;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;


import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.adapters.UpcomingGameAdapter;
import com.conlistech.sportsclubbookingengine.models.GameModel;
import com.conlistech.sportsclubbookingengine.utils.Constants;
import com.conlistech.sportsclubbookingengine.utils.LoaderUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GameInvitations extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rcv_game_invitations)
    RecyclerView rcvGameInvitations;
    ArrayList<GameModel> gameInvitationArrayList;
    UpcomingGameAdapter upcomingGameAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_invitations);
        ButterKnife.bind(this);
        initViews();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Fetching all the invitations
        fetchAllGameInvitations();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        GameInvitations.this.finish();
    }

    // Initializing the views
    private void initViews() {
        rcvGameInvitations.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rcvGameInvitations.setLayoutManager(layoutManager);
    }

    public void fetchAllGameInvitations() {
        LoaderUtils.showProgressBar(GameInvitations.this,
                "Checking your game Invitations...");
        gameInvitationArrayList = new ArrayList<>();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("game_invites").
                child(getCurrentUserId());
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for (DataSnapshot gameData : dataSnapshot.getChildren()) {
                    GameModel games = gameData.getValue(GameModel.class);
                    gameInvitationArrayList.add(games);
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

    // Setting up the adapter
    public void setUpAdapter() {
        if (gameInvitationArrayList.size() > 0) {
            upcomingGameAdapter = new UpcomingGameAdapter(GameInvitations.this, gameInvitationArrayList, getCurrentUserId());
            rcvGameInvitations.setAdapter(upcomingGameAdapter);
        } else {
            upcomingGameAdapter = new UpcomingGameAdapter(GameInvitations.this, gameInvitationArrayList, getCurrentUserId());
            rcvGameInvitations.setAdapter(upcomingGameAdapter);
        }
    }

    public String getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("MyPref", MODE_PRIVATE);
        return prefs.getString(Constants.USER_ID, null);
    }
}
