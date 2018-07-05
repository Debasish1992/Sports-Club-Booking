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


}
