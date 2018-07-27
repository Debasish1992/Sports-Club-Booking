package com.conlistech.sportsclubbookingengine.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;


import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.models.UserConversation;
import com.conlistech.sportsclubbookingengine.models.UserModel;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class RecentChatListAdapter extends RecyclerView.Adapter<RecentChatListAdapter.ViewHolder> {

    public ArrayList<UserConversation> mArrayList;
    Context context;
    DatabaseReference mDatabase;
    private ItemClickListener mClickListener;


    public RecentChatListAdapter(Context ctx,
                                 ArrayList<UserConversation> userModels) {
        this.mArrayList = userModels;
        this.context = ctx;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.row_recent_chats, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        viewHolder.tvFullname.setText(mArrayList.get(position).getUserFullName());
        viewHolder.tvMessage.setText(mArrayList.get(position).getReceiverLastMsg());
        //   viewHolder.tvFavSport.setText(mArrayList.get(position).getFavSport());

       /* if (position == 1 || position == 4) {
            viewHolder.tvUnreadMsgs.setVisibility(View.VISIBLE);
        } else {
            viewHolder.tvUnreadMsgs.setVisibility(View.GONE);
        }

        if (position % 2 == 0) {
            viewHolder.mImgOnline.setImageResource(R.drawable.circle_small_green);
        } else {
            viewHolder.mImgOnline.setImageResource(R.drawable.circle_small_grey);
        }*/

        /*viewHolder.cbSelection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
        });*/
    }


    private int getCategoryPos(String category) {
        return mArrayList.indexOf(category);
    }

    @Override
    public int getItemCount() {
        return mArrayList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView tvFullname, tvMessage, tvUnreadMsgs;
        CheckBox cbSelection;
        ImageView mImgOnline;
        private View mView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            tvFullname = (TextView) view.findViewById(R.id.tvFullname);
            tvMessage = (TextView) view.findViewById(R.id.tvMessage);
            tvUnreadMsgs = (TextView) view.findViewById(R.id.tvUnreadMsgs);
            mImgOnline = view.findViewById(R.id.img_online);
            //  cbSelection = (CheckBox) view.findViewById(R.id.cbSelectTimeSlot);
            view.setOnClickListener(this);
            view.setTag(view);
        }

        @Override
        public void onClick(View v) {
            if (mClickListener != null) mClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}

