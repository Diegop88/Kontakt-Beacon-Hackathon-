package com.example.it.beaconhack;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.it.beaconhack.Fragments.InfoFragment;
import com.example.it.beaconhack.Fragments.LoadingFragment;
import com.kontakt.sdk.android.ble.configuration.ActivityCheckConfiguration;
import com.kontakt.sdk.android.ble.configuration.ForceScanConfiguration;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.configuration.scan.IBeaconScanContext;
import com.kontakt.sdk.android.ble.configuration.scan.ScanContext;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.discovery.BluetoothDeviceEvent;
import com.kontakt.sdk.android.ble.discovery.DistanceSort;
import com.kontakt.sdk.android.ble.discovery.EventType;
import com.kontakt.sdk.android.ble.discovery.ibeacon.IBeaconDeviceEvent;
import com.kontakt.sdk.android.ble.filter.ibeacon.IBeaconFilters;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.rssi.RssiCalculators;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by diego on 21/11/15.
 */
public class Home extends AppCompatActivity implements ProximityManager.ProximityListener{

    private ProximityManager proximityManager;
    private ScanContext scanContext;

    private View coordinator;
    private IBeaconDevice keepBeacon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        coordinator = findViewById(R.id.coordinator);

        proximityManager = new ProximityManager(this);

        IBeaconScanContext iBeaconScanContext = new IBeaconScanContext.Builder()
                .setEventTypes(Arrays.asList(
                        EventType.SPACE_ENTERED,
                        EventType.DEVICE_DISCOVERED,
                        EventType.DEVICES_UPDATE,
                        EventType.DEVICE_LOST,
                        EventType.SPACE_ABANDONED
                ))
                .setIBeaconFilters(Collections.singletonList(
                        IBeaconFilters.newProximityUUIDFilter(UUID.fromString("945765bc-90b1-11e5-8994-feff819cdc9f"))
                ))
                .setRssiCalculator(RssiCalculators.newLimitedMeanRssiCalculator(5))
                .setDevicesUpdateCallbackInterval(TimeUnit.SECONDS.toMillis(2))
                .setDistanceSort(DistanceSort.ASC)
                .build();

        scanContext = new ScanContext.Builder()
                .setScanMode(ProximityManager.SCAN_MODE_BALANCED)
                .setIBeaconScanContext(iBeaconScanContext)
                .setActivityCheckConfiguration(ActivityCheckConfiguration.DEFAULT)
                .setForceScanConfiguration(ForceScanConfiguration.DEFAULT)
                .setScanPeriod(new ScanPeriod(TimeUnit.SECONDS.toMillis(3), TimeUnit.SECONDS.toMillis(20)))
                .build();
    }

    @Override
    protected void onPause() {
        super.onPause();
        proximityManager.finishScan();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initializeScan();
    }

    private void initializeScan() {
        showLoading();

        proximityManager.initializeScan(scanContext, new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                proximityManager.attachListener(Home.this);
            }

            @Override
            public void onConnectionFailure() {
                Snackbar.make(coordinator, R.string.scanFailure, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading() {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new LoadingFragment()).commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        proximityManager.disconnect();
        proximityManager = null;
    }

    @Override
    public void onScanStart() {
    }

    @Override
    public void onScanStop() {
    }

    @Override
    public void onEvent(BluetoothDeviceEvent event) {
        IBeaconDeviceEvent events = (IBeaconDeviceEvent) event;
        List<IBeaconDevice> deviceList = events.getDeviceList();

        if(getSupportFragmentManager().findFragmentByTag("Info") instanceof InfoFragment)
            showSnackBar(deviceList);
        else
            showInfo(deviceList);
    }

    private void showInfo(List<IBeaconDevice> iBeaconDevice) {
        IBeaconDevice closer = null;

        for (IBeaconDevice device : iBeaconDevice){
            if(device != null){
                if (closer == null)
                    closer = device;

                if(closer.getDistance() >= device.getDistance()) {
                    closer = device;
                    showCloserInfo(closer);
                }
            }
        }
    }

    private void showCloserInfo(IBeaconDevice beaconDevice) {
        InfoFragment fragment = new InfoFragment();
        fragment.setBeacon(beaconDevice);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment, "Info").commit();
    }

    private void showSnackBar(List<IBeaconDevice> iBeaconDevice) {
        for(IBeaconDevice device : iBeaconDevice){
            if(keepBeacon == null)
                keepBeacon = device;

            if(keepBeacon.getDistance() >= device.getDistance() && keepBeacon.getUniqueId() != device.getUniqueId()) {
                keepBeacon = device;

                if(((InfoFragment) getSupportFragmentManager().findFragmentByTag("Info")).getIbeacon().getUniqueId() == keepBeacon.getUniqueId())
                    return;

                Snackbar.make(coordinator, "Hay nueva información " + device.getMajor() + ", " + device.getMinor(),Snackbar.LENGTH_INDEFINITE)
                        .setAction("Ver", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showCloserInfo(keepBeacon);
                            }
                        })
                        .show();
            } else
                continue;
        }
    }
}
