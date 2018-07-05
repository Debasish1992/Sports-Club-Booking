package com.conlistech.sportsclubbookingengine.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.models.UserModel;
import com.conlistech.sportsclubbookingengine.models.VenueInfoModel;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class VenueAdapter extends RecyclerView.Adapter<VenueAdapter.ViewHolder> {

    public static ArrayList<VenueInfoModel> mArrayList;
   // public static ArrayList<UserModel> mFilteredList;
    Context context;
    ArrayList<String> keyArray;
    DatabaseReference mDatabase;
    private VenueAdapter.ItemClickListener clickListener;
    String userIdCurrent;
    UserModel currentUserModel;


    public VenueAdapter(Context ctx, ArrayList<VenueInfoModel> venueModel) {
        this.mArrayList = venueModel;
        this.context = ctx;
    }


    @NonNull
    @Override
    public VenueAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                     int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.row_venues, parent, false);
        return new VenueAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        viewHolder.tv_venue_name.setText(mArrayList.get(position).getVenue_name());
        viewHolder.tv_venue_address.setText("$"+ mArrayList.get(position).getPrice() + "/hour");
        viewHolder.tv_venue_distance.setText("5.0 Miles");
        String getVenueImage = mArrayList.get(position).getVenue_image();
        if(getVenueImage != null){
            Picasso.get().load(getVenueImage).into(viewHolder.ivVenueImage);
        }
    }

    // Function responsible for refreshing the list
    public void refreshList(int position){
        mArrayList.remove(position);
        notifyDataSetChanged();
    }

    // Function responsible for displaying the message
    public void displayMessage(String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public int getItemCount() {
        return mArrayList.size();
    }

    public void setClickListener(VenueAdapter.ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView tv_venue_name, tv_venue_address, tv_venue_distance;
        ImageView ivVenueImage;
        RatingBar rbVenueRating;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            tv_venue_name = (TextView) view.findViewById(R.id.tvVenueName);
            tv_venue_address = (TextView) view.findViewById(R.id.tvAddress);
            tv_venue_distance = (TextView) view.findViewById(R.id.tvDistance);
            ivVenueImage = (ImageView) view.findViewById(R.id.ivVenueImage);
            rbVenueRating = (RatingBar) view.findViewById(R.id.rbVenue);
            view.setTag(view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null) clickListener.onClick(v, getAdapterPosition());
        }
    }

    public interface ItemClickListener {
        void onClick(View view, int position);
    }
}
