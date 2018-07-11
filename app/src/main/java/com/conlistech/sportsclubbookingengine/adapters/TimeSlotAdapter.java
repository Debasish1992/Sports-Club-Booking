package com.conlistech.sportsclubbookingengine.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.activities.GameInfoScreen;
import com.conlistech.sportsclubbookingengine.models.PaymentCardModel;
import com.conlistech.sportsclubbookingengine.utils.Constants;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class TimeSlotAdapter extends
        RecyclerView.Adapter<TimeSlotAdapter.ViewHolder> {

    private ArrayList<String> mArrayList;
    Context context;
    DatabaseReference mDatabase;
    private ItemAdapter.ItemClickListener clickListener;
    TextView Summary, tvTitle;
    Button btnConfirm;
    public static ArrayList<String> refreshedTimeSlotArray;



    public TimeSlotAdapter(Context ctx, ArrayList<String>
            arrayList, TextView btnSummary, TextView titleTxt, Button buttonConfirm) {
        this.mArrayList = arrayList;
        this.context = ctx;
        this.Summary = btnSummary;
        this.tvTitle = titleTxt;
        this.btnConfirm = buttonConfirm;
        this.refreshedTimeSlotArray = arrayList;
    }


    @NonNull
    @Override
    public TimeSlotAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                         int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.row_time_slots, parent, false);
        return new TimeSlotAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        viewHolder.tvSlot.setText(mArrayList.get(position));

        viewHolder.cbSelection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int pos = position;
                // Getting time Slot
                String getTimeSlot = mArrayList.get(position);

                if (isChecked) {
                    // Getting the clicked Index
                    int indexClicked = getItemPos(getTimeSlot);

                    if(GameInfoScreen.indicesArray == null){
                        GameInfoScreen.indicesArray = new ArrayList<>();
                    }
                    GameInfoScreen.indicesArray.add(indexClicked);

                    if(Constants.bookingTimeSlots == null){
                        Constants.bookingTimeSlots = new ArrayList<>();
                    }
                    Constants.bookingTimeSlots.add(getTimeSlot);
                } else {
                    int indexClicked = getCategoryPos(getTimeSlot);
                    GameInfoScreen.indicesArray.remove(indexClicked);
                    Constants.bookingTimeSlots.remove(indexClicked);
                }

                // Calculating the total Amount
                int totalAmount = Integer.parseInt(Constants.venuePricing) *
                        Constants.bookingTimeSlots.size();
                Constants.venueTotalBookingPrice = String.valueOf(totalAmount);

                if (Constants.bookingTimeSlots.size() == 0) {
                    makeTheViwInVisible();
                } else if (Constants.bookingTimeSlots.size() == 1) {
                    makeTheViwVisible();
                    Summary.setText("You have selected " + Constants.bookingTimeSlots.toString() + " time slot and the total amount to be paid is $" + String.valueOf(totalAmount));
                } else {
                    makeTheViwVisible();
                    Summary.setText("You have selected " + Constants.bookingTimeSlots.toString() + " time slots, and the total amount to be paid is $" + String.valueOf(totalAmount));
                }
                Log.d("Time Slot", getTimeSlot);
            }
        });
    }

    // Making the views visible
    public void makeTheViwVisible() {
        Summary.setVisibility(TextView.VISIBLE);
        tvTitle.setVisibility(TextView.VISIBLE);
        btnConfirm.setVisibility(Button.VISIBLE);
    }

    // Making the views invisible
    public void makeTheViwInVisible() {
        Summary.setVisibility(TextView.GONE);
        tvTitle.setVisibility(TextView.GONE);
        btnConfirm.setVisibility(Button.GONE);
    }

    private int getCategoryPos(String category) {
        return Constants.bookingTimeSlots.indexOf(category);
    }

    private int getItemPos(String category) {
        return refreshedTimeSlotArray.indexOf(category);
    }


    @Override
    public int getItemCount() {
        return mArrayList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvSlot;
        CheckBox cbSelection;

        public ViewHolder(View view) {
            super(view);
            tvSlot = (TextView) view.findViewById(R.id.tvSlot);
            cbSelection = (CheckBox) view.findViewById(R.id.cbSelectTimeSlot);
            view.setTag(view);
        }
    }
}
