package com.conlistech.sportsclubbookingengine.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.activities.TeammatesScreen;
import com.conlistech.sportsclubbookingengine.database.SqliteHelper;
import com.conlistech.sportsclubbookingengine.models.FriendModel;
import com.conlistech.sportsclubbookingengine.models.UserModel;
import com.conlistech.sportsclubbookingengine.utils.Constants;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class TeammatesRequestAdapter extends
        RecyclerView.Adapter<TeammatesRequestAdapter.ViewHolder> {

    public static ArrayList<FriendModel> mArrayList;
   // public static ArrayList<UserModel> mFilteredList;
    Context context;
    ArrayList<String> keyArray;
    DatabaseReference mDatabase;
    private TeammatesRequestAdapter.ItemClickListener clickListener;
    String userIdCurrent;
    FriendModel currentUserModel;


    public TeammatesRequestAdapter(Context ctx, ArrayList<FriendModel>
            arrayList, ArrayList<String> nodeArray, String userId, FriendModel userModel) {
        this.mArrayList = arrayList;
       // this.mFilteredList = arrayList;
        this.keyArray = nodeArray;
        this.context = ctx;
        this.userIdCurrent = userId;
        this.currentUserModel = userModel;
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
    public TeammatesRequestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                     int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.row_teammate_requests, parent, false);
        return new TeammatesRequestAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        viewHolder.tv_name.setText(mArrayList.get(position).getUserFullName());
        viewHolder.tv_fav_sports.setText(mArrayList.get(position).getFavSport());
        String userProfilePic = mArrayList.get(position).getUserProfileImage();
        if (!TextUtils.isEmpty(userProfilePic)) {
            Picasso.get()
                    .load(userProfilePic)
                    .into(viewHolder.ivProfileImage);
        }

        viewHolder.btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = position;
                String getKey = keyArray.get(pos);
                String getUserId = mArrayList.get(pos).getUserId();

                // Accept User Friend Request
                removeUserTeammateRequest(getUserId);

                DatabaseReference mDatabaseTeammate =
                        FirebaseDatabase.getInstance().getReference("teammates")
                                .child("my_teamates").child(userIdCurrent).child(getKey);
                mDatabaseTeammate.setValue(mArrayList.get(pos));

                DatabaseReference mDatabaseTeammate_Added =
                        FirebaseDatabase.getInstance().getReference("teammates")
                                .child("my_teamates").child(getUserId).child(userIdCurrent);

                currentUserModel.setFriendUserId(userIdCurrent);
                mDatabaseTeammate_Added.setValue(currentUserModel);

                // Inserting user details into local database
                SqliteHelper sqliteHelper = new SqliteHelper(context);
                sqliteHelper.insertTeammates(mArrayList.get(pos).getUserId(),
                        mArrayList.get(pos).getUserFullName());

                // Refreshing the view
                refreshList(pos);

                displayMessage("Friend Request Successfully Accepted");

                TeammatesScreen.isRequestResponded = true;

            }
        });

        viewHolder.btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = position;
                String getKey = keyArray.get(pos);
                // Removing user friend request
                removeUserTeammateRequest(getKey);
                // refreshing the list
                refreshList(pos);
                // Displaying the message
                displayMessage("Friend Request Successfully Rejected");

                TeammatesScreen.isRequestResponded = true;
            }
        });
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

    // Function responsible for accepting user friend request
    public void removeUserTeammateRequest(String key){
        DatabaseReference mDatabase =
                FirebaseDatabase.getInstance().getReference("teammates")
                        .child("teammate_request").child(userIdCurrent).child(key);
        mDatabase.removeValue();
    }


    @Override
    public int getItemCount() {
        return mArrayList.size();
    }

    public void setClickListener(TeammatesRequestAdapter.ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView tv_name, tv_fav_sports;
        ImageView ivProfileImage;
        Button btnAccept, btnReject;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            tv_name = (TextView) view.findViewById(R.id.tvName);
            tv_fav_sports = (TextView) view.findViewById(R.id.tvPrimarySport);
            ivProfileImage = (ImageView) view.findViewById(R.id.iv_profile_pic);
            btnAccept = (Button) view.findViewById(R.id.btnAccept);
            btnReject = (Button) view.findViewById(R.id.btnDecline);
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
