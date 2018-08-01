package com.conlistech.sportsclubbookingengine.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.models.UserModel;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ItemAdapter extends
        RecyclerView.Adapter<ItemAdapter.ViewHolder>
        implements Filterable {

    private ArrayList<UserModel> mArrayList;
    public static ArrayList<UserModel> mFilteredList;
    Context context;
    DatabaseReference mDatabase;
    private ItemClickListener clickListener;


    public ItemAdapter(Context ctx, ArrayList<UserModel>
            arrayList) {
        this.mArrayList = arrayList;
        this.mFilteredList = arrayList;
        this.context = ctx;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @NonNull
    @Override
    public ItemAdapter.ViewHolder
    onCreateViewHolder(@NonNull ViewGroup parent,
                       int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.row_teammate, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemAdapter.ViewHolder viewHolder,
                                 final int position) {
        viewHolder.tv_name.setText(mFilteredList.get(position).getUserFullName());
        viewHolder.tv_fav_sports.setText(mFilteredList.get(position).getFavSport());
        String userProfilePic = mFilteredList.get(position).getUserProfileImage();
        if (!TextUtils.isEmpty(userProfilePic)) {
            Picasso.get()
                    .load(userProfilePic)
                    .into(viewHolder.ivProfileImage);
        }
    }

    @Override
    public int getItemCount() {
        return mFilteredList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    mFilteredList = mArrayList;
                } else {
                    ArrayList<UserModel> filteredList = new ArrayList<>();
                    for (UserModel guitarDetails : mArrayList) {
                        if (guitarDetails.getUserFullName().toLowerCase().contains(charString) ||
                                guitarDetails.getUserFullName().toUpperCase().contains(charString) ||
                                guitarDetails.getFavSport().toUpperCase().contains(charString) ||
                                guitarDetails.getFavSport().toLowerCase().contains(charString) ||
                                guitarDetails.getUserPhoneNumber().contains(charString) ||
                                guitarDetails.getUserEmail().toUpperCase().contains(charString) ||
                                guitarDetails.getUserEmail().toLowerCase().contains(charString)) {
                            filteredList.add(guitarDetails);
                            Log.d("Filtered Values are", filteredList.toString());
                        }
                    }
                    mFilteredList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mFilteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mFilteredList = (ArrayList<UserModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView tv_name, tv_fav_sports;
        ImageView ivProfileImage;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            tv_name = (TextView) view.findViewById(R.id.tvName);
            tv_fav_sports = (TextView) view.findViewById(R.id.tvPrimarySport);
            ivProfileImage = (ImageView) view.findViewById(R.id.iv_profile_pic);
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
