package com.example.it.beaconhack.APIs;

import com.example.it.beaconhack.Models.Base;

import retrofit.Call;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by diego on 21/11/15.
 */
public interface BeaconInfoApi {

    @FormUrlEncoded
    @POST("newEnpoint")
    Call<Base> beaconInfo(@Field("p1") int major, @Field("p2") int minor);
}
