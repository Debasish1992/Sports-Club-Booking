package com.conlistech.sportsclubbookingengine.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import static android.support.constraint.Constraints.TAG;

public class FirebaseIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " +
                refreshedToken);
        storeFcmId(refreshedToken);
    }

    public void storeFcmId(String token){
        SharedPreferences firebase_token_preferences =
                getSharedPreferences("MyPref",
                        Context.MODE_PRIVATE);
        SharedPreferences.Editor firebase_editor =
                firebase_token_preferences.edit();
        firebase_editor.putString("Fcm_id", token);
        firebase_editor.apply();
    }
}
