package com.example.it.beaconhack;

import android.app.Activity;
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
}
