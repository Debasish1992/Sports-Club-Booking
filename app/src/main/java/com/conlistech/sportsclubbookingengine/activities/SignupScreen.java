package com.conlistech.sportsclubbookingengine.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.database.SqliteHelper;
import com.conlistech.sportsclubbookingengine.models.UserConversation;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignupScreen extends AppCompatActivity {

    @BindView(R.id.input_name)
    EditText etName;
    @BindView(R.id.input_email)
    EditText etEmail;
    @BindView(R.id.input_phoneNo)
    EditText etPhoneNumber;
    @BindView(R.id.input_password)
    EditText etPassword;
    @BindView(R.id.btn_signup)
    Button btnSignUp;
    @BindView(R.id.tvFavoriteSports)
    TextView tvFavSports;
    @BindView(R.id.ivDismiss)
    ImageView ivDismiss;
    @BindView(R.id.link_login)
    TextView tvLoginLink;
    private FirebaseAuth firebaseAuth;
    public static SignupScreen SignUpScreen;
    SqliteHelper sqLite;
    ArrayList<String> sportsArray = new ArrayList<>();
    String sportId = null;
    String favSport = null;

    @OnClick(R.id.btn_signup)
    void submit() {
        signUp();
    }

    @OnClick(R.id.tvFavoriteSports)
    void displayDialog() {
        setUpSportsDialog();
    }

    @OnClick(R.id.ivDismiss)
    void Dismiss() {
        SignupScreen.this.finish();
    }

    @OnClick(R.id.link_login)
    void goLogin() {
        SignupScreen.this.finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_signup_screen);
        ButterKnife.bind(this);
        SignUpScreen = SignupScreen.this;
        firebaseAuth = FirebaseAuth.getInstance();
        sqLite = new SqliteHelper(SignupScreen.this);
        sportsArray = sqLite.getAllSports();
    }

    // Function responsible for making the user register
    public void signUp() {
        LoaderUtils.showProgressBar(SignupScreen.this, "Please Wait");
        if (!validate()) {
            onSignupFailed();
            return;
        }

        btnSignUp.setEnabled(false);
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();

        registerUser(email, password, name, phoneNumber, favSport);
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    // Function responsible for handling the sign up success
    public void onSignUpSuccess() {
        LoaderUtils.dismissProgress();
        Toast.makeText(getBaseContext(), "Registration Successful.", Toast.LENGTH_LONG).show();
        btnSignUp.setEnabled(false);
        if (LoginScreen.loginScreen != null) {
            LoginScreen.loginScreen.finish();
        }
        finish();
        startActivity(new Intent(SignupScreen.this, LandingScreen.class));
    }

    // Function responsible for handling the sign up failure
    public void onSignupFailed() {
        LoaderUtils.dismissProgress();
        btnSignUp.setEnabled(true);
    }

    public void setUpSportsDialog() {
        new MaterialDialog.Builder(SignupScreen.this)
                .title("Select Sport")
                .items(sportsArray)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog,
                                            View view, int which,
                                            final CharSequence text) {
                        favSport = text.toString();
                        tvFavSports.setText(text.toString());
                        sportId = sqLite.getSportId(text.toString());
                        Log.d("Sport id", sportId);
                    }
                })
                .show();
    }


    /**
     * Function responsible for validating the user inputs
     *
     * @return status of the validation
     */
    public boolean validate() {
        boolean valid = true;
        String name = etName.getText().toString();
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        String phoneNumber = etPhoneNumber.getText().toString().trim();

        if (name.isEmpty() ||
                name.length() < 3) {
            etName.setError("Name should be least 3 characters");
            etName.requestFocus();
            valid = false;
        } else if (email.isEmpty() ||
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            valid = false;
        } else if (phoneNumber.isEmpty() || phoneNumber.length() != 10) {
            etPhoneNumber.setError("Enter a valid phone number");
            etPhoneNumber.requestFocus();
            valid = false;
        } else if (password.isEmpty() ||
                password.length() < 6 || password.length() > 10) {
            etPassword.setError("Password should not be less than 6 characters");
            etPassword.requestFocus();
            valid = false;
        } else if (TextUtils.isEmpty(sportId)) {
            Toast.makeText(SignUpScreen, "Please Select Your Favorite Sports", Toast.LENGTH_SHORT).show();
            valid = false;
        } else {
            valid = true;
        }
        return valid;
    }

    /**
     * Function responsible for adding the user to the cloud server
     *
     * @param email    user given email address
     * @param password user given password
     */
    public void registerUser(final String email,
                             final String password,
                             final String userFullName,
                             final String phoneNumber,
                             final String favSports) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            String uId = null;

                            if (user != null) {
                                uId = user.getUid();
                                UserModel userModel = new UserModel();
                                userModel.setUserId(uId);
                                userModel.setUserFullName(userFullName);
                                userModel.setUserEmail(email);
                                userModel.setUserPhoneNumber(phoneNumber);
                                userModel.setFavSport(favSports);
                                // Storing User Details
                                storeUserInfo(uId, userModel);
                                // Storing the user details locally
                                storingUserDetails(uId, email, userFullName, phoneNumber, favSports);
                            }
                            onSignUpSuccess();
                        } else {
                            // If sign in fails, display a message to the user.
                            onSignupFailed();

                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                Toast.makeText(SignupScreen.this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                Toast.makeText(SignupScreen.this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
                            } catch (FirebaseAuthUserCollisionException e) {
                                Toast.makeText(SignupScreen.this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Toast.makeText(SignupScreen.this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    /**
     * Function responsible for storing user details
     *
     * @param userId    user Id
     * @param userModel User Model
     */
    public void storeUserInfo(String userId, UserModel userModel) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
        // pushing user to 'users' node using the userId
        mDatabase.child(userId).setValue(userModel);
        // pushing user to 'users' node using the userId
        mDatabase.child(userId).setValue(userModel);
    }



    /**
     * Storing the User Details in Shared Preference
     *
     * @param userId
     * @param userEmail
     * @param userFullName
     * @param phoneNumber
     */
    public void storingUserDetails(String userId,
                                   String userEmail,
                                   String userFullName,
                                   String phoneNumber,
                                   String favSports) {
        SharedPreferences pref = getApplicationContext().
                getSharedPreferences("MyPref", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(Constants.USER_ID, userId);
        editor.putString(Constants.USER_EMAIL, userEmail);
        editor.putString(Constants.USER_FULL_NAME, userFullName);
        editor.putString(Constants.USER_PHONE_NUMBER, phoneNumber);
        editor.putString(Constants.USER_FAV_SPORT, favSports);
        editor.putBoolean(Constants.USER_PROFILE_VISIBILITY, true);
        editor.putBoolean(Constants.USER_CONTACTS_VISIBILITY, true);
        editor.commit();
    }


}
