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

import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.adapters.ItemAdapter;
import com.conlistech.sportsclubbookingengine.adapters.TeammatesRequestAdapter;
import com.conlistech.sportsclubbookingengine.models.FriendModel;
import com.conlistech.sportsclubbookingengine.models.UserModel;
import com.conlistech.sportsclubbookingengine.utils.CommonUtils;
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

public class FriendRequestsScreen extends AppCompatActivity
        implements TeammatesRequestAdapter.ItemClickListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rcv_teammates_requests)
    RecyclerView rcv_temamates_requests;
    ArrayList<FriendModel> userModel;
    TeammatesRequestAdapter teammatesRequestAdapter;
    SharedPreferences pref;
    ArrayList<String> keyArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests_screen);
        ButterKnife.bind(this);

        // Setting up the toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        CommonUtils.changeToolbarFont(toolbar, this);
        pref = getSharedPreferences("MyPref", MODE_PRIVATE);

        initViews();

        fetchAllRequests();
    }

    @Override
    public boolean onSupportNavigateUp() {
        Constants.isTeammateRequestNotification = false;
        FriendRequestsScreen.this.finish();
        startActivity(new Intent(FriendRequestsScreen.this, TeammatesScreen.class));
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Constants.isTeammateRequestNotification = false;
        FriendRequestsScreen.this.finish();
        startActivity(new Intent(FriendRequestsScreen.this, TeammatesScreen.class));
    }

    // Initializing the views
    private void initViews() {
        rcv_temamates_requests.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rcv_temamates_requests.setLayoutManager(layoutManager);
    }


    public void fetchAllRequests() {
        LoaderUtils.showProgressBar(FriendRequestsScreen.this, "Please wait while loading...");
        userModel = new ArrayList<>();
        DatabaseReference mDatabase =
                FirebaseDatabase.getInstance().getReference("teammates")
                        .child("teammate_request").child(getCurrentUserId());
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                //UserModel value = dataSnapshot.getValue(UserModel.class);
                keyArray = new ArrayList<>();
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    String key = noteDataSnapshot.getKey();
                    keyArray.add(key);
                    FriendModel users = noteDataSnapshot.getValue(FriendModel.class);
                    userModel.add(users);
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


    // Setting up the adapter
    public void setUpAdapter() {
        teammatesRequestAdapter = new TeammatesRequestAdapter(FriendRequestsScreen.this,
                userModel, keyArray, getCurrentUserId(), getUserDetails());
        rcv_temamates_requests.setAdapter(teammatesRequestAdapter);
        teammatesRequestAdapter.setClickListener(this);
    }

    @Override
    public void onClick(View v, int position) {
        final FriendModel user = TeammatesRequestAdapter.mArrayList.get(position);
        TeammatesScreen.userId = user.getUserId();
        Intent i = new Intent(this, ProfileScreen.class);
        startActivity(i);
    }

    public String getCurrentUserId() {
        return pref.getString(Constants.USER_ID, null);
    }


    // Getting userModel
    public FriendModel getUserDetails() {
        // Building Model
        FriendModel userModel = new FriendModel();
        userModel.setUserFullName(pref.getString(Constants.USER_FULL_NAME, null));
        userModel.setUserPhoneNumber(pref.getString(Constants.USER_PHONE_NUMBER, null));
        userModel.setUserEmail(pref.getString(Constants.USER_EMAIL, null));
        userModel.setUserId(pref.getString(Constants.USER_ID, null));
        userModel.setFavSport(pref.getString(Constants.USER_FAV_SPORT, null));
        return userModel;
    }


}
