package com.conlistech.sportsclubbookingengine.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.adapters.UpcomingGameAdapter;
import com.conlistech.sportsclubbookingengine.models.GameModel;
import com.conlistech.sportsclubbookingengine.models.GamePlayersModel;
import com.conlistech.sportsclubbookingengine.utils.CommonUtils;
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

public class ExploreGamesScreen extends AppCompatActivity implements UpcomingGameAdapter.ItemClickListener {

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
    @BindView(R.id.swipeContainer)
    android.support.v4.widget.SwipeRefreshLayout swipeRefreshLayout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_games_screen);
        ButterKnife.bind(this);
        initViews();
        setUpToolBar();

        fetchAllGames();

        // Refreshing the layout of the teammates
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchAllGames();
            }
        });

        // Configure the refreshing colors
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_red_light,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_blue_bright);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void setUpToolBar(){
        // Setting up Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        CommonUtils.changeToolbarFont(toolbar, this);
    }

    // Initializing the views
    private void initViews() {
        rcvUpcomingGames.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rcvUpcomingGames.setLayoutManager(layoutManager);
    }

    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("d-MM-yyyy");
        return sdf.format(new Date());
    }

    // Function responsible for fetching all the upcoming games
    public void fetchAllGames() {
        LoaderUtils.showProgressBar(ExploreGamesScreen.this,
                "Please wait while loading your games...");
        gameModelArrayList = new ArrayList<>();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("games");
        Query query = mDatabase.orderByChild("gameDate")
                .startAt(String.valueOf(GameInfoScreen.convertDateToMillis(getCurrentDate())));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for (DataSnapshot gameData : dataSnapshot.getChildren()) {
                    GameModel games = gameData.getValue(GameModel.class);
                    gameModelArrayList.add(games);
                }

                Collections.sort(gameModelArrayList, new Comparator<GameModel>() {
                    @Override
                    public int compare(GameModel o1, GameModel o2) {
                        return Long.compare(Long.parseLong(o1.getGameDate()),
                                Long.parseLong(o2.getGameDate()));
                    }
                });

                swipeRefreshLayout.setRefreshing(false);
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
        if (gameModelArrayList.size() > 0) {
            upcomingGameAdapter = new UpcomingGameAdapter(ExploreGamesScreen.this,
                    gameModelArrayList, getCurrentUserId());
            rcvUpcomingGames.setAdapter(upcomingGameAdapter);
            tvNoGamesFound.setVisibility(TextView.GONE);
            upcomingGameAdapter.setClickListener(this);
        } else {
            upcomingGameAdapter = new UpcomingGameAdapter(ExploreGamesScreen.this,
                    gameModelArrayList, getCurrentUserId());
            rcvUpcomingGames.setAdapter(upcomingGameAdapter);
            tvNoGamesFound.setVisibility(TextView.VISIBLE);
            upcomingGameAdapter.setClickListener(this);
        }
    }

    /**
     * Getting current userId
     * @return
     */
    public String getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("MyPref", MODE_PRIVATE);
        return prefs.getString(Constants.USER_ID, null);
    }

    @Override
    public void onClick(View view, int position) {
        UpcomingGamesScreen.gameModel = gameModelArrayList.get(position);
        startActivity(new Intent(ExploreGamesScreen.this, GameDetails.class));
    }
}
