package com.conlistech.sportsclubbookingengine.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.activities.ChatMessageActivity;
import com.conlistech.sportsclubbookingengine.activities.ExploreGamesScreen;
import com.conlistech.sportsclubbookingengine.activities.FriendRequestsScreen;
import com.conlistech.sportsclubbookingengine.activities.GameDetails;
import com.conlistech.sportsclubbookingengine.activities.LandingScreen;
import com.conlistech.sportsclubbookingengine.activities.RecentChatListActivity;
import com.conlistech.sportsclubbookingengine.activities.TeammatesScreen;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.facebook.stetho.inspector.network.ResponseHandlingInputStream.TAG;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final String TAG = "Notifications";
    String senderId = null;
    String receiverId = null;
    String channelId = null;
    String senderName = null;
    String receiverName = null;
    String messageType = null;
    String gameId = null;
    String game_creator_name = null;
    String game_creator_id = null;
    String game_play_date = null;
    Map<String, String> data = null;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        String notificationTitle = null, notificationBody = null;
        String strChatArray[] = null;
        List<String> tempCityList = null;


        String currentUserId = getCurrentUserId();
        Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        notificationTitle = remoteMessage.getNotification().getTitle();
        notificationBody = remoteMessage.getNotification().getBody();

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            if (remoteMessage.getData().size() > 0) {
                data = remoteMessage.getData();
                messageType = data.get(Constants.MESSAGE_TYPE);

                if (messageType != null &&
                        messageType.equalsIgnoreCase("ChatMessage")) {
                    receiverId = data.get(Constants.RECEIVER_ID);
                    senderId = data.get(Constants.SENDER_ID);
                    channelId = data.get(Constants.CHANNEL_ID);
                    receiverName = data.get(Constants.RECEIVER_NAME);
                    senderName = data.get(Constants.SENDER_NAME);
                }
            }
        }
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.

        if (messageType.equalsIgnoreCase("ChatMessage") &&
                Constants.CHAT_USER_ID == null &&
                receiverId.equalsIgnoreCase(getCurrentUserId()) &&
                !Constants.IS_USER_ONLINE) {
            sendNotification(notificationTitle, notificationBody);
        } else if (messageType.equalsIgnoreCase("ChatMessage") &&
                Constants.CHAT_USER_ID != null &&
                senderId.equalsIgnoreCase(Constants.CHAT_USER_ID)) {
        } else if (!TextUtils.isEmpty(messageType) &&
                messageType.equalsIgnoreCase("Teammate Request")) {
            senderId = data.get(Constants.SENDER_ID);
            senderName = notificationTitle;
            receiverId = data.get(Constants.RECEIVER_ID);
            if (currentUserId.equalsIgnoreCase(receiverId)) {
                sendNotification(notificationTitle, notificationBody);
            }
        } else if (!TextUtils.isEmpty(messageType) &&
                        messageType.equalsIgnoreCase("Teammate_Response")) {
            senderId = data.get(Constants.SENDER_ID);
            if (currentUserId.equalsIgnoreCase(senderId)) {
                sendNotification(notificationTitle, notificationBody);
            }
        }else if(!TextUtils.isEmpty(messageType) &&
                messageType.equalsIgnoreCase("NearBy_Game")){
            gameId = data.get(Constants.GAME_ID);
            game_creator_name = data.get(Constants.GAME_CREATOR_NAME);
            game_play_date = data.get(Constants.GAME_PLAY_DATE);
            game_creator_id =  data.get(Constants.GAME_CREATOR_ID);
            if(!getCurrentUserId().equalsIgnoreCase(game_creator_id)){
                sendNotification(notificationTitle,notificationBody);
            }
        }
    }

    public String getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("MyPref", MODE_PRIVATE);
        return prefs.getString(Constants.USER_ID, null);
    }


    // Function responsible for showing the push notifications
    private void sendNotification(String notificationTitle, String notificationBody) {
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.app_logo);
        Intent intent = null;
        if (messageType != null && messageType.equalsIgnoreCase("Teammate Request")) {
            Constants.isTeammateRequestNotification = true;
            intent = new Intent(this, TeammatesScreen.class);
        } else if (messageType != null && messageType.equalsIgnoreCase("ChatMessage")) {
            Constants.CHAT_CHANNEL_ID = channelId;
            Constants.isChatNotification = true;
            Constants.CHAT_RECEIVER_ID = senderId;
            Constants.SENDER_USER_FULLNAME = senderName;
            intent = new Intent(this, ChatMessageActivity.class);
        } else if (!TextUtils.isEmpty(messageType) &&
                messageType.equalsIgnoreCase("Teammate_Response")) {
            intent = new Intent(this, TeammatesScreen.class);
        }else if(!TextUtils.isEmpty(messageType) &&
                messageType.equalsIgnoreCase("NearBy_Game")){
            Constants.pushNotificationGameId = gameId;
            intent = new Intent(this, GameDetails.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setAutoCancel(true)   //Automatically delete the notification
                .setSmallIcon(R.drawable.app_logo) //Notification icon
                .setContentIntent(pendingIntent)
                .setContentTitle(notificationTitle)
                .setLargeIcon(icon)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setContentText(notificationBody)
                .setWhen(System.currentTimeMillis())
                .setSound(defaultSoundUri);
        notificationBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
        notificationBuilder.setLights(Color.YELLOW, 1000, 300);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = this.getString(R.string.default_notification_channel_id);
            NotificationChannel channel =
                    new NotificationChannel(channelId,
                            "UserSignUps",
                            NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(notificationBody);
            notificationManager.createNotificationChannel(channel);
            notificationBuilder.setChannelId(channelId);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }
}
