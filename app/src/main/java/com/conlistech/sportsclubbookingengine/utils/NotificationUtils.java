package com.conlistech.sportsclubbookingengine.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.conlistech.sportsclubbookingengine.activities.ChatMessageActivity;
import com.conlistech.sportsclubbookingengine.models.NotificationModel;
import com.conlistech.sportsclubbookingengine.models.UserModel;

import java.util.Random;

import static android.content.Context.MODE_PRIVATE;

public class NotificationUtils {

    public static Context mContext;

    NotificationUtils(Context context) {
        this.mContext = context;
    }


    public static NotificationModel getNotificationInfo(String userId, String currentUserID,
                                                        String currentUserName, String noticationType) {
        NotificationModel notificationModel = new NotificationModel();
        notificationModel.setNotificationID("" + getRandomKey());
        notificationModel.setReceiverUserID(userId);
        notificationModel.setSenderUserID(currentUserID);
        notificationModel.setSenderFullName(currentUserName);
        notificationModel.setNotifyMessage(getMessageFromType(noticationType, currentUserName));
        notificationModel.setNotifyTime(ChatMessageActivity.getTimestampInUTC());
        notificationModel.setNotifyType(noticationType);
        return notificationModel;
    }

    public static String getMessageFromType(String noticationType, String currentUserName) {
        String message = "";
        if (noticationType.equalsIgnoreCase(Constants.FRIEND_REQUEST_SEND)) {
            message = "has sent you a Teammate request";
        } else if (noticationType.equalsIgnoreCase(Constants.FRIEND_REQUEST_ACCEPTED)) {
            message = "has accepted your Teammate request";
        } else if (noticationType.equalsIgnoreCase(Constants.FRIEND_REQUEST_REJECTED)) {
            message = "has rejected your Teammate request";
        }
        return message;
    }


    /**
     * Function to generate random key of 6 digit
     *
     * @return
     */
    public static int getRandomKey() {
        Random rand = new Random();
        int num = rand.nextInt(900000) + 100000;
        return num;
    }


}
