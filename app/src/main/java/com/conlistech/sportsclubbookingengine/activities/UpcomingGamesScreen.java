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
import android.widget.ImageView;
import android.widget.TextView;


import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.adapters.ItemAdapter;
import com.conlistech.sportsclubbookingengine.adapters.UpcomingGameAdapter;
import com.conlistech.sportsclubbookingengine.models.GameModel;
import com.conlistech.sportsclubbookingengine.models.UserModel;
import com.conlistech.sportsclubbookingengine.utils.Constants;
import com.conlistech.sportsclubbookingengine.utils.LoaderUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UpcomingGamesScreen extends AppCompatActivity
        implements UpcomingGameAdapter.ItemClickListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rcv_upcoming_games)
    RecyclerView rcvUpcomingGames;
    @BindView(R.id.ivNoGamesFound)
    TextView tvNoGamesFound;
    ImageView gameNotifications;
    ArrayList<GameModel> gameModelArrayList;
    ArrayList<GameModel> gameInvitationArrayList;
    UpcomingGameAdapter upcomingGameAdapter;
    public static GameModel gameModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upcoming_games_screen);
        ButterKnife.bind(this);

        initViews();
        gameNotifications = (ImageView) toolbar.findViewById(R.id.ivGameInvites);
        gameNotifications.setVisibility(ImageView.GONE);
        tvNoGamesFound.setVisibility(TextView.GONE);

        // Setting up Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        gameNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UpcomingGamesScreen.this, GameInvitations.class));
            }
        });

        // Getting all the game invitations
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
        UpcomingGamesScreen.this.finish();
    }

    public String getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("MyPref", MODE_PRIVATE);
        return prefs.getString(Constants.USER_ID, null);
    }

    // Initializing the views
    private void initViews() {
        rcvUpcomingGames.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rcvUpcomingGames.setLayoutManager(layoutManager);
    }


    // Function responsible for fetching all the upcoming games
    public void fetchAllUpcomingGames() {
        LoaderUtils.showProgressBar(UpcomingGamesScreen.this,
                "Please wait while loading your games...");
        gameModelArrayList = new ArrayList<>();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("games");
        Query query = mDatabase.orderByChild("gameCreatorUserId").equalTo(getCurrentUserId());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for (DataSnapshot gameData : dataSnapshot.getChildren()) {
                    GameModel games = gameData.getValue(GameModel.class);
                    long getGameTime = Long.parseLong(games.getGameDate());
                    long getCurrentTimeMillis = GameInfoScreen.convertDateToMillis(getCurrentDate());
                    if (getGameTime > getCurrentTimeMillis) {
                        gameModelArrayList.add(games);
                    }
                }

                Collections.sort(gameModelArrayList, new Comparator<GameModel>() {
                    @Override
                    public int compare(GameModel o1, GameModel o2) {
                        return Long.compare(Long.parseLong(o1.getGameDate()),
                                Long.parseLong(o2.getGameDate()));
                    }
                });

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

    public long getCurrentDateMillis() {
        Long tsLong = System.currentTimeMillis() / 1000;
        String convertedDate = tsLong.toString();
        return tsLong;
    }

    // Setting up the adapter
    public void setUpAdapter() {
        if (gameModelArrayList.size() > 0) {
            upcomingGameAdapter = new UpcomingGameAdapter(UpcomingGamesScreen.this,
                    gameModelArrayList, getCurrentUserId());
            rcvUpcomingGames.setAdapter(upcomingGameAdapter);
            tvNoGamesFound.setVisibility(TextView.GONE);
            upcomingGameAdapter.setClickListener(this);
        } else {
            upcomingGameAdapter = new UpcomingGameAdapter(UpcomingGamesScreen.this,
                    gameModelArrayList, getCurrentUserId());
            rcvUpcomingGames.setAdapter(upcomingGameAdapter);
            tvNoGamesFound.setVisibility(TextView.VISIBLE);
            upcomingGameAdapter.setClickListener(this);
        }
    }

    public String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("d-MM-yyyy");
        return sdf.format(new Date());
    }


    // Fetching all the game invitations
    public void fetchAllGameInvitations() {
        LoaderUtils.showProgressBar(UpcomingGamesScreen.this,
                "Checking your game Invitations...");
        gameInvitationArrayList = new ArrayList<>();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("game_invites").
                child(getCurrentUserId());
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                int getChildCount = (int) dataSnapshot.getChildrenCount();

                if (getChildCount > 0) {
                    gameNotifications.setVisibility(ImageView.VISIBLE);
                }
                LoaderUtils.dismissProgress();

                fetchAllUpcomingGames();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                LoaderUtils.dismissProgress();
                // Failed to read value
                Log.w("HomeScreen", "Failed to read value.", error.toException());
            }
        });
    }


    @Override
    public void onClick(View view, int position) {
        gameModel = gameModelArrayList.get(position);
        startActivity(new Intent(UpcomingGamesScreen.this, GameDetails.class));
    }
}
