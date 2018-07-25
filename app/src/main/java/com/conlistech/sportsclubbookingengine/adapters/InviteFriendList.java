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
import android.widget.TextView;

import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.activities.GameInfoScreen;
import com.conlistech.sportsclubbookingengine.activities.GameInvitesScreen;
import com.conlistech.sportsclubbookingengine.models.UserModel;
import com.conlistech.sportsclubbookingengine.utils.Constants;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class InviteFriendList
        extends RecyclerView.Adapter<InviteFriendList.ViewHolder> {

    private ArrayList<UserModel> mArrayList;
    Context context;
    DatabaseReference mDatabase;


    public InviteFriendList(Context ctx,
                            ArrayList<UserModel> userModels) {
        this.mArrayList = userModels;
        this.context = ctx;

    }

    @NonNull
    @Override
    public InviteFriendList.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                          int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.row_invite_friends, parent, false);
        return new InviteFriendList.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        viewHolder.tvFullname.setText(mArrayList.get(position).getUserFullName());
        viewHolder.tvFavSport.setText(mArrayList.get(position).getFavSport());

        viewHolder.cbSelection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int pos = position;
                // Getting time Slot
                String getUserId = mArrayList.get(pos).getUserId();
                String getUserName = mArrayList.get(pos).getUserFullName();

                if (isChecked) {
                    // Getting the clicked Index
                    GameInvitesScreen.gameInvitedUserId.add(getUserId);
                    GameInvitesScreen.gameInvitedUserNames.add(getUserName);
                } else {
                    int indexClicked = getCategoryPos(getUserId);
                    GameInvitesScreen.gameInvitedUserId.remove(indexClicked);
                    GameInvitesScreen.gameInvitedUserNames.remove(indexClicked);
                }
            }
        });
    }


    private int getCategoryPos(String category) {
        return mArrayList.indexOf(category);
    }

    @Override
    public int getItemCount() {
        return mArrayList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvFullname, tvFavSport;
        CheckBox cbSelection;

        public ViewHolder(View view) {
            super(view);
            tvFullname = (TextView) view.findViewById(R.id.tvFullname);
            tvFavSport = (TextView) view.findViewById(R.id.tvFavSport);
            cbSelection = (CheckBox) view.findViewById(R.id.cbSelectTimeSlot);
            view.setTag(view);
        }


    }

}
