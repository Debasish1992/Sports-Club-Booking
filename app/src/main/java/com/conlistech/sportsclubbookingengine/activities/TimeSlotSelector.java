package com.conlistech.sportsclubbookingengine.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.utils.Constants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TimeSlotSelector extends AppCompatActivity {


    @BindView(R.id.calendarView)
    com.applandeo.materialcalendarview.CalendarView calenderView;
    public static String formattedDate = null;
    public static TimeSlotSelector timeSlotSelector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_slot_selector);
        ButterKnife.bind(this);
        timeSlotSelector = this;

        calenderView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                String date = eventDay.getCalendar().getTime().toString();
                formattedDate = formatDate(date);
                Intent intent = new Intent(TimeSlotSelector.this, SelectTimeSlot.class);
                startActivity(intent);
            }
        });

    }

    public String formatDate(String formatDate){
        String formattedDate = null;
        try{
            //Mon Jul 02 12:38:35 GMT+05:30 2018
            DateFormat df1 = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy"); // for parsing input
            DateFormat df2 = new SimpleDateFormat("d-MM-yyyy");  // for formatting output
            Date d = df1.parse(formatDate);

            formattedDate = df2.format(d);
            Constants.gameScheduledDate = formattedDate;
            Log.d("Formatted date", formattedDate);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return formattedDate;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ShareGameScreen.releaseAllValues();
    }
}
