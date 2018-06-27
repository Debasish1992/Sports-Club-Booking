package com.conlistech.sportsclubbookingengine.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.database.SqliteHelper;
import com.conlistech.sportsclubbookingengine.models.UserModel;
import com.conlistech.sportsclubbookingengine.utils.Constants;
import com.conlistech.sportsclubbookingengine.utils.LoaderUtils;
import com.conlistech.sportsclubbookingengine.utils.RandomNumberGenerator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProfileScreen extends AppCompatActivity {

    @BindView(R.id.tvFullName)
    TextView tvFullName;
    @BindView(R.id.tvEmailValue)
    TextView tvEmail;
    @BindView(R.id.tvUserId)
    TextView tvUserFavSport;
    @BindView(R.id.tvPhoneValue)
    TextView tvUserPhoneNumber;
    @BindView(R.id.ivBack)
    ImageView ivBack;
    @BindView(R.id.ivAddFriend)
    ImageView ivAddFriend;
    @BindView(R.id.ivLogout)
    ImageView ivLogoutUser;
    @BindView(R.id.layDetails)
    RelativeLayout layProfileDetails;
    UserModel userModel;
    SharedPreferences prefs;
    SqliteHelper sqliteHelper;


    @OnClick(R.id.ivBack)
    void Back() {
        TeammatesScreen.userId = null;
        ProfileScreen.this.finish();
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
        sqliteHelper = new SqliteHelper(this);
        sqliteHelper.clearDb();
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
        prefs = getSharedPreferences("MyPref", MODE_PRIVATE);

        String currentUserId = getCurrentUserId();

        ivLogoutUser.setVisibility(ImageView.GONE);

        // Check for current User
        if (TeammatesScreen.userId.equalsIgnoreCase(currentUserId)) {
            ivAddFriend.setVisibility(ImageView.GONE);
            ivLogoutUser.setVisibility(ImageView.VISIBLE);
        }

        getUserDetails();
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
     * Storing user info
     *
     * @param userId
     * @param userModel
     */
    public void storeUserInfo(String userId, UserModel userModel) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference(Constants.TEAMMATE_TABLE);
        // pushing user to 'users' node using the userId
        mDatabase.child(Constants.TEAMMATE_REQUEST_TABLE)
                .child(userId)
                .child(String.valueOf(RandomNumberGenerator.getRandomNumber()))
                .setValue(userModel);
        Toast.makeText(this, "Teammate Request Sent Successfully.", Toast.LENGTH_SHORT).show();
    }

    // Getting Current User Details
    public void getCurrentUserDetails() {
        // Building Model
        UserModel userModel = new UserModel();
        userModel.setUserFullName(prefs.getString(Constants.USER_FULL_NAME, null));
        userModel.setUserPhoneNumber(prefs.getString(Constants.USER_PHONE_NUMBER, null));
        userModel.setUserEmail(prefs.getString(Constants.USER_EMAIL, null));
        userModel.setUserId(prefs.getString(Constants.USER_ID, null));
        userModel.setFavSport(prefs.getString(Constants.USER_FAV_SPORT, null));

        String favSport = prefs.getString(Constants.USER_FAV_SPORT, null);

        // Storing User Details as Request
        storeUserInfo(TeammatesScreen.userId, userModel);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        TeammatesScreen.userId = null;
    }

    // Getting Current User Id
    public String getCurrentUserId() {
        return prefs.getString(Constants.USER_ID, null);
    }
}