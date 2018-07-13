package com.conlistech.sportsclubbookingengine.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.activities.GameInfoScreen;
import com.conlistech.sportsclubbookingengine.models.GameModel;
import com.conlistech.sportsclubbookingengine.models.UserModel;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class UpcomingGameAdapter
        extends RecyclerView.Adapter<UpcomingGameAdapter.ViewHolder> {

    public ArrayList<GameModel> gameModel;
    Context context;
    ArrayList<String> keyArray;
    DatabaseReference mDatabase;
    UpcomingGameAdapter.ItemClickListener clickListenerGames;
    String userIdCurrent;
    UserModel currentUserModel;


    public UpcomingGameAdapter(Context ctx, ArrayList<GameModel> gameModel, String currentUSerId) {
        this.gameModel = gameModel;
        this.context = ctx;
        this.userIdCurrent = currentUSerId;
    }

    @NonNull
    @Override
    public UpcomingGameAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                             int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.upcoming_game_row, parent, false);
        return new UpcomingGameAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tv_game_name.setText(gameModel.get(position).getGameName());
        holder.tvGameSports.setText(gameModel.get(position).getGameSport());
        String venueAddress = gameModel.get(position).getVenueInfoModel().getLocationModel().getAddress();
        holder.tv_venue_address.setText(gameModel.get(position).getVenueInfoModel().getLocationModel().getAddress());
        String gameCreatorUserId = gameModel.get(position).getGameCreatorUserId();
        if (userIdCurrent != null && userIdCurrent.equalsIgnoreCase(gameCreatorUserId)) {
            holder.tv_game_creator.setText("You" +
                    " have scheduled this game for " +
                    GameInfoScreen.getDate(Long.parseLong(gameModel.get(position).getGameDate())));
        } else {
            holder.tv_game_creator.setText(gameModel.get(position).getGameCreatorUserName() +
                    " has scheduled this game for " +
                    GameInfoScreen.getDate(Long.parseLong(gameModel.get(position).getGameDate())));
        }


        holder.tv_game_price.setText("$" + gameModel.get(position).getVenueInfoModel().getPrice());
        String getVenueImage = gameModel.get(position).getVenueInfoModel().getVenue_image();
        if (getVenueImage != null) {
            Picasso.get()
                    .load(getVenueImage)
                    .placeholder(R.drawable.default_loading)
                    .into(holder.ivVenueImage);
        }
    }

    // Function responsible for displaying the message
    public void displayMessage(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public int getItemCount() {
        return gameModel.size();
    }

    public void setClickListener(UpcomingGameAdapter.ItemClickListener itemClickListener) {
        this.clickListenerGames = itemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private TextView tv_game_name, tv_venue_address, tv_game_creator, tv_game_price, tvGameSports;
        ImageView ivVenueImage;

        public ViewHolder(View view) {
            super(view);
            tv_game_name = (TextView) view.findViewById(R.id.tvGameName);
            tv_venue_address = (TextView) view.findViewById(R.id.tvVenueaddress);
            tv_game_creator = (TextView) view.findViewById(R.id.tvGameCreator);
            tv_game_price = (TextView) view.findViewById(R.id.tvGamePrice);
            ivVenueImage = (ImageView) view.findViewById(R.id.ivVenueImage);
            tvGameSports = (TextView) view.findViewById(R.id.tvGameSports);
            view.setTag(view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (clickListenerGames != null) clickListenerGames.onClick(v, getAdapterPosition());
        }
    }

    public interface ItemClickListener {
        void onClick(View view, int position);
    }
}
