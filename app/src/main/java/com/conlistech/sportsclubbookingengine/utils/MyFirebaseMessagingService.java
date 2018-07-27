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
import android.util.Log;

import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.activities.LandingScreen;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Arrays;
import java.util.List;

import static com.facebook.stetho.inspector.network.ResponseHandlingInputStream.TAG;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final String TAG = "Notifications";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        String notificationTitle = null, notificationBody = null;
        String strChatArray[] = new String[200];
        List<String> tempCityList = null;

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            notificationTitle = remoteMessage.getNotification().getTitle();
            notificationBody = remoteMessage.getNotification().getBody();

            if (notificationBody.contains("-")) {
                strChatArray = notificationBody.split("-");
                // tempCityList = Arrays.asList(strChatArray);
                //notificationBody = tempCityList.get(0);
            }
        }
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.

       /* if (strChatArray[1].equalsIgnoreCase("ChatMessage") &&
                Constants.CHAT_USER_ID == null &&
                strChatArray[2].equalsIgnoreCase(getCurrentUserId()) &&
                !Constants.IS_USER_ONLINE) {
            notificationBody = strChatArray[0];
            sendNotification(notificationTitle, notificationBody);
        } else if (strChatArray[1].equalsIgnoreCase("ChatMessage") &&
                Constants.CHAT_USER_ID != null &&
                strChatArray[3].equalsIgnoreCase(Constants.CHAT_USER_ID)) {

        } else if (strChatArray == null) {
            sendNotification(notificationTitle, notificationBody);
        }*/

    }

    public String getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("MyPref", MODE_PRIVATE);
        return prefs.getString(Constants.USER_ID, null);
    }


    // Function responsible for showing the push notifications
    private void sendNotification(String notificationTitle, String notificationBody) {
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        Intent intent = new Intent(this, LandingScreen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setAutoCancel(false)   //Automatically delete the notification
                .setSmallIcon(R.mipmap.ic_launcher) //Notification icon
                .setContentIntent(pendingIntent)
                .setContentTitle(notificationTitle)
                .setLargeIcon(icon)
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
                            notificationTitle,
                            NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(notificationBody);
            notificationManager.createNotificationChannel(channel);
            notificationBuilder.setChannelId(channelId);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }
}
