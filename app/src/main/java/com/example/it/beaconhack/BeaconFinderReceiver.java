package com.example.it.beaconhack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by diego on 21/11/15.
 */
public class BeaconFinderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startActivity(new Intent(context, FinderBeacon.class));
    }
}
