package com.example.it.beaconhack.APIs;

import com.example.it.beaconhack.Models.Base;

import retrofit.Call;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by diego on 21/11/15.
 */
public interface LoginAPI {

    @FormUrlEncoded
    @POST("endPoint")
    Call<Base> login(@Field("p1") String p1);
}
