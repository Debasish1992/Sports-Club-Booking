package com.conlistech.sportsclubbookingengine.activities;

import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.os.UserManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.adapters.ChatMessageAdapter;
import com.conlistech.sportsclubbookingengine.adapters.ChatMessageListAdapter;
import com.conlistech.sportsclubbookingengine.models.BaseMessage;
import com.conlistech.sportsclubbookingengine.models.ChatModel;
import com.conlistech.sportsclubbookingengine.models.UserConversation;
import com.conlistech.sportsclubbookingengine.models.UserMessage;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import butterknife.BindView;

public class ChatMessageActivity extends AppCompatActivity {
    private RecyclerView mMessageRecycler;
    private ChatMessageAdapter chatMessageAdapter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);
        // toolbar = findViewById(R.id.toolbar);

        mMessageRecycler = findViewById(R.id.reyclerview_message_list);
        initViews();
        chatMessageAdapter = new ChatMessageAdapter(this, getBaseMessageData());
        //  chatMessageListAdapter.setClickListener(this);
        mMessageRecycler.setAdapter(chatMessageAdapter);

    }

    // Initializing the views
    private void initViews() {
        mMessageRecycler.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mMessageRecycler.setLayoutManager(layoutManager);
    }

    private ArrayList<UserMessage> getMessageDataList() {
        ArrayList<UserMessage> mArrMessageList = new ArrayList<>();
        String[] message = {"Hiii", "Hello!!!, Buddy how are you. How is all going?. Tell me if you have any time.", "This is Vaibhav Patil."};
        String[] nickName = {"Kar", "Vai", "Nil"};
        for (int i = 0; i < 3; i++) {
            UserMessage userMessage = new UserMessage();
            userMessage.setNickNmae(nickName[i]);
            userMessage.setMessage(message[i]);
            userMessage.setProfileImgURL("https://s3.amazonaws.com/uifaces/faces/twitter/marcoramires/128.jpg");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                userMessage.setCreatedAt("" + Calendar.getInstance().getTime());
            }
            mArrMessageList.add(userMessage);
        }

        return mArrMessageList;
    }

    private ArrayList<BaseMessage> getBaseMessageData() {
        String[] user = {"sender", "reciever", "sender"};

        ArrayList<BaseMessage> mArrBaseMesgData = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            BaseMessage baseMessage = new BaseMessage();
            baseMessage.setUser(user[i]);
            baseMessage.setmArrMessageList(getMessageDataList());
            mArrBaseMesgData.add(baseMessage);
        }
        return mArrBaseMesgData;
    }


    public void storeChatInfo(String channelID, ChatModel chatModel) {

        DatabaseReference mDatabaseMessages = FirebaseDatabase.getInstance().getReference("chats");

        mDatabaseMessages.child(channelID).setValue(chatModel);

        /* DatabaseReference mDatabaseMessagesSender = FirebaseDatabase.getInstance().getReference("chats");
          mDatabaseMessagesSender.child(receiverConversation.getUserId()).child(randomKey).setValue(senderConversation);*/
    }


    private MenuItem menuItem;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        menuItem = menu.findItem(R.id.item);
       /* SearchView searchView = (SearchView)
                MenuItemCompat.getActionView(menuItem);
        menuItem(searchView);*/
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


}