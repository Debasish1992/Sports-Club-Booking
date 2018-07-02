package com.conlistech.sportsclubbookingengine.activities;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.models.PaymentCardModel;
import com.conlistech.sportsclubbookingengine.models.UserModel;
import com.conlistech.sportsclubbookingengine.models.VenueInfoModel;
import com.conlistech.sportsclubbookingengine.models.VenueReviewModel;
import com.conlistech.sportsclubbookingengine.utils.Constants;
import com.conlistech.sportsclubbookingengine.utils.LoaderUtils;
import com.conlistech.sportsclubbookingengine.utils.RandomNumberGenerator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DetailsScreen extends AppCompatActivity implements RatingDialogListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.imgVenue)
    ImageView ivVenueImage;
    @BindView(R.id.tvVenueName)
    TextView tvVenueName;
    @BindView(R.id.tvVenueAddress)
    TextView tvVenueAddress;
    @BindView(R.id.tvVenueOfferingsValues)
    TextView tvVenueOffering;
    @BindView(R.id.tvVenueAmenitiesValues)
    TextView tvVenueAmenities;
    @BindView(R.id.tvVenuePhone)
    TextView tvVenuePhone;
    @BindView(R.id.tvVenueWebsite)
    TextView tvVenueWebsite;
    @BindView(R.id.btnBookSlot)
    Button btnBookSlot;
    @BindView(R.id.rbVenueRating)
    RatingBar tbVenueRating;
    @BindView(R.id.tvVenueEquipmentsValues)
    TextView tvVenueEquipments;
    @BindView(R.id.layVenueRating)
    RelativeLayout layRatings;
    SharedPreferences pref;
    VenueInfoModel venueInfoModel;

    @OnClick(R.id.layVenueRating)
    void Ratimgs() {
        //Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show();

        showDialog();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_screen);
        ButterKnife.bind(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        pref = getSharedPreferences("MyPref", MODE_PRIVATE);

        // Fetching all the details
        fetchVenueDetails();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Getting all the details of the venue
    public void fetchVenueDetails() {
        LoaderUtils.showProgressBar(DetailsScreen.this, "Please wait while loading...");
        DatabaseReference mDatabase =
                FirebaseDatabase.getInstance().getReference("venues")
                        .child(LandingScreen.venueId);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                VenueInfoModel venueInfoModel = dataSnapshot.getValue(VenueInfoModel.class);
                setDataInView(venueInfoModel);
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

    /**
     * Function Responsible for setting the data
     */
    public void setDataInView(VenueInfoModel venueInfoModel) {
        if (venueInfoModel != null) {
            String venueImage = venueInfoModel.getVenue_image();

            if (venueImage != null) {
                Picasso.get().load(venueImage).into(ivVenueImage);
            }
            tvVenueName.setText(venueInfoModel.getVenue_name());
            tvVenueAddress.setText(venueInfoModel.getVenueDesc());
            tvVenueAmenities.setText(removeSpecialChars(venueInfoModel.getAmenities()));
            tvVenueOffering.setText(removeSpecialChars(venueInfoModel.getVenue_offers().toString()));
            tvVenuePhone.setText("Phone Number - " + venueInfoModel.getVenue_phone());
            tvVenueWebsite.setText("WebSite - " + venueInfoModel.getVenue_website());
            tvVenueEquipments.setText(venueInfoModel.getVenue_equipments());
            toolbar.setTitle(venueInfoModel.getVenue_name());
        }
    }

    /**
     * Function responsible for removing the special chars
     *
     * @param value
     * @return
     */
    public String removeSpecialChars(String value) {
        if (value != null) {
            value = value.replace("_", "");
            value = value.replace("|", ",");
        }
        return value;
    }

    private void showDialog() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNeutralButtonText("Later")
                .setNoteDescriptions(Arrays.asList("Very Bad", "Not good", "Quite ok", "Very Good", "Excellent !!!"))
                .setDefaultRating(3)
                .setTitle("Please Rate this Venue")
                .setDescription("Please select some stars and give your feedback")
                .setDefaultComment("This venue is pretty cool to have Games!")
                .setStarColor(R.color.md_blue_300)
                .setNoteDescriptionTextColor(R.color.md_blue_grey_300)
                .setTitleTextColor(R.color.md_grey_600)
                .setDescriptionTextColor(R.color.md_grey_400)
                .setHint("Please write your comment here ...")
                .setHintTextColor(R.color.black)
                .setCommentTextColor(R.color.black)
                .setCommentBackgroundColor(R.color.md_grey_200)
                .setWindowAnimation(R.style.MyDialogFadeAnimation)
                .create(DetailsScreen.this)
                .show();
    }

    @Override
    public void onPositiveButtonClicked(int rate, String s) {
       // Toast.makeText(this, String.valueOf(rate), Toast.LENGTH_SHORT).show();

        VenueReviewModel venueReviewModel = new VenueReviewModel();
        venueReviewModel.setUserId(getCurrentUserId());
        venueReviewModel.setUsrName(getCurrentUserName());
        venueReviewModel.setReviewDesc(s.toString());
        venueReviewModel.setRatings(String.valueOf(rate));

        // Storing Venue Reviews
        storeRatings(venueReviewModel);
    }

    @Override
    public void onNegativeButtonClicked() {

    }

    @Override
    public void onNeutralButtonClicked() {

    }


    // Getting current User Id
    public String getCurrentUserId() {
        return pref.getString(Constants.USER_ID, null);
    }

    public String getCurrentUserName() {
        return pref.getString(Constants.USER_FULL_NAME, null);
    }



    // Storing Venue Ratings
    public void storeRatings(VenueReviewModel venueReviewModel) {

        // Saving the review data
        DatabaseReference mDatabaseReviews =
                FirebaseDatabase.getInstance().getReference("venues")
                        .child(LandingScreen.venueId).child("Reviews")
                        .child(String.valueOf(RandomNumberGenerator.getRandomNumber()));
        mDatabaseReviews.setValue(venueReviewModel);

        // Saving the review data
        DatabaseReference mDatabaseReviewsCount =
                FirebaseDatabase.getInstance().getReference("venues")
                        .child(LandingScreen.venueId);
        mDatabaseReviewsCount.child("total_reviews").setValue(venueReviewModel);

        Toast.makeText(this, "Venue Review Saved Successfully.", Toast.LENGTH_SHORT).show();
    }
}
