package com.conlistech.sportsclubbookingengine.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.activities.GameInvitesScreen;
import com.conlistech.sportsclubbookingengine.models.UserModel;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class GamePlayedAdapter extends RecyclerView.Adapter<GamePlayedAdapter.ViewHolder> {

    private ArrayList<UserModel> mArrayList;
    Context context;
    DatabaseReference mDatabase;

    public GamePlayedAdapter(Context ctx,
                             ArrayList<UserModel> userModels) {
        this.mArrayList = userModels;
        this.context = ctx;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.row_teammate, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        viewHolder.tvVenue.setVisibility(View.VISIBLE);
        viewHolder.tvGameName.setText(mArrayList.get(position).getUserFullName());
        // viewHolder.tvVenue.setText(mArrayList.get(position).getUserFullName());
        viewHolder.tvFavSport.setText(mArrayList.get(position).getFavSport());
    }

    private int getCategoryPos(String category) {
        return mArrayList.indexOf(category);
    }

    @Override
    public int getItemCount() {
        return mArrayList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvGameName, tvFavSport, tvVenue;
        private ImageView mImgProfile;

        public ViewHolder(View view) {
            super(view);
            tvGameName = view.findViewById(R.id.tvName);
            tvFavSport = view.findViewById(R.id.tvPrimarySport);
            tvVenue = view.findViewById(R.id.tvVenue);
            mImgProfile = view.findViewById(R.id.iv_profile_pic);
            view.setTag(view);
        }


    }

}
