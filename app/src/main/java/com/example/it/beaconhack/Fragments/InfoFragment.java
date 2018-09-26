package com.example.it.beaconhack.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.it.beaconhack.APIs.BeaconInfoApi;
import com.example.it.beaconhack.ActivityListener;
import com.example.it.beaconhack.Models.Base;
import com.example.it.beaconhack.MyApp;
import com.example.it.beaconhack.R;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;

import java.util.concurrent.TimeoutException;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by diego on 22/11/15.
 */
public class InfoFragment extends Fragment {
    IBeaconDevice iBeaconDevice;
    private ActivityListener mListener;

    public void setBeacon(IBeaconDevice beacon) {
        iBeaconDevice = beacon;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BeaconInfoApi api = MyApp.getRetrofit().create(BeaconInfoApi.class);
        Call<Base> call = api.beaconInfo(iBeaconDevice.getMajor(), iBeaconDevice.getMinor());
        call.enqueue(new Callback<Base>() {
            @Override
            public void onResponse(Response<Base> response, Retrofit retrofit) {

            }

            @Override
            public void onFailure(Throwable t) {

            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.info_beacon, container, false);

        view.findViewById(R.id.bt_aceptar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.showLoading();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.bt_aceptar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.showLoading();
            }
        });

        TextView major = (TextView) view.findViewById(R.id.major);
        major.append(iBeaconDevice != null ? String.valueOf(iBeaconDevice.getMajor()) : null);

        TextView minor = (TextView) view.findViewById(R.id.minor);
        minor.append(iBeaconDevice != null ? String.valueOf(iBeaconDevice.getMinor()) : null);

    }

    public IBeaconDevice getIbeacon() {
        return iBeaconDevice;
    }

    public void setListener(ActivityListener l) {
        mListener = l;
    }
}
