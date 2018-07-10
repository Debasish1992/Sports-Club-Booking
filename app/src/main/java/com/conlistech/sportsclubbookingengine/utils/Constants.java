package com.conlistech.sportsclubbookingengine.utils;

import java.util.ArrayList;

public class Constants {

    public static final String USER_ID = "user_id";
    public static final String USER_EMAIL = "user_email";
    public static final String USER_FULL_NAME = "user_fullname";
    public static final String USER_PHONE_NUMBER = "user_phone_number";
    public static final String USER_FAV_SPORT = "user_fav_sport";
    public static final String USER_PROFILE_VISIBILITY = "user_profile_visibility";
    public static final String USER_CONTACTS_VISIBILITY = "user_contacts_visibility";


    // Firebase Constants
    public static final String TEAMMATE_TABLE = "teammates";
    public static final String TEAMMATE_REQUEST_TABLE = "teammate_request";


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


}
