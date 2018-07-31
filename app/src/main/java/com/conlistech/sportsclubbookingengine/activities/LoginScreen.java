package com.conlistech.sportsclubbookingengine.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.models.UserModel;
import com.conlistech.sportsclubbookingengine.utils.Constants;
import com.conlistech.sportsclubbookingengine.utils.LoaderUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class LoginScreen extends AppCompatActivity {

    @BindView(R.id.link_signup) TextView tvSignUpLink;
    @BindView(R.id.input_email)
    EditText etEmail;
    @BindView(R.id.input_password)
    EditText etPassword;
    @BindView(R.id.btn_login)
    Button btnLogin;
    @BindView(R.id.link_forgotpassword)
    TextView tvForgorPassword;
    private FirebaseAuth firebaseAuth;
    public static LoginScreen loginScreen;

    @OnClick(R.id.link_signup) void submit() {
        startActivity(new Intent(LoginScreen.this,
                SignupScreen.class));
    }

    @OnClick (R.id.link_forgotpassword) void forgotPassword(){
        startActivity(new Intent(LoginScreen.this,
                ForgotPasswordScreen.class));
    }

    @OnClick(R.id.btn_login) void letUserIn(){
        login();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login_screen);
        ButterKnife.bind(this);
        firebaseAuth = FirebaseAuth.getInstance();
        loginScreen = this;
    }

    // Function Responsible for letting the user login
    public void login() {
        LoaderUtils.showProgressBar(LoginScreen.this, "Please Wait..");
        if (!validate()) {
            onLoginFailed();
            return;
        }
        btnLogin.setEnabled(false);


        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        // Function responsible for making the user log in
        signIn(email, password);
    }

    public void signIn(final String email, final String password){
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            String userId = user.getUid();
                            fetchUserDetails(userId);
                        } else {
                            // If sign in fails, display a message to the user.
                            onLoginFailed();

                            try {
                                throw task.getException();
                            } catch(FirebaseAuthWeakPasswordException e) {
                                Toast.makeText(LoginScreen.this,e.getMessage().toString(),Toast.LENGTH_LONG).show();
                            } catch(FirebaseAuthInvalidCredentialsException e) {
                                Toast.makeText(LoginScreen.this,e.getMessage().toString(),Toast.LENGTH_LONG).show();
                            } catch(FirebaseAuthUserCollisionException e) {
                                Toast.makeText(LoginScreen.this,e.getMessage().toString(),Toast.LENGTH_LONG).show();
                            } catch(Exception e) {
                                Toast.makeText(LoginScreen.this,e.getMessage().toString(),Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    // Function responsible fro fetching the guitar details
    public void fetchUserDetails(final String userId){
        LoaderUtils.showProgressBar(LoginScreen.this, "Please wait while fetching the details..");
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users").child(userId);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                UserModel user = dataSnapshot.getValue(UserModel.class);
                String userEmail = user.getUserEmail();
                String userFullName = user.getUserFullName();
                String userPhone = user.getUserPhoneNumber();
                String userFavSport = user.getFavSport();
                String userProfileImage = user.getUserProfileImage();
                boolean profileVisibility = user.isProfile_visibility();
                boolean contactsVisibility = user.isContact_visibility();

                // Storing the user details locally
                storingUserDetails(userId, userEmail, userFullName, userPhone,
                        userFavSport, profileVisibility, contactsVisibility, userProfileImage );
                onLoginSuccess();
                Log.d("HomeScreen", "Value is: " + user.toString());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                LoaderUtils.dismissProgress();
                // Failed to read value
                Log.w("HomeScreen", "Failed to read value.", error.toException());
            }
        });
    }

    // Storing UserDetails
    public void storingUserDetails(String userId,
                                   String userEmail,
                                   String userFullName,
                                   String phoneNumber,
                                   String userFavSport,
                                   boolean isProfileVisible,
                                   boolean isContactsVisible,
                                   String profileImage){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(Constants.USER_ID, userId);
        editor.putString(Constants.USER_EMAIL, userEmail);
        editor.putString(Constants.USER_FULL_NAME, userFullName);
        editor.putString(Constants.USER_PHONE_NUMBER, phoneNumber);
        editor.putString(Constants.USER_FAV_SPORT, userFavSport);
        editor.putBoolean(Constants.USER_PROFILE_VISIBILITY, isProfileVisible);
        editor.putBoolean(Constants.USER_CONTACTS_VISIBILITY, isContactsVisible);
        editor.putString(Constants.USER_PROFILE_IMAGE, profileImage);

        editor.commit();
    }

    /**
     * Function responsible for storing user details
     * @param userId user Id
     * @param userModel User Model
     */
    public void storeUserInfo(String userId, UserModel userModel){
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
        // pushing user to 'users' node using the userId
        mDatabase.child(userId).setValue(userModel);
    }


    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        LoaderUtils.dismissProgress();
        btnLogin.setEnabled(false);
        Toast.makeText(getBaseContext(), "Successfully logged in", Toast.LENGTH_LONG).show();
        finish();
        startActivity(new Intent(LoginScreen.this, LandingScreen.class));
    }

    public void onLoginFailed() {
        LoaderUtils.dismissProgress();
        btnLogin.setEnabled(true);
    }

    /**
     * Validating user inputs
     * @return
     */
    public boolean validate() {
        boolean valid = true;
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        if (email.isEmpty() ||
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            valid = false;
        } else {
            etEmail.setError(null);
        }

        if (password.isEmpty() ||
                password.length() < 4 ||
                password.length() > 10) {
            etPassword.setError("Password should not be less than 4 characters");
            valid = false;
        } else {
            etPassword.setError(null);
        }

        return valid;
    }
}
