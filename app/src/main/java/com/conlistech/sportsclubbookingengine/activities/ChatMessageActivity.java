package com.conlistech.sportsclubbookingengine.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.os.UserManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.adapters.ChatMessageAdapter;
import com.conlistech.sportsclubbookingengine.adapters.RecentChatListAdapter;
import com.conlistech.sportsclubbookingengine.models.BaseMessage;
import com.conlistech.sportsclubbookingengine.models.ChatModel;
import com.conlistech.sportsclubbookingengine.models.UserConversation;
import com.conlistech.sportsclubbookingengine.models.UserMessage;
import com.conlistech.sportsclubbookingengine.utils.Constants;
import com.conlistech.sportsclubbookingengine.utils.LoaderUtils;
import com.conlistech.sportsclubbookingengine.utils.RandomString;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChatMessageActivity extends AppCompatActivity {
    @BindView(R.id.reyclerview_message_list)
    RecyclerView mMessageRecycler;
    private ChatMessageAdapter chatMessageAdapter;
    ArrayList<ChatModel> userArray = new ArrayList<>();
    UserConversation userConversation;
    @BindView(R.id.img_profile)
    ImageView mImgProfile;
    @BindView(R.id.textUserTitle)
    TextView mTvTitle;
    @BindView(R.id.textUserOnline)
    TextView mTvIsOnline;
    @BindView(R.id.edittext_chatbox)
    TextView mEdtTextMsg;
    @BindView(R.id.button_chatbox_send)
    ImageButton mBtnSend;
    @BindView(R.id.layFriendNotFound)
    RelativeLayout layNoFriendsFound;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @OnClick(R.id.img_back)
    public void onBack() {
        onBackPressed();
    }

    @OnClick(R.id.button_chatbox_send)
    public void onSend() {

        if (!mEdtTextMsg.getText().toString().isEmpty()) {
            storeChatInfo(getChatDetails(userConversation));
            mEdtTextMsg.setText(null);
        } else {
            Toast.makeText(this, "Please enter any message!!!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Constants.CHAT_USER_ID = null;
        Constants.IS_USER_ONLINE = false;
    }


    @Override
    protected void onPause() {
        super.onPause();
        Constants.CHAT_USER_ID = null;
        Constants.IS_USER_ONLINE = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);
        ButterKnife.bind(this);

        // Initializing the views
        initViews();

        // Calling the fetch messages function
        setActivityData();

        // Setting up the adapter
        setUpAdapter();

        Constants.CHAT_USER_ID = getCurrentUserId();
        Constants.IS_USER_ONLINE = true;

        /*mEdtTextMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshREcyclerViewIndex();
            }
        });*/


    }

    private void setActivityData() {
        userConversation = (UserConversation) getIntent().getSerializableExtra("userConversation");
        getAllChatMessages();
        //Setting the Data
        mTvTitle.setText(userConversation.getUserFullName());
    }

    // Initializing the views
    private void initViews() {
        layNoFriendsFound.setVisibility(RelativeLayout.GONE);
        mMessageRecycler.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mMessageRecycler.setLayoutManager(layoutManager);
    }

    /**
     * Getting all the chat messages from firebase table
     */
    private void getAllChatMessages() {
        LoaderUtils.showProgressBar(ChatMessageActivity.this, "Please wait while loading...");
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("chats");
        // .child(userConversation.getChannelID());

        Query query = mDatabase.child(userConversation.getChannelID()).orderByChild("timeStamp");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                userArray.clear();
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    ChatModel chatModel = noteDataSnapshot.getValue(ChatModel.class);
                    userArray.add(chatModel);
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

    public String getLastReceiverMsg() {
        String mStrLastMsg = "";
        if (userArray.size() > 0) {
            mStrLastMsg = userArray.get(userArray.size() - 1).getChatMessage();
        } else {
            mStrLastMsg = "Start your first chat.";
        }
        return mStrLastMsg;
    }

    public UserConversation getCurrentUserDetails() {
        UserConversation userConversation1 = new UserConversation();
        SharedPreferences prefs = getSharedPreferences("MyPref", MODE_PRIVATE);
        userConversation1.setUserId(prefs.getString(Constants.USER_ID, null));
        userConversation1.setUserFullName(prefs.getString(Constants.USER_FULL_NAME, null));
        userConversation1.setChannelID(userConversation.getChannelID());
        userConversation1.setOnline(false);
        userConversation1.setReceiverLastMsg(getLastReceiverMsg());
        userConversation1.setUserImage("https://s3.amazonaws.com/uifaces/faces/twitter/marcoramires/128.jpg");
        return userConversation1;
    }


    // Setting up the adapter
    public void setUpAdapter() {
        chatMessageAdapter = new ChatMessageAdapter(ChatMessageActivity.this,
                getCurrentUserId(),
                userArray);

        if (userArray.size() > 0) {
            mMessageRecycler.setVisibility(RecyclerView.VISIBLE);
            layNoFriendsFound.setVisibility(RelativeLayout.GONE);
            mMessageRecycler.setAdapter(chatMessageAdapter);
            refreshREcyclerViewIndex();
            storeLastMsgInConversation(getCurrentUserDetails(), userConversation);
        } else {
            // Toast.makeText(ChatMessageActivity.this, "No Chat Found", Toast.LENGTH_LONG).show();
            mMessageRecycler.setVisibility(RecyclerView.GONE);
            layNoFriendsFound.setVisibility(RelativeLayout.VISIBLE);
        }
    }


    public void storeLastMsgInConversation(UserConversation senderConversation,
                                           UserConversation receiverConversation) {

        String lastMessage = senderConversation.getReceiverLastMsg();
        String senderId = senderConversation.getUserId();
        String receiverId = receiverConversation.getUserId();

        DatabaseReference mDatabaseMessagesUpdateLastMessage =
                FirebaseDatabase.getInstance().getReference("conversation").child(senderId).child(receiverId);
        mDatabaseMessagesUpdateLastMessage.child("receiverLastMsg").setValue(lastMessage);

        DatabaseReference mDatabaseMessagesUpdateLastMessageReceiver =
                FirebaseDatabase.getInstance().getReference("conversation").child(receiverId).child(senderId);

        mDatabaseMessagesUpdateLastMessageReceiver.child("receiverLastMsg").setValue(lastMessage);
    }

    public void refreshREcyclerViewIndex() {
        mMessageRecycler.scrollToPosition(chatMessageAdapter.getItemCount() - 1);
    }

    /**
     * Function responsible for getting the current User ID
     *
     * @return
     */
    public String getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("MyPref", MODE_PRIVATE);
        return prefs.getString(Constants.USER_ID, null);
    }

    /**
     * Function responsible for getting the current User Name
     *
     * @return
     */
    public String getCurrentUserName() {
        SharedPreferences prefs = getSharedPreferences("MyPref", MODE_PRIVATE);
        return prefs.getString(Constants.USER_FULL_NAME, null);
    }

    /**
     * Function responsible for storing the Chat Model
     *
     * @param chatModel
     */
    public void storeChatInfo(ChatModel chatModel) {
        DatabaseReference mDatabaseMessages = FirebaseDatabase.getInstance().getReference("chats");
        mDatabaseMessages.child(userConversation.getChannelID()).child(randomMessageID()).setValue(chatModel);
        //getAllChatMessages();
    }

    /**
     * Function responsible for generating the random number for message ID
     *
     * @return
     */
    public static String randomMessageID() {
        // String easy = RandomString.digits + "ACEFGHJKLMNPQRUVWXYabcdefhijkprstuvwx";
        RandomString randoKey = new RandomString(32, new SecureRandom());
        return randoKey.nextString();
    }


    private MenuItem menuItem;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        menuItem = menu.findItem(R.id.item);
        return true;
    }

    private void menuItem(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // itemAdapter.getFilter().filter(newText);
                return true;
            }
        });
    }


    /**
     * Function responsible for building the chat Model
     *
     * @param user
     * @return
     */
    public ChatModel getChatDetails(UserConversation user) {
        ChatModel chatModel = new ChatModel();
        chatModel.setSenderId(getCurrentUserId());
        chatModel.setReceiverID(user.getUserId());
        chatModel.setSenderFullName(getCurrentUserName());
        chatModel.setTimeStamp(getTimestampInUTC());
        chatModel.setChatMessage(mEdtTextMsg.getText().toString().trim());
        chatModel.setReceiverFullName(user.getUserFullName());
        chatModel.setReceiverImage(user.getUserImage());
        return chatModel;
    }


    /**
     * Getting the time in UTC format
     *
     * @return
     */
    private String getTimestampInUTC() {
        Date myDate = new Date();
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTime(myDate);
        Date time = calendar.getTime();
        SimpleDateFormat outputFmt = new SimpleDateFormat("MMM dd, yyy h:mm:ss a zz");
        String dateAsString = outputFmt.format(time);
        String millis = convertDateToMillis(dateAsString);
        return millis;
    }

    public static String convertDateToMillis(String date) {
        long millis = 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyy h:mm:ss a zz");
            Date dateFormatted = sdf.parse(date);
            millis = dateFormatted.getTime();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return String.valueOf(millis);
    }

}