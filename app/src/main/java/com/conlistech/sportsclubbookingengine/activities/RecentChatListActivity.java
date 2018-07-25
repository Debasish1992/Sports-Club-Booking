package com.conlistech.sportsclubbookingengine.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.adapters.RecentChatListAdapter;
import com.conlistech.sportsclubbookingengine.models.ChatModel;
import com.conlistech.sportsclubbookingengine.models.UserConversation;
import com.conlistech.sportsclubbookingengine.models.UserModel;
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
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RecentChatListActivity extends AppCompatActivity implements RecentChatListAdapter.ItemClickListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rcv_time_slots)
    RecyclerView rcvRecentChatList;
    @BindView(R.id.layFriendNotFound)
    RelativeLayout layNoFriendsFound;
    ArrayList<UserConversation> userArray;
    RecentChatListAdapter recentChatListadapter;
    TextView inviteText;
    @BindView(R.id.fab_add)
    FloatingActionButton mBtnAdd;


    @OnClick(R.id.fab_add)
    void addFriend() {
        startActivity(new Intent(RecentChatListActivity.this, AddTochatUserActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_invites_screen);
        ButterKnife.bind(this);
        toolbar.setTitle("Conversations");
        layNoFriendsFound.setVisibility(RelativeLayout.GONE);
        inviteText = (TextView) toolbar.findViewById(R.id.toolbar_inviteText);
        inviteText.setVisibility(View.GONE);

        initViews();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        getAllRecentChats();

        mBtnAdd.setVisibility(View.VISIBLE);
    }

    // Initializing the views
    private void initViews() {
        rcvRecentChatList.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rcvRecentChatList.setLayoutManager(layoutManager);
    }

    public void storeConversationInfo(String randomKey,
                                      UserConversation senderConversation, UserConversation receiverConversation) {

        DatabaseReference mDatabaseMessages = FirebaseDatabase.getInstance().getReference("conversation");

        mDatabaseMessages.child(senderConversation.getUserId()).child(randomKey).setValue(receiverConversation);

        DatabaseReference mDatabaseMessagesSender = FirebaseDatabase.getInstance().getReference("conversation");
        mDatabaseMessagesSender.child(receiverConversation.getUserId()).child(randomKey).setValue(senderConversation);
    }

    //
    public String getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("MyPref", MODE_PRIVATE);
        return prefs.getString(Constants.USER_ID, null);
    }

    public void getAllRecentChats() {
        LoaderUtils.showProgressBar(RecentChatListActivity.this, "Please wait while loading...");
        userArray = new ArrayList<>();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("conversation").child(getCurrentUserId());
        //  .child("my_teamates").child(getCurrentUserId());
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    UserConversation usersCon = noteDataSnapshot.getValue(UserConversation.class);
                    userArray.add(usersCon);
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
        if (userArray.size() > 0) {
            recentChatListadapter = new RecentChatListAdapter(RecentChatListActivity.this, userArray);
            recentChatListadapter.setClickListener(this);
            rcvRecentChatList.setAdapter(recentChatListadapter);
        } else {
            Toast.makeText(RecentChatListActivity.this, "No Teammates Found", Toast.LENGTH_LONG).show();
            inviteText.setVisibility(TextView.GONE);
            rcvRecentChatList.setVisibility(RecyclerView.GONE);
            layNoFriendsFound.setVisibility(RelativeLayout.VISIBLE);
            //inviteAdapter = new InviteFriendList(GameInvitesScreen.this, userArray);
            //rcvInviteFriendList.setAdapter(inviteAdapter);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onItemClick(View view, int position) {
        final UserConversation user = recentChatListadapter.mArrayList.get(position);

        ChatMessageActivity chatMessageActivity = new ChatMessageActivity();
        chatMessageActivity.storeChatInfo(user.getChannelID(), getChatDetails(user));

        Intent intent = new Intent(this, ChatMessageActivity.class);
        startActivity(intent);
    }

    public ChatModel getChatDetails(UserConversation user) {
        ChatModel chatModel = new ChatModel();
        //   SharedPreferences prefs = getSharedPreferences("MyPref", MODE_PRIVATE);
        chatModel.setSenderId(getCurrentUserId());
        chatModel.setReceiverID(user.getUserId());
        chatModel.setTimeStamp(getTimestampInUTC());
        chatModel.setChatMessage("message");
        chatModel.setMessageId(randomMessageID());
        chatModel.setReceiverFullName(user.getUserFullName());
        chatModel.setReceiverImage(user.getUserImage());
        return chatModel;
    }

    private String getTimestampInUTC() {
        Date myDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTime(myDate);
        Date time = calendar.getTime();
        SimpleDateFormat outputFmt = new SimpleDateFormat("MMM dd, yyy h:mm a zz");
        String dateAsString = outputFmt.format(time);
        return dateAsString;
    }


    public String randomMessageID() {
        // String easy = RandomString.digits + "ACEFGHJKLMNPQRUVWXYabcdefhijkprstuvwx";
        RandomString randoKey = new RandomString(32, new SecureRandom());
        return randoKey.nextString();
    }
}
