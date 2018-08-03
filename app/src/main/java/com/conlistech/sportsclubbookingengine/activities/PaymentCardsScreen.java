package com.conlistech.sportsclubbookingengine.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.adapters.ItemAdapter;
import com.conlistech.sportsclubbookingengine.adapters.PaymentCardAdapter;
import com.conlistech.sportsclubbookingengine.database.SqliteHelper;
import com.conlistech.sportsclubbookingengine.models.PaymentCardModel;
import com.conlistech.sportsclubbookingengine.models.UserModel;
import com.conlistech.sportsclubbookingengine.utils.CommonUtils;
import com.conlistech.sportsclubbookingengine.utils.Constants;
import com.conlistech.sportsclubbookingengine.utils.LoaderUtils;
import com.conlistech.sportsclubbookingengine.utils.RandomNumberGenerator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

import static android.media.MediaRecorder.VideoSource.CAMERA;

public class PaymentCardsScreen extends AppCompatActivity {


    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fabAddCard)
    android.support.design.widget.FloatingActionButton fabAddCard;
    @BindView(R.id.rcv_payment_add)
    RecyclerView rcvPaymentCards;
    @BindView(R.id.tvCardNotFound)
    TextView tvCardNotFound;
    SharedPreferences pref;
    ArrayList<PaymentCardModel> paymentCardModels;
    PaymentCardAdapter paymentCardAdapter;
    SqliteHelper sqliteHelper;

    @OnClick(R.id.fabAddCard)
    void addCard() {
        onScanPress();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_cards_screen);
        ButterKnife.bind(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        CommonUtils.changeToolbarFont(toolbar, this);
        checkPermission();
        pref = getSharedPreferences("MyPref", MODE_PRIVATE);
        sqliteHelper = new SqliteHelper(this);
        tvCardNotFound.setVisibility(TextView.GONE);

        initViews();

        // Fetching all the payment cards
        fetchAllCards();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Initializing the views
    private void initViews() {
        rcvPaymentCards.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rcvPaymentCards.setLayoutManager(layoutManager);
    }


    // Checking Permission
    public boolean checkPermission() {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(PaymentCardsScreen.this,
                    Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) PaymentCardsScreen.this,
                        Manifest.permission.WRITE_CALENDAR)) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(PaymentCardsScreen.this);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission Necessary");
                    alertBuilder.setMessage("Camera Permission is necessary to set up your payment methods!!");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) PaymentCardsScreen.this,
                                    new String[]{Manifest.permission.WRITE_CALENDAR}, 1);
                        }
                    });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();
                } else {
                    ActivityCompat.requestPermissions((Activity) PaymentCardsScreen.this, new String[]{Manifest.permission.WRITE_CALENDAR}, 1);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0]
                        == PackageManager.PERMISSION_GRANTED) {
                    onScanPress();
                } else {
                }
                break;
        }
    }

    // Function responsible for canning the scanner
    public void onScanPress() {
        Intent scanIntent = new Intent(this, CardIOActivity.class);
        // customize these values to suit your needs.
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, false); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false); // default: false
        // MY_SCAN_REQUEST_CODE is arbitrary and is only used within this activity.
        startActivityForResult(scanIntent, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2) {
            String resultDisplayStr;
            if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);

                // Never log a raw card number. Avoid displaying it, but if necessary use getFormattedCardNumber()
                resultDisplayStr = "Card Number: " + scanResult.getRedactedCardNumber() + "\n";

                String cardType = scanResult.getCardType().toString();
                String expiry = scanResult.expiryMonth + "/" + scanResult.expiryYear;
                String cardNumber = scanResult.getRedactedCardNumber();

                // Do something with the raw number, e.g.:
                // myService.setCardNumber( scanResult.cardNumber );

                if (scanResult.isExpiryValid()) {
                    resultDisplayStr += "Expiration Date: " + scanResult.expiryMonth + "/" + scanResult.expiryYear + "\n";
                }

                if (scanResult.cvv != null) {
                    // Never log or display a CVV
                    resultDisplayStr += "CVV has " + scanResult.cvv.length() + " digits.\n";
                }

                if (scanResult.postalCode != null) {
                    resultDisplayStr += "Postal Code: " + scanResult.postalCode + "\n";
                }

                // Storing user Payment methods
                PaymentCardModel paymentCardModel = buildCardModel(cardNumber, expiry, cardType);
                storeUserPaymentCardDetails(paymentCardModel);

            } else {
                resultDisplayStr = "Scan was canceled.";
            }
        }
    }


    // Building the card Model
    public PaymentCardModel buildCardModel(String cardNumber,
                                           String cardExpiry,
                                           String cardType) {
        PaymentCardModel paymentCardModel = new PaymentCardModel();
        paymentCardModel.setCardNumber(cardNumber);
        paymentCardModel.setCardExpiry(cardExpiry);
        paymentCardModel.setCardType(cardType);
        if (paymentCardModels.size() == 0) {
            paymentCardModel.setPrimary(true);
        } else {
            paymentCardModel.setPrimary(false);
        }
        return paymentCardModel;
    }

    // Storing user Payment Cards
    public void storeUserPaymentCardDetails(PaymentCardModel paymentCardModel) {
        int cardId = RandomNumberGenerator.getRandomNumber();
        DatabaseReference mDatabasePayments =
                FirebaseDatabase.getInstance().getReference("payment_cards")
                        .child(getCurrentUserId()).child(String.valueOf(cardId));
        mDatabasePayments.setValue(paymentCardModel, cardId);
        storeCardLocally(paymentCardModel, cardId);
    }


    // Storing the Card Details Locally
    public void storeCardLocally(PaymentCardModel paymentCardModel, int cardId) {
        int primaryStatus = 0;
        boolean isPrimary = paymentCardModel.isPrimary();
        if (isPrimary) {
            primaryStatus = 1;
        }
        // Inserting to the local Db
        sqliteHelper.insertPaymentDetails(cardId,
                                        paymentCardModel.getCardNumber(),
                                        paymentCardModel.getCardType(),
                                        paymentCardModel.getCardExpiry(),
                                        primaryStatus);
        Toast.makeText(this, "Card Information Saved Successfully.", Toast.LENGTH_SHORT).show();
    }

    // Getting current user id
    public String getCurrentUserId() {
        return pref.getString(Constants.USER_ID, null);
    }


    // Fetching all cards
    public void fetchAllCards() {
        LoaderUtils.showProgressBar(PaymentCardsScreen.this, "Please wait while loading...");
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("payment_cards")
                .child(getCurrentUserId());
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                paymentCardModels = new ArrayList<>();
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    PaymentCardModel paymentCardModel =
                            noteDataSnapshot.getValue(PaymentCardModel.class);
                    paymentCardModels.add(paymentCardModel);
                }
                setUpAdapter();
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

    // Setting up adapter
    public void setUpAdapter() {
        if (paymentCardModels.size() > 0) {
            tvCardNotFound.setVisibility(TextView.GONE);
            paymentCardAdapter = new PaymentCardAdapter(PaymentCardsScreen.this, paymentCardModels);
            rcvPaymentCards.setAdapter(paymentCardAdapter);

        } else {
            tvCardNotFound.setVisibility(TextView.VISIBLE);
            paymentCardAdapter = new PaymentCardAdapter(PaymentCardsScreen.this, paymentCardModels);
            rcvPaymentCards.setAdapter(paymentCardAdapter);
        }
    }

}
