package com.conlistech.sportsclubbookingengine.database;

public class DatabaseConstants {

    public static final String DATABASE_NAME = "Sports_App_Db";
    public static final int DATABASE_VERSION = 2;

    public static final String SPORTS_TABLE = "sports_table";
    public static final String TEAMMATE_TABLE = "teammate_table";
    public static final String PAYMENT_TABLE = "payment_table";

    public static final String SPORT_ID = "sport_id";
    public static final String SPORT_NAME = "sport_name";

    public static final String TEAMMATE_ID = "teammate_id";
    public static final String TEAMMATE_NAME = "teammate_name";

    public static final String CARD_ID = "card_id";
    public static final String CARD_NUMBER = "card_number";
    public static final String CARD_TYPE = "card_type";
    public static final String CARD_EXPIRY = "card_exp";
    public static final String PRIMARY_STATUS = "is_primary";


    // Create table SQL query
    public static final String CREATE_SPORT_TABLE =
            "CREATE TABLE " + SPORTS_TABLE + "("
                    + SPORT_ID + " TEXT PRIMARY KEY ,"
                    + SPORT_NAME + " TEXT"
                    + ")";

    public static final String CREATE_TEAMMATE_TABLE =
            "CREATE TABLE " + TEAMMATE_TABLE + "("
                    + TEAMMATE_ID + " TEXT PRIMARY KEY ,"
                    + TEAMMATE_NAME + " TEXT"
                    + ")";

    public static final String CREATE_PAYMENT_TABLE =
            "CREATE TABLE " + PAYMENT_TABLE + "("
                    + CARD_ID + " INTEGER PRIMARY KEY ,"
                    + CARD_NUMBER + " TEXT ,"
                    + CARD_TYPE + " TEXT ,"
                    + CARD_EXPIRY + " TEXT ,"
                    + PRIMARY_STATUS + " INTEGER"
                    + ")";


}
