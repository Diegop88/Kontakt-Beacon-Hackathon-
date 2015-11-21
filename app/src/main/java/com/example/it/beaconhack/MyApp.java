package com.example.it.beaconhack;

import android.app.Application;

import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.log.LogLevel;

/**
 * Created by diego on 21/11/15.
 */
public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        KontaktSDK.initialize(this)
                .setDebugLoggingEnabled(BuildConfig.DEBUG)
                .setLogLevelEnabled(LogLevel.DEBUG, true);
    }
}
