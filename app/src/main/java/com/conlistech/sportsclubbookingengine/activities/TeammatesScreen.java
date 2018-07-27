package com.conlistech.sportsclubbookingengine.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.adapters.ItemAdapter;
import com.conlistech.sportsclubbookingengine.database.SqliteHelper;
import com.conlistech.sportsclubbookingengine.models.UserModel;
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
import butterknife.OnClick;

public class TeammatesScreen extends AppCompatActivity
        implements ItemAdapter.ItemClickListener {

    MenuItem search;
    @BindView(R.id.rcv_teammates)
    RecyclerView mRecyclerView;
    ItemAdapter itemAdapter;
    ArrayList<UserModel> userModel;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.layTeammateRequest)
    RelativeLayout layTeammates;
    @BindView(R.id.tvRequestTitle)
    TextView tvRequests;
    public static String userId = null;
    public static boolean isRequestResponded = false;
    String countStr;
    @BindView(R.id.fabAddTeammates)
    android.support.design.widget.FloatingActionButton fabAddTeammates;
    @BindView(R.id.tvTeammatesNotFound)
    TextView tvNoTeamatesFound;
    public static TeammatesScreen teammatesScreen;
    SqliteHelper sqliteHelper;
    @BindView(R.id.swipeContainer)
    android.support.v4.widget.SwipeRefreshLayout swipeRefreshLayout;

    @OnClick(R.id.layTeammateRequest)
    void redirectUser() {
        Intent intent = new Intent(TeammatesScreen.this, FriendRequestsScreen.class);
        startActivity(intent);
    }

    @OnClick(R.id.fabAddTeammates)
    void addTeammates() {
        Intent intent = new Intent(TeammatesScreen.this, AddTeammates.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teammates_screen);
        ButterKnife.bind(this);
        initViews();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        layTeammates.setVisibility(RelativeLayout.GONE);
        tvNoTeamatesFound.setVisibility(TextView.GONE);
        teammatesScreen = TeammatesScreen.this;

        sqliteHelper = new SqliteHelper(this);

        // Refreshing the layout of the teammates
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchAllRequests();
            }
        });

        // Configure the refreshing colors
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        if(Constants.isTeammateRequestNotification){
            redirectUser();
        }else{
            fetchAllRequests();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        search = menu.findItem(R.id.search);
        SearchView searchView = (SearchView)
                MenuItemCompat.getActionView(search);
        search(searchView);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void search(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                itemAdapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    // Setting up the adapter
    public void setUpAdapter() {
        if (userModel.size() > 0) {
            tvNoTeamatesFound.setVisibility(TextView.GONE);
            itemAdapter = new ItemAdapter(TeammatesScreen.this, userModel);
            mRecyclerView.setAdapter(itemAdapter);
            itemAdapter.setClickListener(this);
        } else {
            tvNoTeamatesFound.setVisibility(TextView.VISIBLE);
            search.setVisible(false);
            itemAdapter = new ItemAdapter(TeammatesScreen.this, userModel);
            mRecyclerView.setAdapter(itemAdapter);
            itemAdapter.setClickListener(this);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    // Initializing the views
    private void initViews() {
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
    }

    // Function responsible for getting all the teammates
    public void fetchAllUsers() {
        LoaderUtils.showProgressBar(TeammatesScreen.this, "Please wait while loading...");
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("teammates")
                .child("my_teamates").child(getCurrentUserId());
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                userModel = new ArrayList<>();
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    UserModel users = noteDataSnapshot.getValue(UserModel.class);
                    String userId = users.getUserId();
                    String userName = users.getUserFullName();
                    sqliteHelper.insertTeammates(userId, userName);
                    userModel.add(users);
                }
                setUpAdapter();
                LoaderUtils.dismissProgress();
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                LoaderUtils.dismissProgress();
                // Failed to read value
                Log.w("HomeScreen", "Failed to read value.", error.toException());
            }
        });
    }


    // Function responsible for getting all the friend Requests
    public void fetchAllRequests() {
        LoaderUtils.showProgressBar(TeammatesScreen.this, "Please wait while loading...");
        userModel = new ArrayList<>();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance()
                .getReference(Constants.TEAMMATE_TABLE)
                .child(Constants.TEAMMATE_REQUEST_TABLE)
                .child(getCurrentUserId());
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long getCount = 0;
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                //UserModel value = dataSnapshot.getValue(UserModel.class);
                // for (DataSnapshot snap: dataSnapshot.getChildren()) {
                getCount = dataSnapshot.getChildrenCount();
                //}

                countStr = String.valueOf(getCount);

                LoaderUtils.dismissProgress();

                // Refreshing the UI
                updateUi();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                LoaderUtils.dismissProgress();
                // Failed to read value
                Log.w("HomeScreen", "Failed to read value.", error.toException());
            }
        });
    }

    // Function responsible for refreshing the UI
    public void updateUi() {
        if (Integer.parseInt(countStr) > 0) {
            layTeammates.setVisibility(RelativeLayout.VISIBLE);

            if (Integer.parseInt(countStr) == 1) {
                tvRequests.setText(countStr + " Teammate Request Pending");
            } else {
                tvRequests.setText(countStr + " Teammates Requests Pending");
            }
        } else {
            layTeammates.setVisibility(RelativeLayout.GONE);
        }

        // Fetching all the users
        fetchAllUsers();

    }

    public String getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("MyPref", MODE_PRIVATE);
        return prefs.getString(Constants.USER_ID, null);
    }

    @Override
    public void onClick(View v, int position) {
        final UserModel user = itemAdapter.mFilteredList.get(position);
        userId = user.getUserId();
        Intent i = new Intent(this, ProfileScreen.class);
        startActivity(i);
    }

    @Override
    protected void onResume() {
        super.onResume();
       /* if (TeammatesScreen.isRequestResponded) {
            fetchAllRequests();
            TeammatesScreen.isRequestResponded = false;
        }*/


    }
}
