package com.conlistech.sportsclubbookingengine.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.adapters.GamePlayedAdapter;
import com.conlistech.sportsclubbookingengine.adapters.InviteFriendList;
import com.conlistech.sportsclubbookingengine.database.SqliteHelper;
import com.conlistech.sportsclubbookingengine.models.ChatUserOnlineModel;
import com.conlistech.sportsclubbookingengine.models.FriendModel;
import com.conlistech.sportsclubbookingengine.models.NotificationModel;
import com.conlistech.sportsclubbookingengine.models.UserConversation;
import com.conlistech.sportsclubbookingengine.models.UserModel;
import com.conlistech.sportsclubbookingengine.utils.CommonUtils;
import com.conlistech.sportsclubbookingengine.utils.Constants;
import com.conlistech.sportsclubbookingengine.utils.LoaderUtils;
import com.conlistech.sportsclubbookingengine.utils.NotificationUtils;
import com.conlistech.sportsclubbookingengine.utils.RandomNumberGenerator;
import com.conlistech.sportsclubbookingengine.utils.RandomString;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.soundcloud.android.crop.Crop;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.UUID;

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
    @BindView(R.id.tvRequestSent)
    TextView tvRequestSent;
    @BindView(R.id.ivPerson)
    ImageView ivProfileImage;
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
    @BindView(R.id.tvTeammatesCount)
    TextView tvTeamatesCount;
    UserModel userModel;
    SharedPreferences prefs;
    SqliteHelper sqliteHelper;
    GamePlayedAdapter gamePlayedAdapter;
    ArrayList<UserModel> userArray;
    @BindView(R.id.rcv_game_played)
    RecyclerView rcvGamePlayed;
    @BindView(R.id.tvNoFriendFound)
    TextView tvNoGameNotFound;
    MenuItem mItem;

    @OnClick(R.id.ivChat)
    void sendToChat() {
        // new AddTochatUserActivity().checkChatExistence(userModel, getCurrentUserId());
        checkChatExistence(userModel, getCurrentUserId());
    }

    long teammatesCount = 0;
    String userId;
    private final int PICK_IMAGE_REQUEST = 71;
    private Uri filePath;
    final int PIC_CROP = 1;
    FirebaseStorage storage;
    StorageReference storageReference;
    String userProfileImage;


    @OnClick(R.id.ivPerson)
    void ChangeProfilePic() {
        if (userId != null && userId.equalsIgnoreCase(getCurrentUserId())) {
            chooseImage();
        }
    }

    @OnClick(R.id.ivAddFriend)
    void addFriend() {
        getCurrentUserDetails();
    }

    @OnClick(R.id.ivLogout)
    void logout() {
       /* SharedPreferences preferences = getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
        sqliteHelper.removeAllRecords();
        finish();
        finishAllActivities();*/
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

    public void initFirebaseStorage() {
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }

    /**
     * Function to choose images
     */
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            beginCrop(filePath);
        } else if (requestCode == PIC_CROP) {
            if (data != null) {
                // get the returned data
                Bundle extras = data.getExtras();
                // get the cropped bitmap
                Bitmap selectedBitmap = extras.getParcelable("data");
                ivProfileImage.setImageBitmap(selectedBitmap);
            }
        } else if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, data);
        }
    }


    /**
     * Opening the cropper
     *
     * @param source
     */
    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
    }

    /**
     * Handling of the cropping
     *
     * @param resultCode
     * @param result
     */
    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            Uri fileUri = Crop.getOutput(result);
            ivProfileImage.setImageURI(fileUri);
            uploadImage(fileUri);
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Uploading image to firebase
     */
    private void uploadImage(Uri imagePathUpload) {
        LoaderUtils.showProgressBar(ProfileScreen.this, "Uploading your image..");
        if (imagePathUpload != null) {
            final StorageReference reference = storageReference.child("profileImages/" + UUID.randomUUID().toString());
            UploadTask uploadTask = reference.putFile(imagePathUpload);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override

                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    LoaderUtils.dismissProgress();
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    // Continue with the task to get the download URL
                    return reference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    LoaderUtils.dismissProgress();
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        userProfileImage = downloadUri.toString();
                        Log.d("Upload Image", downloadUri.toString());
                        // Uploading the image to firebase storage
                        updateUserProfileImage();
                    } else {
                        Toast.makeText(ProfileScreen.this, "Unable to upload your image", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    /**
     * Function responsible for updating user profile Picture
     */
    public void updateUserProfileImage() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
        // pushing user to 'users' node using the userId
        mDatabase.child(userId).child("userProfileImage").setValue(userProfileImage);
        updateLocalStorege();
    }

    /**
     * Function responsible for updating the profile image in local storage
     */
    public void updateLocalStorege() {
        SharedPreferences pref = getApplicationContext().
                getSharedPreferences("MyPref", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(Constants.USER_PROFILE_IMAGE, userProfileImage);
        editor.commit();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_screen);
        ButterKnife.bind(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Profile");
        tvNoGameNotFound.setVisibility(RelativeLayout.GONE);
        tvRequestSent.setVisibility(View.GONE);
        prefs = getSharedPreferences("MyPref", MODE_PRIVATE);
        sqliteHelper = new SqliteHelper(this);

        //setHomeButton for the Profile screen
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        CommonUtils.changeToolbarFont(toolbar, this);

        //calling recycler view
        initViews();

        ArrayList<String> getAllTeammates = sqliteHelper.getAllTeammateIds();

        String currentUserId = getCurrentUserId();

        ivLogoutUser.setVisibility(ImageView.GONE);

        initFirebaseStorage();


        // Check for current User
        if (TeammatesScreen.userId.equalsIgnoreCase(currentUserId)) {
            ivAddFriend.setVisibility(ImageView.GONE);
            //  ivLogoutUser.setVisibility(ImageView.VISIBLE);
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
                // Setting all the details data
                setUserData(userModel);
                //  getTeammatesCount();

                if (userModel != null) {
                    checkFriendRequestExistence(userModel);
                }
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

            if (userModel.getUserId().equalsIgnoreCase(getCurrentUserId())) {
                mItem.setVisible(true);
            } else {
                mItem.setVisible(true);
            }
            // boolean isProfileVisible = userModel.isProfile_visibility();
            userId = userModel.getUserId();
            if (!userModel.isProfile_visibility() &&
                    !userId.equalsIgnoreCase(getCurrentUserId())) {
                Toast.makeText(this, "The User Profile is Private", Toast.LENGTH_SHORT).show();
                tvFullName.setText(userModel.getUserFullName());
                tvEmail.setText(userModel.getUserEmail());
                tvUserFavSport.setText(userModel.getFavSport());
                tvUserPhoneNumber.setText(userModel.getUserPhoneNumber());
                loadUserProfileImage(userModel);
                //finish();
                //return;
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
                    loadUserProfileImage(userModel);
                }
            } else {
                tvFullName.setText(userModel.getUserFullName());
                tvEmail.setText(userModel.getUserEmail());
                tvUserFavSport.setText(userModel.getFavSport());
                tvUserPhoneNumber.setText(userModel.getUserPhoneNumber());
                loadUserProfileImage(userModel);
            }
        }
    }

    /**
     * Function responsible for loading the user profile image
     *
     * @param userModel
     */
    public void loadUserProfileImage(UserModel userModel) {
        if (userModel != null) {
            String profileImage = userModel.getUserProfileImage();
            if (!TextUtils.isEmpty(profileImage)) {
                Picasso.get()
                        .load(profileImage)
                        .into(ivProfileImage);
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

        showFriendRequestSent(userModel);
    }

    private void showFriendRequestSent(FriendModel userModel) {
        tvRequestSent.setVisibility(View.VISIBLE);
        ivAddFriend.setVisibility(View.GONE);
        findViewById(R.id.lyt_add_friend).setVisibility(View.VISIBLE);

        //store in Notification table of firebase
        createNotificationForRequestSend();
    }

    private void createNotificationForRequestSend() {
        NotificationActivity notificationActivity = new NotificationActivity();
        notificationActivity.storeNotificationInfo(NotificationUtils.getNotificationInfo(userId, getCurrentUserId(), getCurrentUserName(),
                Constants.FRIEND_REQUEST_SEND));
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
        friendModel.setUserProfileImage(prefs.getString(Constants.USER_PROFILE_IMAGE, null));
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
        mItem = menu.findItem(R.id.itemLogout);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.itemLogout:
                SharedPreferences preferences = getSharedPreferences("MyPref", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear();
                editor.commit();
                sqliteHelper.removeAllRecords();
                finish();
                finishAllActivities();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // Getting Current User Id
    public String getCurrentUserId() {
        return prefs.getString(Constants.USER_ID, null);
    }

    // Getting Current User Name
    public String getCurrentUserName() {
        return prefs.getString(Constants.USER_FULL_NAME, null);
    }

    /**
     * Function responsible for checking the Friend request Existence
     *
     * @param user
     */
    public void checkFriendRequestExistence(final UserModel user) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("teammates")
                .child("teammate_request").child(user.getUserId());
        mDatabase.child(getCurrentUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // TODO: handle the case where the data already exists
                    tvRequestSent.setVisibility(View.VISIBLE);
                    ivAddFriend.setVisibility(View.GONE);
                    findViewById(R.id.lyt_add_friend).setVisibility(View.VISIBLE);
                } else {
                    if (sqliteHelper.getAllTeammateIds().contains(user.getUserId())) {
                        // TODO: handle the case where the data does not yet exist
                        tvRequestSent.setVisibility(View.GONE);
                        ivAddFriend.setVisibility(View.GONE);
                        findViewById(R.id.lyt_add_friend).setVisibility(View.GONE);
                    } else {
                        tvRequestSent.setVisibility(View.GONE);
                        ivAddFriend.setVisibility(View.VISIBLE);
                        findViewById(R.id.lyt_add_friend).setVisibility(View.VISIBLE);
                    }
                }

                LoaderUtils.dismissProgress();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Function responsible for checking the chat existences
     *
     * @param user
     */
    public void checkChatExistence(final UserModel user, final String currentUserID) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("conversation")
                .child(currentUserID);
        mDatabase.child(user.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // TODO: handle the case where the data already exists
                    UserConversation usersCon = snapshot.getValue(UserConversation.class);
                    Constants.CHAT_CHANNEL_ID = usersCon.getChannelID();
                    Constants.isChatNotification = false;
                    Constants.CHAT_RECEIVER_ID = user.getUserId();
                    Constants.SENDER_USER_FULLNAME = user.getUserFullName();
                    Intent i = new Intent(ProfileScreen.this, ChatMessageActivity.class);
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


    /**
     * Function responsible for initiating the chat
     *
     * @param user
     */
    public void initiateChat(UserModel user) {
        randomChannelID();
        RecentChatListActivity recentChatListActivity = new RecentChatListActivity();
        recentChatListActivity.storeConversationInfo(getCurrentUserData(), getReceiverDetails(user));
        keepUserOnlineStatus(getReceiverDetails(user));

        Constants.CHAT_CHANNEL_ID = getCurrentUserData().getChannelID();
        Constants.isChatNotification = false;
        Constants.CHAT_RECEIVER_ID = user.getUserId();
        Constants.SENDER_USER_FULLNAME = user.getUserFullName();

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

    public UserConversation getCurrentUserData() {
        UserConversation userConversation = new UserConversation();
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


    /**
     * Function responsible for getting the teammates count
     */
       /* public void getTeammatesCount() {
            LoaderUtils.showProgressBar(ProfileScreen.this,
                    "Please wait while getting teammates..");
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("teammates")
                    .child("my_teamates").child(userId);

            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    teammatesCount = snapshot.getChildrenCount();
                    tvTeamatesCount.setText(String.valueOf(teammatesCount));
                    LoaderUtils.dismissProgress();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    LoaderUtils.dismissProgress();
                }
            });

        }*/

}