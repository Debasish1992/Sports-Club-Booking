package com.conlistech.sportsclubbookingengine.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
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
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.conlistech.sportsclubbookingengine.utils.LoaderUtils.progressDialog;

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
    @BindView(R.id.ivPerson)
    ImageView ivUserProfilePicture;
    @BindView(R.id.link_login)
    TextView tvLoginLink;
    private FirebaseAuth firebaseAuth;
    public static SignupScreen SignUpScreen;
    SqliteHelper sqLite;
    ArrayList<String> sportsArray = new ArrayList<>();
    String sportId = null;
    String favSport = null;
    String userProfileImage = null;
    SharedPreferences pref;
    private final int PICK_IMAGE_REQUEST = 71;
    private Uri filePath;
    final int PIC_CROP = 1;
    FirebaseStorage storage;
    StorageReference storageReference;

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

    @OnClick(R.id.ivPerson)
    void upload() {
        chooseImage();
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            /*try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                ivUserProfilePicture.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            beginCrop(filePath);

        } else if (requestCode == PIC_CROP) {
            if (data != null) {
                // get the returned data
                Bundle extras = data.getExtras();
                // get the cropped bitmap
                Bitmap selectedBitmap = extras.getParcelable("data");
                ivUserProfilePicture.setImageBitmap(selectedBitmap);
            }
        } else if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, data);
        }
    }


    /**
     * Opening the cropper
     *
     * @param source
     */
    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
    }

    /**
     * Handling of the cropping
     *
     * @param resultCode
     * @param result
     */
    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            Uri fileUri = Crop.getOutput(result);
            ivUserProfilePicture.setImageURI(fileUri);
            uploadImage(fileUri);
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Uploading image to firebase
     */
    private void uploadImage(Uri imagePathUpload) {
        LoaderUtils.showProgressBar(SignUpScreen, "Uploading your image..");
        if (imagePathUpload != null) {
            final StorageReference reference = storageReference.child("profileImages/" + UUID.randomUUID().toString());
            UploadTask uploadTask = reference.putFile(imagePathUpload);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override

                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    LoaderUtils.dismissProgress();
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    // Continue with the task to get the download URL
                    return reference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    LoaderUtils.dismissProgress();
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        userProfileImage = downloadUri.toString();
                        Log.d("Upload Image", downloadUri.toString());
                    } else {
                        Toast.makeText(SignupScreen.this, "Unable to upload your image", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
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
        pref = getApplicationContext().
                getSharedPreferences("MyPref", 0);
        initFirebaseStorage();

        etPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    signUp();
                    handled = true;
                }
                return handled;
            }
        });

    }

    public void initFirebaseStorage() {
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
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
                                userModel.setUserProfileImage(userProfileImage);
                                userModel.setNotificationToken(pref.getString("Fcm_id", null));
                                // Storing User Details
                                storeUserInfo(uId, userModel);
                                // Storing the user details locally
                                storingUserDetails(uId, email, userFullName, phoneNumber,
                                        favSports, userProfileImage);
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
                                   String favSports,
                                   String userProfileImage) {
        // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(Constants.USER_ID, userId);
        editor.putString(Constants.USER_EMAIL, userEmail);
        editor.putString(Constants.USER_FULL_NAME, userFullName);
        editor.putString(Constants.USER_PHONE_NUMBER, phoneNumber);
        editor.putString(Constants.USER_FAV_SPORT, favSports);
        editor.putString(Constants.USER_PROFILE_IMAGE, userProfileImage);
        editor.putBoolean(Constants.USER_PROFILE_VISIBILITY, true);
        editor.putBoolean(Constants.USER_CONTACTS_VISIBILITY, true);
        editor.commit();
    }

}
