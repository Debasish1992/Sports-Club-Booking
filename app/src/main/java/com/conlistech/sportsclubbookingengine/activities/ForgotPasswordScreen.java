package com.conlistech.sportsclubbookingengine.activities;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.utils.CommonUtils;
import com.conlistech.sportsclubbookingengine.utils.LoaderUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ForgotPasswordScreen extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.input_email)
    EditText etEmail;
    @BindView(R.id.btn_submit)
    Button btnSubmit;

    @OnClick(R.id.btn_submit)
    void submit() {
        validateInputs();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_screen);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        CommonUtils.changeToolbarFont(toolbar, this);
    }

    public void validateInputs() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty() ||
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
        } else {
            resetPassword(email);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        ForgotPasswordScreen.this.finish();
        return true;
    }

    public void resetPassword(String email) {
        LoaderUtils.showProgressBar(ForgotPasswordScreen.this, "Please wait..");
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        LoaderUtils.dismissProgress();
                        if (task.isSuccessful()) {
                            Log.d("Forgot Password", "Email sent.");
                            Toast.makeText(ForgotPasswordScreen.this, "Please check your email inbox.", Toast.LENGTH_SHORT).show();
                            ForgotPasswordScreen.this.finish();
                        } else {
                            try {
                                throw task.getException();
                            } catch (Exception e) {
                                Toast.makeText(ForgotPasswordScreen.this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
                            }

                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ForgotPasswordScreen.this.finish();
    }
}
