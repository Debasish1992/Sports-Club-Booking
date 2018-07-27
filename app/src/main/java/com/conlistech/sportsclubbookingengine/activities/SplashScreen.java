package com.conlistech.sportsclubbookingengine.activities;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.database.SqliteHelper;
import com.conlistech.sportsclubbookingengine.models.LocationModel;
import com.conlistech.sportsclubbookingengine.models.PaymentCardModel;
import com.conlistech.sportsclubbookingengine.models.SportsModel;
import com.conlistech.sportsclubbookingengine.models.UserModel;
import com.conlistech.sportsclubbookingengine.utils.Constants;
import com.conlistech.sportsclubbookingengine.utils.LoaderUtils;
import com.conlistech.sportsclubbookingengine.utils.RandomNumberGenerator;
import com.conlistech.sportsclubbookingengine.utils.SharedPrefManager;
import com.facebook.stetho.Stetho;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class SplashScreen extends AppCompatActivity {

    ArrayList<String> ids = new ArrayList<>();
    ArrayList<String> sportsArray = new ArrayList<>();
    SharedPrefManager hashMap = null;
    SqliteHelper db_sqlite;
    SharedPreferences prefs;
    DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);
        Stetho.initializeWithDefaults(this);
        FirebaseMessaging.getInstance().subscribeToTopic("pushNotifications");
       /* if (mDatabase == null) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            database.setPersistenceEnabled(true);
            mDatabase = database.getReference();
        }*/


        prefs = getSharedPreferences("MyPref", MODE_PRIVATE);

        if (!prefs.contains("Fcm_id")) {
            getPushNotificationToken();
        }

        db_sqlite = new SqliteHelper(this);

        //add_timeSlots();

        // Fetching the sports from the database
        if (db_sqlite.getSportsCount() == 0) {
            fetchUserDetails();
        } else {
            redirectUser();
        }
    }

    public void redirectUser() {
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                SplashScreen.this.finish();
                if (TextUtils.isEmpty(getCurrentUserId())) {
                    startActivity(new Intent(SplashScreen.this,
                            LoginScreen.class));
                } else {
                    startActivity(new Intent(SplashScreen.this,
                            LandingScreen.class));
                }
            }
        }, 3000);
    }

    // Function responsible fro fetching the guitar details
    public void fetchUserDetails() {
        LoaderUtils.showProgressBar(SplashScreen.this,
                "Please wait while fetching the details..");
        mDatabase = FirebaseDatabase.getInstance().getReference("sports");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for (DataSnapshot sportDataSnapshot : dataSnapshot.getChildren()) {
                    SportsModel sportsModel = sportDataSnapshot.getValue(SportsModel.class);
                    String sportId = sportsModel.getSportId();
                    String sportName = sportsModel.getSportName();
                    db_sqlite.insertSports(sportId, sportName);
                }
                redirectUser();
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

    public String getCurrentUserId() {
        return prefs.getString(Constants.USER_ID, null);
    }


    public void add_timeSlots() {
        ArrayList<LocationModel> arrayList = new ArrayList();

        LocationModel locationModel1 = new LocationModel();
        locationModel1.setLatitude("18.986634");
        locationModel1.setLongitude("72.814460");
        locationModel1.setAddress("Lala Lajpatrai Marg, Lotus Colony, Worli, Mumbai, Maharashtra 400018");

        LocationModel locationModel2 = new LocationModel();
        locationModel2.setLatitude("18.977142");
        locationModel2.setLongitude("72.815435");
        locationModel2.setAddress(" Keshav Rao Khadye Marg, Near Hajiali Circle, Tulsiwadi, Tardeo, Mumbai, Maharashtra 400034");

        LocationModel locationModel3 = new LocationModel();
        locationModel3.setLatitude("18.594139");
        locationModel3.setLongitude("73.758917");
        locationModel3.setAddress("165/1, 166/5 , Near by Mumbai-Bangalore Highway, Behind Vedanta Society, Wakad, Pune, Maharashtra 411057");

        LocationModel locationModel4 = new LocationModel();
        locationModel4.setLatitude("18.545523");
        locationModel4.setLongitude("73.804129");
        locationModel4.setAddress("19/1B/1, Near Hotel Green Park, Someshwar Wadi Road, Ward No. 8, Someshwarwadi, Pashan, Pune, Maharashtra 411008");
        arrayList.add(locationModel1);
        arrayList.add(locationModel2);
        arrayList.add(locationModel3);
        arrayList.add(locationModel4);


        ArrayList<String> arrayList1 = new ArrayList();
        arrayList1.add("-LFaxAYqiZb-1g-1kiEi");
        arrayList1.add("-LFayI-yoGkP06VNxTa6");
        arrayList1.add("-LFb2xylAm59CQkqOyRB");
        arrayList1.add("-LFpwKNw_O0JbAt9z2BE");


        for (int i = 0; i < arrayList1.size(); i++) {
            String date = String.valueOf(i);
            DatabaseReference mDatabaseReviews =
                    FirebaseDatabase.getInstance().getReference("venues")
                            .child(arrayList1.get(i)).child("location");
            mDatabaseReviews.setValue(arrayList.get(i));
        }
    }

    public void getPushNotificationToken() {
        // Get new Instance ID token
        String token = FirebaseInstanceId.getInstance().getToken();
        storeFcmId(token);
    }

    public void storeFcmId(String token) {
        SharedPreferences.Editor firebase_editor =
                prefs.edit();
        firebase_editor.putString("Fcm_id", token);
        firebase_editor.apply();
    }
}
