package com.example.it.beaconhack;

import android.app.Application;

import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.log.LogLevel;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Created by diego on 21/11/15.
 */
public class MyApp extends Application {

    private static Retrofit retrofit;

    @Override
    public void onCreate() {
        super.onCreate();

        KontaktSDK.initialize(this)
                .setDebugLoggingEnabled(BuildConfig.DEBUG)
                .setLogLevelEnabled(LogLevel.DEBUG, true);

        retrofit = new Retrofit.Builder()
                .baseUrl("http://10.20.0.55:6942/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static Retrofit getRetrofit() {
        return retrofit;
    }
}
