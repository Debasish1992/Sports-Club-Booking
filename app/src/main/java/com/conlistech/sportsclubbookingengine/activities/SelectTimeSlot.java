package com.conlistech.sportsclubbookingengine.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.adapters.ItemAdapter;
import com.conlistech.sportsclubbookingengine.adapters.TimeSlotAdapter;
import com.conlistech.sportsclubbookingengine.models.PaymentCardModel;
import com.conlistech.sportsclubbookingengine.models.TImeSlotModel;
import com.conlistech.sportsclubbookingengine.utils.LoaderUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SelectTimeSlot extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rcv_time_slots)
    RecyclerView rcv_time_slots;
    @BindView(R.id.ttText)
    TextView titleTv;
    @BindView(R.id.summaryText)
    TextView tvSummaryText;
    @BindView(R.id.btnConfirm)
    Button btnConfirm;
    String pattern = "dd-MM-yyyy";
    ArrayList<String> timeSlotModelArr;
    TimeSlotAdapter timeSlotAdapter;
    public static SelectTimeSlot selectTimeSlot;

    @OnClick(R.id.btnConfirm)
    void redirectToGameInfoPage() {
        startActivity(new Intent(SelectTimeSlot.this, GameInfoScreen.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_time_slot);
        ButterKnife.bind(this);
        titleTv.setVisibility(TextView.GONE);
        btnConfirm.setVisibility(Button.GONE);
        selectTimeSlot = this;

        initViews();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Fetching all the Slots details
        fetchAllSlots();
    }

    // Setting up the adapter
    public void setUpAdapter() {
        if (timeSlotModelArr.size() > 0) {
            timeSlotAdapter = new TimeSlotAdapter(SelectTimeSlot.this,
                    timeSlotModelArr, tvSummaryText, titleTv, btnConfirm);
            rcv_time_slots.setAdapter(timeSlotAdapter);
        } else {
            Toast.makeText(SelectTimeSlot.this, "No Teammates Found", Toast.LENGTH_LONG).show();
            timeSlotAdapter = new TimeSlotAdapter(SelectTimeSlot.this,
                    timeSlotModelArr, tvSummaryText, titleTv, btnConfirm);
            rcv_time_slots.setAdapter(timeSlotAdapter);
        }
    }


    // Initializing the views
    private void initViews() {
        rcv_time_slots.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rcv_time_slots.setLayoutManager(layoutManager);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Fetching all cards
    public void fetchAllSlots() {
        timeSlotModelArr = new ArrayList<>();
        LoaderUtils.showProgressBar(SelectTimeSlot.this, "Please wait while loading...");
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("venues")
                .child(LandingScreen.venueId).child("time_slots").child(TimeSlotSelector.formattedDate);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    Log.d("Value", noteDataSnapshot.getKey());

                    String timeSlotData = noteDataSnapshot.getValue(String.class);
                    timeSlotModelArr.add(timeSlotData);
                    // Getting user id from model
                    //timeSlotModelArr.add(timeSlotModel);
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

    // Getting current Date
    public String getCurrentDate() {
        String dateInString = new SimpleDateFormat(pattern).format(new Date());
        return dateInString;
    }
}
