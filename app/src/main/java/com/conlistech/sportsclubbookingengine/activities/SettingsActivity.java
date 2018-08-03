package com.conlistech.sportsclubbookingengine.activities;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.utils.CommonUtils;
import com.conlistech.sportsclubbookingengine.utils.Constants;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;

public class SettingsActivity extends AppCompatActivity {


    @BindView(R.id.chkProfileVisibility)
    android.support.v7.widget.SwitchCompat chkProfileVisibility;
    @BindView(R.id.chkContactVisibility)
    android.support.v7.widget.SwitchCompat chkContactVisibility;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    SharedPreferences pref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        CommonUtils.changeToolbarFont(toolbar, this);

        pref = getSharedPreferences("MyPref", MODE_PRIVATE);


        chkProfileVisibility.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateUserSettingsProfile(isChecked);
                updateStatusProfileVisibility(isChecked);
            }
        });

        chkContactVisibility.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateUserSettingsContacts(isChecked);
                updateStatusContactsVisibility(isChecked);
            }
        });

        // Keeping the check status
        chkProfileVisibility.setChecked(isProfileVisible());

        // Keeping the check status
        chkContactVisibility.setChecked(isContactsVisible());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Getting User Id
    public String getCurrentUserId() {
        return pref.getString(Constants.USER_ID, null);
    }


    // Getting profile visibility status
    public boolean isProfileVisible(){
        return pref.getBoolean(Constants.USER_PROFILE_VISIBILITY, false);
    }

    // Getting contacts visibility status
    public boolean isContactsVisible(){
        return pref.getBoolean(Constants.USER_CONTACTS_VISIBILITY, false);
    }

    // Updating the Profile Visibility
    public void updateUserSettingsProfile(boolean updateStatus) {
        DatabaseReference mDatabaseTeammate =
                FirebaseDatabase.getInstance().getReference("users")
                        .child(getCurrentUserId()).child("profile_visibility");
        mDatabaseTeammate.setValue(updateStatus);
    }

    // Updating the Contacts Visibility
    public void updateUserSettingsContacts(boolean updateStatus) {
        DatabaseReference mDatabaseTeammate =
                FirebaseDatabase.getInstance().getReference("users")
                        .child(getCurrentUserId()).child("contact_visibility");
        mDatabaseTeammate.setValue(updateStatus);
    }

    // Updating in local storage
    public void updateStatusProfileVisibility(boolean status){
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(Constants.USER_PROFILE_VISIBILITY, status);
        editor.commit();
    }

    // Updating in local storage
    public void updateStatusContactsVisibility(boolean status){
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(Constants.USER_CONTACTS_VISIBILITY, status);
        editor.commit();
    }
}
