package com.conlistech.sportsclubbookingengine.activities;

import android.arch.persistence.room.Room;
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

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class SplashScreen extends AppCompatActivity {

    ArrayList<String> ids = new ArrayList<>();
    ArrayList<String> sportsArray = new ArrayList<>();
    SharedPrefManager hashMap = null;
    SqliteHelper db_sqlite;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);
        Stetho.initializeWithDefaults(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        prefs = getSharedPreferences("MyPref", MODE_PRIVATE);

        db_sqlite = new SqliteHelper(this);

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
                if(TextUtils.isEmpty(getCurrentUserId())){
                    startActivity(new Intent(SplashScreen.this,
                            LoginScreen.class));
                }else{
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
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("sports");
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


}