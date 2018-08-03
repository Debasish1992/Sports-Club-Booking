package com.conlistech.sportsclubbookingengine.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class Constants {

    public static final String USER_ID = "user_id";
    public static final String USER_EMAIL = "user_email";
    public static final String USER_FULL_NAME = "user_fullname";
    public static final String USER_PHONE_NUMBER = "user_phone_number";
    public static final String USER_FAV_SPORT = "user_fav_sport";
    public static final String USER_PROFILE_VISIBILITY = "user_profile_visibility";
    public static final String USER_CONTACTS_VISIBILITY = "user_contacts_visibility";
    public static final String USER_PROFILE_IMAGE = "user_profile_image";


    // Firebase Constants
    public static final String TEAMMATE_TABLE = "teammates";
    public static final String TEAMMATE_REQUEST_TABLE = "teammate_request";

    public static final String SENDER_ID = "sender_user_id";
    public static final String RECEIVER_ID = "receiver_user_id";
    public static final String CHANNEL_ID = "channelId";
    public static final String SENDER_NAME = "sender_user_name";
    public static final String RECEIVER_NAME = "receiver_user_fullName";
    public static final String MESSAGE_TYPE = "notification_type";


    //Game Schedule Constants
    public static String venueId = null;
    public static String venuePricing = null;
    public static String gameScheduledDate = null;
    public static ArrayList<String> bookingTimeSlots = new ArrayList<>();
    public static String gameName = null;
    public static String venueTotalBookingPrice = null;
    public static String gameTotalNoPlayers = null;
    public static String maximumNoPlayers = null;
    public static String gameNotes = null;
    public static String gameSport = null;

    public static final String GAME_ROLE_CREATOR = "Creator";
    public static final String GAME_ROLE_PLAYER = "Teammate";
    public static String CHAT_USER_ID = null;
    public static boolean IS_USER_ONLINE = false;
    public static boolean isTeammateRequestNotification = false;
    public static boolean isChatNotification = false;
    public static String CHAT_CHANNEL_ID = null;
    public static String CHAT_RECEIVER_ID = null;
    public static String RECEIVER_USER_FULLNAME = null;
    public static String SENDER_USER_FULLNAME = null;

    //For Notification section
    public static String FRIEND_REQUEST_SEND = "friend_request_send";
    public static String FRIEND_REQUEST_ACCEPTED = "friend_request_accepted";
    public static String FRIEND_REQUEST_REJECTED = "friend_request_rejected";



    public static final String APP_FIREBASE_BASE_LINK = "https://sports-club-eeee7.firebaseio.com/";
    public static final String APP_BASE_LINK = "http://www.conlistech.com/";
    public static final String APP_PACKAGE_NAME = "com.conlistech.sportsclubbookingengine";


    public static String buildFireBaseDeepLinkingURL(String gameId) {
        String deepLink = null;

        try {
            deepLink = APP_FIREBASE_BASE_LINK
                    + "link=" + APP_BASE_LINK + gameId
                    + "&apn=" + APP_PACKAGE_NAME;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return deepLink;
    }

    // COnvertig millisecond to date
    public static String convertToUTCDate(long time){
        SimpleDateFormat sdf = new SimpleDateFormat("EEE dd, MMM");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date(time));
    }


}
