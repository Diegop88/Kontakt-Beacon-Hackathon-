package com.example.it.beaconhack;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * Created by it on 22/11/15.
 */
public class MarketingActivity extends Activity {
    private ImageView imMarketing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.marketing_layout);

        Bundle extras = getIntent().getExtras();
        if( extras != null ){
            imMarketing = (ImageView) findViewById(R.id.im_marketing);
            Glide.with(MarketingActivity.this).load(extras.getString("image")).into(imMarketing);


        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pushMainActivityIntoStack();
    }

    //push the main activity down the stack, so when user quits the View Ad activity, it goes back to the main
    private void pushMainActivityIntoStack() {
        Intent mainActivity = new Intent(this, Home.class);
        mainActivity.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainActivity);
    }
}
