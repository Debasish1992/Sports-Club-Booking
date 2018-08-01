package com.conlistech.sportsclubbookingengine.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.adapters.GamePlayedAdapter;
import com.conlistech.sportsclubbookingengine.adapters.InviteFriendList;
import com.conlistech.sportsclubbookingengine.database.SqliteHelper;
import com.conlistech.sportsclubbookingengine.models.FriendModel;
import com.conlistech.sportsclubbookingengine.models.UserConversation;
import com.conlistech.sportsclubbookingengine.models.UserModel;
import com.conlistech.sportsclubbookingengine.utils.Constants;
import com.conlistech.sportsclubbookingengine.utils.LoaderUtils;
import com.conlistech.sportsclubbookingengine.utils.RandomNumberGenerator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProfileScreen extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tvFullName)
    TextView tvFullName;
    @BindView(R.id.tvEmailValue)
    TextView tvEmail;
    @BindView(R.id.tvUserId)
    TextView tvUserFavSport;
    @BindView(R.id.tvPhoneValue)
    TextView tvUserPhoneNumber;
    @BindView(R.id.ivCall)
    ImageView ivCall;
    @BindView(R.id.ivEmail)
    ImageView ivEmail;
    @BindView(R.id.ivChat)
    ImageView ivChat;
    @BindView(R.id.ivAddFriend)
    ImageView ivAddFriend;
    @BindView(R.id.ivLogout)
    ImageView ivLogoutUser;
    @BindView(R.id.layDetails)
    RelativeLayout layProfileDetails;
    UserModel userModel;
    SharedPreferences prefs;
    SqliteHelper sqliteHelper;
    GamePlayedAdapter gamePlayedAdapter;
    ArrayList<UserModel> userArray;
    @BindView(R.id.rcv_game_played)
    RecyclerView rcvGamePlayed;
    @BindView(R.id.tvNoFriendFound)
    TextView tvNoGameNotFound;

    @OnClick(R.id.ivChat)
    void sendToChat() {

        /*Intent intent = new Intent(this, ChatMessageActivity.class);
        startActivity(intent);*/
    }

    @OnClick(R.id.ivAddFriend)
    void addFriend() {
        getCurrentUserDetails();
    }

    @OnClick(R.id.ivLogout)
    void logout() {
        SharedPreferences preferences = getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
        sqliteHelper.removeAllRecords();
        finish();
        finishAllActivities();

    }

    // Finishing all the contexts
    public void finishAllActivities() {
        if (TeammatesScreen.teammatesScreen != null) {
            TeammatesScreen.teammatesScreen.finish();
        }

        if (AddTeammates.addTeammates != null) {
            AddTeammates.addTeammates.finish();
        }

        Intent intent = new Intent(ProfileScreen.this, LoginScreen.class);
        startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_screen);
        ButterKnife.bind(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Profile");
        tvNoGameNotFound.setVisibility(RelativeLayout.GONE);
        prefs = getSharedPreferences("MyPref", MODE_PRIVATE);
        sqliteHelper = new SqliteHelper(this);

        //setHomeButton for the Profile screen
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //calling recycler view
        initViews();

        ArrayList<String> getAllTeammates = sqliteHelper.getAllTeammateIds();

        String currentUserId = getCurrentUserId();

        ivLogoutUser.setVisibility(ImageView.GONE);


        // Check for current User
        if (TeammatesScreen.userId.equalsIgnoreCase(currentUserId)) {
            ivAddFriend.setVisibility(ImageView.GONE);
            ivLogoutUser.setVisibility(ImageView.VISIBLE);
        } else if (getAllTeammates.contains(TeammatesScreen.userId)) {
            ivAddFriend.setVisibility(ImageView.GONE);
        }

        getUserDetails();
        // getAllGamePlayed();
    }

    // Initializing the views
    private void initViews() {
        rcvGamePlayed.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rcvGamePlayed.setLayoutManager(layoutManager);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Getting User Model Data
    public void getUserDetails() {
        LoaderUtils.showProgressBar(ProfileScreen.this,
                "Please wait while fetching the details..");
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users")
                .child(TeammatesScreen.userId);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                userModel = dataSnapshot.getValue(UserModel.class);
                LoaderUtils.dismissProgress();
                // Setting all the details data
                setUserData(userModel);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                LoaderUtils.dismissProgress();
                // Failed to read value
            }
        });
    }

    public void getAllGamePlayed() {
        LoaderUtils.showProgressBar(ProfileScreen.this, "Please wait while loading...");
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

    // Setting up the adapter for the Game Played by User
    public void setUpAdapter() {
        if (userArray.size() > 0) {
            rcvGamePlayed.setVisibility(RecyclerView.VISIBLE);
            tvNoGameNotFound.setVisibility(RelativeLayout.GONE);
            gamePlayedAdapter = new GamePlayedAdapter(ProfileScreen.this, userArray);
            rcvGamePlayed.setAdapter(gamePlayedAdapter);
        } else {
            // Toast.makeText(GameInvitesScreen.this, "No Teammates Found", Toast.LENGTH_LONG).show();
            rcvGamePlayed.setVisibility(RecyclerView.GONE);
            tvNoGameNotFound.setVisibility(RelativeLayout.VISIBLE);
        }
    }

    /**
     * Setting user details in the view
     *
     * @param userModel UserModel
     */
    public void setUserData(UserModel userModel) {
        if (userModel != null) {
            // boolean isProfileVisible = userModel.isProfile_visibility();
            String userId = userModel.getUserId();
            if (!userModel.isProfile_visibility() &&
                    !userId.equalsIgnoreCase(getCurrentUserId())) {
                Toast.makeText(this, "The User Profile is Private", Toast.LENGTH_SHORT).show();
                finish();
                return;
            } else if (userModel.isProfile_visibility() &&
                    !userId.equalsIgnoreCase(getCurrentUserId())) {
                if (!userModel.isContact_visibility()) {
                    Toast.makeText(this, "The User Contacts are Private", Toast.LENGTH_SHORT).show();
                    tvFullName.setText(userModel.getUserFullName());
                    tvEmail.setText("NA");
                    tvUserFavSport.setText("NA");
                    tvUserPhoneNumber.setText("NA");
                    //layProfileDetails.setVisibility(RelativeLayout.GONE);
                } else {
                    tvFullName.setText(userModel.getUserFullName());
                    tvEmail.setText(userModel.getUserEmail());
                    tvUserFavSport.setText(userModel.getFavSport());
                    tvUserPhoneNumber.setText(userModel.getUserPhoneNumber());
                }
            } else {
                tvFullName.setText(userModel.getUserFullName());
                tvEmail.setText(userModel.getUserEmail());
                tvUserFavSport.setText(userModel.getFavSport());
                tvUserPhoneNumber.setText(userModel.getUserPhoneNumber());
            }
        }
    }


    /**
     * Checking teammate request existence for the same user
     *
     * @param userId
     * @param userModel
     */
    public void checkChatExistence(final String userId, final FriendModel userModel) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("teammates")
                .child("teammate_request").child(userId);
        mDatabase.child(getCurrentUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // TODO: handle the case where the data already exists
                    Toast.makeText(ProfileScreen.this,
                            "You have already sent a teammate request to " + userModel.getUserFullName(), Toast.LENGTH_SHORT).show();
                } else {
                    // TODO: handle the case where the data does not yet exist
                    storeUserInfo(userId, userModel);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    /**
     * Storing user info
     *
     * @param userId
     * @param userModel
     */
    public void storeUserInfo(String userId, FriendModel userModel) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance()
                .getReference(Constants.TEAMMATE_TABLE);
        mDatabase.child(Constants.TEAMMATE_REQUEST_TABLE)
                .child(userId)
                .child(getCurrentUserId())
                .setValue(userModel);
        Toast.makeText(this, "Teammate Request Sent Successfully.", Toast.LENGTH_SHORT).show();
    }

    // Getting Current User Details
    public void getCurrentUserDetails() {
        // Building Model
        FriendModel friendModel = new FriendModel();
        friendModel.setUserFullName(prefs.getString(Constants.USER_FULL_NAME, null));
        friendModel.setUserPhoneNumber(prefs.getString(Constants.USER_PHONE_NUMBER, null));
        friendModel.setUserEmail(prefs.getString(Constants.USER_EMAIL, null));
        friendModel.setUserId(prefs.getString(Constants.USER_ID, null));
        friendModel.setFavSport(prefs.getString(Constants.USER_FAV_SPORT, null));
        friendModel.setFriendUserId(userModel.getUserId());
        String favSport = prefs.getString(Constants.USER_FAV_SPORT, null);
        // Storing User Details as Request

        checkChatExistence(TeammatesScreen.userId, friendModel);
        //storeUserInfo(TeammatesScreen.userId, friendModel);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        TeammatesScreen.userId = null;
        ProfileScreen.this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.itemLogout:
                Toast.makeText(ProfileScreen.this, "LOGOUT", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // Getting Current User Id
    public String getCurrentUserId() {
        return prefs.getString(Constants.USER_ID, null);
    }

    public String getCurrentUserName() {
        return prefs.getString(Constants.USER_FULL_NAME, null);
    }
}