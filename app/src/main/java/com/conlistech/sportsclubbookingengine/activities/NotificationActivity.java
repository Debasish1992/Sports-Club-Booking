package com.conlistech.sportsclubbookingengine.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.adapters.NotificatoinAdapter;
import com.conlistech.sportsclubbookingengine.interfaces.ItemClickListener;
import com.conlistech.sportsclubbookingengine.models.NotificationModel;
import com.conlistech.sportsclubbookingengine.models.UserConversation;
import com.conlistech.sportsclubbookingengine.models.UserModel;
import com.conlistech.sportsclubbookingengine.utils.CommonUtils;
import com.conlistech.sportsclubbookingengine.utils.Constants;
import com.conlistech.sportsclubbookingengine.utils.LoaderUtils;
import com.conlistech.sportsclubbookingengine.utils.RandomString;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NotificationActivity extends AppCompatActivity implements ItemClickListener {


    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rcv_time_slots)
    RecyclerView rcvRecentChatList;
    @BindView(R.id.layFriendNotFound)
    RelativeLayout layNoFriendsFound;
    ArrayList<NotificationModel> userArray;
    NotificatoinAdapter notificatoinAdapter;
    TextView inviteText;
    @BindView(R.id.tvNoFriendFound)
    TextView mTxtNoFriendFound;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_invites_screen);
        ButterKnife.bind(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Notifications");
        layNoFriendsFound.setVisibility(RelativeLayout.GONE);
        inviteText = (TextView) toolbar.findViewById(R.id.toolbar_inviteText);
        inviteText.setVisibility(View.GONE);
        prefs = getSharedPreferences("MyPref", MODE_PRIVATE);

        initViews();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        CommonUtils.changeToolbarFont(toolbar, this);

        getAllNotificationList();
    }

    // Initializing the views
    private void initViews() {
        mTxtNoFriendFound.setText("OOPS !! No Notifications Yet.");
        rcvRecentChatList.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rcvRecentChatList.setLayoutManager(layoutManager);
    }

    /**
     * Function to store notification into the firebase Notification table
     *
     * @param notificationModel
     */
    public void storeNotificationInfo(NotificationModel notificationModel) {
        DatabaseReference mDatabaseMessages = FirebaseDatabase.getInstance().getReference("notificaton");
        mDatabaseMessages.child(notificationModel.getReceiverUserID()).child(randomMessageID())
                .setValue(notificationModel);

    }

    /**
     * getting all Notification List from firebase
     */
    public void getAllNotificationList() {
        LoaderUtils.showProgressBar(NotificationActivity.this, "Please wait while loading...");

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("notificaton")
                .child(getCurrentUserId());
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                userArray = new ArrayList<>();
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    NotificationModel notificationModel = noteDataSnapshot.getValue(NotificationModel.class);
                    userArray.add(notificationModel);
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

    /*public ArrayList<NotificationModel> getUserArray() {
        userArray = new ArrayList<>();
        String[] strArr = {"Friend request sent", "Friend request accepted", "Game Initiated"};
        for (int i = 0; i < 2; i++) {
            NotificationModel notificationModel = new NotificationModel();
            notificationModel.setNotificationID("" + getRandomKey());
            notificationModel.setSenderUserID(getCurrentUserDetails().getUserId());
            notificationModel.setSenderFullName(getCurrentUserDetails().getUserFullName());
            notificationModel.setNotifyMessage(strArr[i]);
            notificationModel.setNotifyTime(ChatMessageActivity.getTimestampInUTC());
            notificationModel.setNotifyType(Constants.FRIEND_REQUEST_SEND);
            userArray.add(notificationModel);
        }
        return userArray;
    }*/

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Setting up the adapter
    public void setUpAdapter() {
        if (userArray.size() > 0) {
            rcvRecentChatList.setVisibility(RecyclerView.VISIBLE);
            layNoFriendsFound.setVisibility(RelativeLayout.GONE);
            notificatoinAdapter = new NotificatoinAdapter(NotificationActivity.this, userArray);
            notificatoinAdapter.setClickListener(this);
            rcvRecentChatList.setAdapter(notificatoinAdapter);
        } else {
            //  Toast.makeText(RecentChatListActivity.this, "No Teammates Found", Toast.LENGTH_LONG).show();
            inviteText.setVisibility(TextView.GONE);
            rcvRecentChatList.setVisibility(RecyclerView.GONE);
            layNoFriendsFound.setVisibility(RelativeLayout.VISIBLE);
        }
    }

    /**
     * Function generate random key of 32 bit
     */
    public String randomMessageID() {
        // String easy = RandomString.digits + "ACEFGHJKLMNPQRUVWXYabcdefhijkprstuvwx";
        RandomString randoKey = new RandomString(32, new SecureRandom());
        return randoKey.nextString();
    }

    /**
     * Function to generate random key of 6 digit
     *
     * @return
     */
    public int getRandomKey() {
        Random rand = new Random();
        int num = rand.nextInt(900000) + 100000;
        return num;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    //gettting the current user id
    public String getCurrentUserId() {
        return prefs.getString(Constants.USER_ID, null);
    }

    //gettting the current user id
    public UserModel getCurrentUserDetails() {
        UserModel userModel = new UserModel();
        userModel.setUserId(prefs.getString(Constants.USER_ID, null));
        userModel.setUserFullName(prefs.getString(Constants.USER_FULL_NAME, null));
        userModel.setUserProfileImage("https://s3.amazonaws.com/uifaces/faces/twitter/marcoramires/128.jpg");
        return userModel;
    }

    @Override
    public void onItemClick(View view, int position) {
        final NotificationModel notificationModel = notificatoinAdapter.mArrayList.get(position);
        Intent intent = null;
        if (notificationModel.getNotifyType().equalsIgnoreCase(Constants.FRIEND_REQUEST_SEND)) {
            intent = new Intent(this, FriendRequestsScreen.class);
        } else if (notificationModel.getNotifyType().equalsIgnoreCase(Constants.FRIEND_REQUEST_ACCEPTED)) {
            intent = new Intent(this, TeammatesScreen.class);
        }
        startActivity(intent);
    }

}
