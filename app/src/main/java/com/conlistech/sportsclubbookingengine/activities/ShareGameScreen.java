package com.conlistech.sportsclubbookingengine.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import com.conlistech.sportsclubbookingengine.R;
import com.conlistech.sportsclubbookingengine.utils.Constants;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ShareGameScreen extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tvDeepLinkShare)
    TextView tvDeepLink;
    @BindView(R.id.ivShare)
    ImageView ivShare;
    @BindView(R.id.btnGoback)
    Button btnGoBack;
    String getDeepLink;

    @OnClick(R.id.ivShare)
    void shareGame() {
        shareLink(getDeepLink/*"https://sportsclubbookingengine.page.link/81nF"*/);
    }

    @OnClick(R.id.btnGoback)
    void goBackHome() {
        onBackPressed();
    }

    public void shareLink(String deepLink) {
        String buildData = "Hi There, I have scheduled a game on Consports app. Click the below link to join my game. " + deepLink + ". Lets Play There. Happy Gaming.";
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "Join My Game on Conlis!");
            String sAux = buildData;
            //sAux = sAux + "https://play.google.com/store/apps/details?id=com.conlistech.qrattendanceuser \n\n";
            i.putExtra(Intent.EXTRA_TEXT, sAux);
            startActivity(Intent.createChooser(i, "Choose One"));
        } catch (Exception e) {
            //e.toString();
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_game_screen);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getDeepLink = generateDynamicLink();
        Log.d("DeepLink", getDeepLink);
        tvDeepLink.setText(getDeepLink);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ShareGameScreen.this.finish();
        GameInvitesScreen.finishAllScreens();
    }

    // Generating DeepLink
    public String generateDynamicLink() {
        DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://play.google.com/store/apps/details?id=com.conlistech.qrattendanceuser"))
                .setDynamicLinkDomain("sportsclubbookingengine.page.link")
                .setAndroidParameters(
                        new DynamicLink.AndroidParameters.Builder("com.conlistech.sportsclubbookingengine")
                                .build())
                .buildDynamicLink();

        Uri dynamicLinkUri = dynamicLink.getUri();
        return dynamicLinkUri.toString();
    }
}
