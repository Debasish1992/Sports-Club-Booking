package com.conlistech.sportsclubbookingengine.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.utils.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GameInfoScreen extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.input_name)
    EditText etGameName;
    @BindView(R.id.input_playerCount)
    EditText etPlayerCount;
    @BindView(R.id.input_gameNote)
    EditText etGameNotes;
    @BindView(R.id.btnSubmit)
    Button btnCreateGame;

    String gameName, gamePlayerCount, gameNote;

    @OnClick (R.id.btnSubmit) void createGame(){
        boolean isValid = validateUserEntries();

        if(isValid){
            Constants.gameName = gameName;
            Constants.maximumNoPlayers = gamePlayerCount;
            Constants.gameNotes = gameNote;
            Toast.makeText(this, "User entries are valid.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_info_screen);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    // Validating user inputs
    public boolean validateUserEntries(){
        boolean valid = false;
        gameName = etGameName.getText().toString().trim();
        gamePlayerCount = etPlayerCount.getText().toString().trim();
        gameNote = etGameNotes.getText().toString().trim();

        if(TextUtils.isEmpty(gameName)){
            Toast.makeText(this, "Please enter name of your game.", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(gamePlayerCount)){
            Toast.makeText(this, "Please enter the player count", Toast.LENGTH_SHORT).show();
        } else if(Integer.parseInt(gamePlayerCount) == 0){
            Toast.makeText(this, "Please enter a valid player count", Toast.LENGTH_SHORT).show();
        } else {
            valid = true;
        }
        return valid;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
