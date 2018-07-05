package com.conlistech.sportsclubbookingengine.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.models.PaymentCardModel;
import com.conlistech.sportsclubbookingengine.models.UserModel;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class PaymentCardAdapter extends
        RecyclerView.Adapter<PaymentCardAdapter.ViewHolder> {


    private ArrayList<PaymentCardModel> mArrayList;
    public static ArrayList<PaymentCardModel> mFilteredList;
    Context context;
    DatabaseReference mDatabase;
    private ItemAdapter.ItemClickListener clickListener;


    public PaymentCardAdapter(Context ctx, ArrayList<PaymentCardModel>
            arrayList) {
        this.mArrayList = arrayList;
        this.mFilteredList = arrayList;
        this.context = ctx;
    }


    @NonNull
    @Override
    public PaymentCardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                       int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.row_teammate, parent, false);
        return new PaymentCardAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        viewHolder.tv_name.setText(mFilteredList.get(position).getCardNumber());
        viewHolder.tv_fav_sports.setText(mFilteredList.get(position).getCardExpiry());

        boolean isPrimary = mFilteredList.get(position).isPrimary();

        if (isPrimary) {
            viewHolder.tvPrimary.setText("DEFAULT");
        }
        String cardType = mFilteredList.get(position).getCardType();

        if(cardType != null && cardType.equalsIgnoreCase("VISA")){
            viewHolder.ivProfileImage.setBackgroundResource(R.drawable.ic_visa);
        }else if (cardType != null && cardType.equalsIgnoreCase("MASTERCARD")){
            viewHolder.ivProfileImage.setBackgroundResource(R.drawable.ic_mastercard);
        }else if(cardType != null && cardType.equalsIgnoreCase("Maestro")){
            viewHolder.ivProfileImage.setBackgroundResource(R.drawable.ic_maestro);
        } else if(cardType != null && cardType.equalsIgnoreCase("JCB")){
            viewHolder.ivProfileImage.setBackgroundResource(R.drawable.ic_jcb);
        } else if(cardType != null && cardType.equalsIgnoreCase("Discover")){
            viewHolder.ivProfileImage.setBackgroundResource(R.drawable.ic_discover);
        } else if(cardType != null && cardType.equalsIgnoreCase("Diners Club")){
            viewHolder.ivProfileImage.setBackgroundResource(R.drawable.ic_diners_club);
        } else if(cardType != null && cardType.equalsIgnoreCase("AmEx")){
            viewHolder.ivProfileImage.setBackgroundResource(R.drawable.ic_american_express);
        }

    }

    @Override
    public int getItemCount() {
        return mFilteredList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_name, tv_fav_sports, tvPrimary;
        ImageView ivProfileImage;

        public ViewHolder(View view) {
            super(view);
            tv_name = (TextView) view.findViewById(R.id.tvName);
            tv_fav_sports = (TextView) view.findViewById(R.id.tvPrimarySport);
            tvPrimary = (TextView) view.findViewById(R.id.tvPrimary);
            ivProfileImage = (ImageView) view.findViewById(R.id.iv_profile_pic);
            view.setTag(view);
        }
    }

}
