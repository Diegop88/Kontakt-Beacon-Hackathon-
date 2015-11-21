package com.example.it.beaconhack;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.kontakt.sdk.android.ble.configuration.ActivityCheckConfiguration;
import com.kontakt.sdk.android.ble.configuration.ForceScanConfiguration;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.configuration.scan.EddystoneScanContext;
import com.kontakt.sdk.android.ble.configuration.scan.IBeaconScanContext;
import com.kontakt.sdk.android.ble.configuration.scan.ScanContext;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.discovery.BluetoothDeviceEvent;
import com.kontakt.sdk.android.ble.discovery.EventType;
import com.kontakt.sdk.android.ble.discovery.eddystone.EddystoneDeviceEvent;
import com.kontakt.sdk.android.ble.discovery.ibeacon.IBeaconAdvertisingPacket;
import com.kontakt.sdk.android.ble.discovery.ibeacon.IBeaconDeviceEvent;
import com.kontakt.sdk.android.ble.filter.eddystone.EddystoneFilters;
import com.kontakt.sdk.android.ble.filter.ibeacon.IBeaconFilter;
import com.kontakt.sdk.android.ble.filter.ibeacon.IBeaconFilters;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.rssi.RssiCalculator;
import com.kontakt.sdk.android.ble.rssi.RssiCalculators;
import com.kontakt.sdk.android.ble.util.BluetoothUtils;
import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.Proximity;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;
import com.kontakt.sdk.android.common.profile.IEddystoneNamespace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by diego on 21/11/15.
 */
public class FinderBeacon extends Service implements ProximityManager.ProximityListener {

    static String TAG = FinderBeacon.class.getSimpleName();

    ProximityManager proximityManager;
    ScanContext scanContext;

    @Override
    public void onCreate() {
        super.onCreate();

        proximityManager = new ProximityManager(this);

        IBeaconScanContext iBeaconScanContext = new IBeaconScanContext.Builder()
                .setEventTypes(EnumSet.of(EventType.SPACE_ENTERED, EventType.DEVICE_DISCOVERED, EventType.DEVICES_UPDATE, EventType.DEVICE_LOST, EventType.SPACE_ABANDONED))
                .setRssiCalculator(RssiCalculators.newLimitedMeanRssiCalculator(2))
                .setIBeaconFilters(Arrays.asList(IBeaconFilters.newProximityUUIDFilter(UUID.fromString("f7826da6-4fa2-4e98-8024-bc5b71e0893e"))))
                .build();

        EddystoneScanContext eddystoneScanContext = new EddystoneScanContext.Builder()
                .setEventTypes(EnumSet.of(EventType.SPACE_ENTERED, EventType.DEVICE_DISCOVERED, EventType.DEVICES_UPDATE, EventType.DEVICE_LOST, EventType.SPACE_ABANDONED))
                .setUIDFilters(Arrays.asList(
                        EddystoneFilters.newUIDFilter(KontaktSDK.DEFAULT_KONTAKT_EDDYSTONE_NAMESPACE_ID, "000023")
                ))
                .setURLFilters(Arrays.asList(
                        EddystoneFilters.newURLFilter("http://myapp.mx")
                ))
                .build();


        scanContext = new ScanContext.Builder()
                .setScanMode(ProximityManager.SCAN_MODE_BALANCED)
                .setIBeaconScanContext(iBeaconScanContext)
                .setEddystoneScanContext(eddystoneScanContext)
                .setActivityCheckConfiguration(ActivityCheckConfiguration.DEFAULT)
                .setForceScanConfiguration(ForceScanConfiguration.DEFAULT)
                .setScanPeriod(new ScanPeriod(TimeUnit.SECONDS.toMillis(60), 0))
                .build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(BluetoothUtils.isBluetoothEnabled())
            initializeScan();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        proximityManager.finishScan();
        proximityManager.disconnect();
        proximityManager = null;
    }

    private void initializeScan() {
        proximityManager.initializeScan(scanContext, new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                proximityManager.attachListener(FinderBeacon.this);
            }

            @Override
            public void onConnectionFailure() {
                Toast.makeText(getApplicationContext(), R.string.scanFailure, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onScanStart() {

    }

    @Override
    public void onScanStop() {

    }

    @Override
    public void onEvent(BluetoothDeviceEvent event) {
        switch (event.getDeviceProfile()) {

            case IBEACON:
                final IBeaconDeviceEvent iBeaconDeviceEvent = (IBeaconDeviceEvent) event;
                new Runnable() {
                    @Override
                    public void run() {
                        IBeaconRegion region = iBeaconDeviceEvent.getRegion();
                        Log.d(TAG, "iBeacon " + region);
                        List<IBeaconDevice> deviceList = iBeaconDeviceEvent.getDeviceList();
                    }
                }.run();
                break;

            case EDDYSTONE:
                final EddystoneDeviceEvent eddystoneDeviceEvent = (EddystoneDeviceEvent) event;
                new Runnable() {
                    @Override
                    public void run() {
                        IEddystoneNamespace namespace = eddystoneDeviceEvent.getNamespace();
                        Log.d(TAG, "Eddystone " + namespace);
                        List<IEddystoneDevice> deviceList = eddystoneDeviceEvent.getDeviceList();
                    }
                }.run();
                break;

            default:

        }
    }
}
