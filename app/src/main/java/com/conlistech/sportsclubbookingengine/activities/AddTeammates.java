package com.conlistech.sportsclubbookingengine.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.view.MenuItemCompat;
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

public class AddTeammates extends AppCompatActivity
        implements ItemAdapter.ItemClickListener {

    MenuItem search;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rcv_teammates_add)
    RecyclerView rcvTeammates;
    ArrayList<UserModel> userModel;
    ItemAdapter itemAdapter;
    public static AddTeammates addTeammates;
    SqliteHelper sqliteHelper;
    ArrayList<String> getAllTeammates;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_teammates);
        ButterKnife.bind(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        sqliteHelper = new SqliteHelper(this);


        addTeammates = AddTeammates.this;

        initViews();

        // Getting all the teammates
        getAllTeammates = sqliteHelper.getAllTeammateIds();

        fetchAllUsers();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_teammates, menu);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
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

    // Initializing the views
    private void initViews() {
        rcvTeammates.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rcvTeammates.setLayoutManager(layoutManager);
    }

    // Setting up the adapter
    public void setUpAdapter() {
        if(userModel.size() > 0){
            itemAdapter = new ItemAdapter(AddTeammates.this, userModel);
            rcvTeammates.setAdapter(itemAdapter);
            itemAdapter.setClickListener(this);
        }else{
            Toast.makeText(addTeammates, "No Teammates Found.", Toast.LENGTH_SHORT).show();
            itemAdapter = new ItemAdapter(AddTeammates.this, userModel);
            rcvTeammates.setAdapter(itemAdapter);
            itemAdapter.setClickListener(this);
        }
    }

    public void fetchAllUsers() {
        LoaderUtils.showProgressBar(AddTeammates.this, "Please wait while loading...");
        userModel = new ArrayList<>();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                UserModel value = dataSnapshot.getValue(UserModel.class);
                Log.d("HomeScreen", "Value is: " + value.toString());
                String currentUserId = getCurrentUserId();
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    UserModel users = noteDataSnapshot.getValue(UserModel.class);
                    // Getting user id from model
                    String getUserId = users.getUserId();
                    // Checking for the existence
                    if(!getAllTeammates.contains(getUserId) &&
                            !getUserId.equalsIgnoreCase(currentUserId)){
                        userModel.add(users);
                    }
                    setUpAdapter();
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

    @Override
    public void onClick(View v, int position) {
        final UserModel user = itemAdapter.mFilteredList.get(position);
        TeammatesScreen.userId = user.getUserId();
        Intent i = new Intent(this, ProfileScreen.class);
        startActivity(i);
    }

    public String getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("MyPref", MODE_PRIVATE);
        return prefs.getString(Constants.USER_ID, null);
    }
}
