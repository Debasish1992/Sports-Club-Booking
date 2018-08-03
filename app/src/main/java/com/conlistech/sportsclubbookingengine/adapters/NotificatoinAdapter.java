package com.conlistech.sportsclubbookingengine.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.interfaces.ItemClickListener;
import com.conlistech.sportsclubbookingengine.models.NotificationModel;
import com.conlistech.sportsclubbookingengine.models.UserConversation;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

import static com.conlistech.sportsclubbookingengine.adapters.ChatMessageAdapter.convertSecondsToHMmSs;

public class NotificatoinAdapter extends RecyclerView.Adapter<NotificatoinAdapter.ViewHolder> {

    public ArrayList<NotificationModel> mArrayList;
    Context context;
    DatabaseReference mDatabase;
    private ItemClickListener mClickListener;
    SpannableStringBuilder builder = new SpannableStringBuilder();


    public NotificatoinAdapter(Context ctx,
                               ArrayList<NotificationModel> userModels) {
        this.mArrayList = userModels;
        this.context = ctx;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.row_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        //  viewHolder.tvFullname.setText(mArrayList.get(position).getUserFullName());

        String str = mArrayList.get(position).getSenderFullName() + " " +
                mArrayList.get(position).getNotifyMessage();
        str = str.replace("" + mArrayList.get(position).getSenderFullName(),
                "<b><font color='#424242'>" + mArrayList.get(position).getSenderFullName() + "</font></b>");
        viewHolder.tvMessage.setText(Html.fromHtml(str));
        viewHolder.tvTimeStamp.setText(convertSecondsToHMmSs(
                Long.parseLong(mArrayList.get(position).getNotifyTime())));
    }

   /* public String getColoredStringText(String str) {
        SpannableString str1 = new SpannableString(str);
        str1.setSpan(new ForegroundColorSpan(Color.RED), 0, str1.length(), 0);
        builder.append(str1);
        return builder;
    }*/


    private int getCategoryPos(String category) {
        return mArrayList.indexOf(category);
    }

    @Override
    public int getItemCount() {
        return mArrayList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView tvFullname, tvMessage, tvTimeStamp;
        private View mView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            tvFullname = (TextView) view.findViewById(R.id.tvFullname);
            tvMessage = (TextView) view.findViewById(R.id.tvMessage);
            tvTimeStamp = (TextView) view.findViewById(R.id.tvTimeStamp);
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

}


