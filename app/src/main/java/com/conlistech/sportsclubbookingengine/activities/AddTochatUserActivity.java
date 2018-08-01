package com.conlistech.sportsclubbookingengine.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
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
import com.conlistech.sportsclubbookingengine.models.ChatUserOnlineModel;
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
import java.util.ArrayList;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddTochatUserActivity extends AppCompatActivity
        implements ItemAdapter.ItemClickListener {

    MenuItem search;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rcv_teammates_add)
    RecyclerView rcvTeammates;
    ArrayList<UserModel> userModel;
    ItemAdapter itemAdapter;
    public static AddTochatUserActivity addTochatUserActivity;
    SqliteHelper sqliteHelper;
    ArrayList<String> getAllTeammates;
    private int MAX_LENGTH = 14;
    String chatUserId = null;


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

        addTochatUserActivity = AddTochatUserActivity.this;

        initViews();

        // Getting all the teammates
        getAllTeammates = sqliteHelper.getAllTeammateIds();

        fetchAllUsers();
    }

    public String getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("MyPref", MODE_PRIVATE);
        return prefs.getString(Constants.USER_ID, null);
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
        if (userModel.size() > 0) {
            itemAdapter = new ItemAdapter(AddTochatUserActivity.this, userModel);
            rcvTeammates.setAdapter(itemAdapter);
            itemAdapter.setClickListener(this);
        } else {
            Toast.makeText(addTochatUserActivity, "No Teammates Found.", Toast.LENGTH_SHORT).show();
            itemAdapter = new ItemAdapter(AddTochatUserActivity.this, userModel);
            rcvTeammates.setAdapter(itemAdapter);
            itemAdapter.setClickListener(this);
        }
    }

    public void fetchAllUsers() {
        LoaderUtils.showProgressBar(AddTochatUserActivity.this, "Please wait while loading...");
        userModel = new ArrayList<>();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance()
                .getReference("teammates")
                .child("my_teamates")
                .child(getCurrentUserId());

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    UserModel users = noteDataSnapshot.getValue(UserModel.class);
                    // Checking for the existence
                    userModel.add(users);
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

    @Override
    public void onClick(View v, int position) {
        final UserModel user = itemAdapter.mFilteredList.get(position);
        chatUserId = user.getUserId();
        checkChatExistence(user);
    }

    /**
     * Function responsible for initiating the chat
     *
     * @param user
     */
    public void initiateChat(UserModel user) {
        randomChannelID();
        RecentChatListActivity recentChatListActivity = new RecentChatListActivity();
        recentChatListActivity.storeConversationInfo(getCurrentUserDetails(), getReceiverDetails(user));
        keepUserOnlineStatus(getReceiverDetails(user));

        Constants.CHAT_CHANNEL_ID = getCurrentUserDetails().getChannelID();
        Constants.isChatNotification = false;
        Constants.CHAT_RECEIVER_ID = user.getUserId();
        Constants.SENDER_USER_FULLNAME = getCurrentUserDetails().getUserFullName();

        Intent i = new Intent(this, ChatMessageActivity.class);
        startActivity(i);
        finish();
    }

    /**
     * Setting user onine status
     *
     * @param user
     */
    public void keepUserOnlineStatus(UserConversation user) {
        ChatUserOnlineModel chatUserOnlineModelSender = new ChatUserOnlineModel();
        chatUserOnlineModelSender.setOnline(false);
        ChatUserOnlineModel chatUserOnlineModelReceiver = new ChatUserOnlineModel();
        chatUserOnlineModelReceiver.setOnline(false);
        DatabaseReference mDatabaseOnlineStatus = FirebaseDatabase.getInstance().getReference("online_status");
        mDatabaseOnlineStatus.child(user.getChannelID()).child(getCurrentUserId()).setValue(chatUserOnlineModelSender);
        DatabaseReference mDatabaseOnlineStatusReveiver = FirebaseDatabase.getInstance().getReference("online_status");
        mDatabaseOnlineStatusReveiver.child(user.getChannelID()).child(user.getUserId()).setValue(chatUserOnlineModelReceiver);
    }


    /**
     * Function responsible for checking the chat existences
     *
     * @param user
     */
    public void checkChatExistence(final UserModel user) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("conversation")
                .child(getCurrentUserId());
        mDatabase.child(chatUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // TODO: handle the case where the data already exists
                    Intent i = new Intent(AddTochatUserActivity.this, RecentChatListActivity.class);
                    startActivity(i);
                    finish();
                } else {
                    // TODO: handle the case where the data does not yet exist
                    initiateChat(user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public UserConversation getReceiverDetails(UserModel userModel) {
        UserConversation userConversation = new UserConversation();
        userConversation.setUserId(userModel.getUserId());
        userConversation.setUserFullName(userModel.getUserFullName());
        userConversation.setChannelID(randomChanelId);
        userConversation.setOnline(false);
        userConversation.setReceiverLastMsg("Start your first chat.");
        userConversation.setUserImage("https://s3.amazonaws.com/uifaces/faces/twitter/marcoramires/128.jpg");
        return userConversation;
    }

    public UserConversation getCurrentUserDetails() {
        UserConversation userConversation = new UserConversation();
        SharedPreferences prefs = getSharedPreferences("MyPref", MODE_PRIVATE);
        userConversation.setUserId(prefs.getString(Constants.USER_ID, null));
        userConversation.setUserFullName(prefs.getString(Constants.USER_FULL_NAME, null));
        userConversation.setChannelID(randomChanelId);
        userConversation.setOnline(false);
        userConversation.setReceiverLastMsg("Start your first chat.");
        userConversation.setUserImage("https://s3.amazonaws.com/uifaces/faces/twitter/marcoramires/128.jpg");
        return userConversation;
    }


    String randomChanelId = "";

    public void randomChannelID() {
        // String easy = RandomString.digits + "ACEFGHJKLMNPQRUVWXYabcdefhijkprstuvwx";
        RandomString randoKey = new RandomString(32, new SecureRandom());
        randomChanelId = randoKey.nextString();
    }

    public int getRandomKey() {
        Random rand = new Random();
        int num = rand.nextInt(900000) + 100000;
        return num;
    }

}
