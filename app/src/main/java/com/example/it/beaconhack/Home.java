package com.example.it.beaconhack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
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
import com.kontakt.sdk.android.ble.discovery.ibeacon.IBeaconAdvertisingPacket;
import com.kontakt.sdk.android.ble.discovery.ibeacon.IBeaconDeviceEvent;
import com.kontakt.sdk.android.ble.filter.ibeacon.IBeaconFilter;
import com.kontakt.sdk.android.ble.filter.ibeacon.IBeaconFilters;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.rssi.RssiCalculators;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.proxama.tappoint.auth.AuthListener;
import com.proxama.tappoint.auth.Authentication;
import com.proxama.tappoint.error.ApiError;
import com.proxama.tappoint.reporting.Reporting;
import com.proxama.tappoint.reporting.TriggerEventType;
import com.proxama.tappoint.sync.SyncListener;
import com.proxama.tappoint.sync.SyncResult;
import com.proxama.tappoint.sync.Synchronisation;
import com.proxama.tappoint.trigger.Trigger;
import com.proxama.tappoint.trigger.Triggers;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by diego on 21/11/15.
 */
public class Home extends AppCompatActivity implements ProximityManager.ProximityListener, AuthListener, SyncListener,
        BeaconEventClickListener {

    private ProximityManager proximityManager;
    private ScanContext scanContext;
    /** Log tag. */
    private static final String TAG = Home.class.getSimpleName();
    TextView textView;
    TextView listDevices;

    private View coordinator;
    private IBeaconDevice keepBeacon;

    /** Name of the app as recognised by the TapPoint server. */
    private static final String APP_NAME = "khbushwacker";

    /** JSON key for the data block found within the payload of a trigger. */
    public static final String JSON_KEY_DATA = "data";

    /** JSON key for the subtitle found within the payload of a trigger. */
    public static final String JSON_KEY_SUBTITLE = "subtitle";

    /** JSON key for the title found within the payload of a trigger. */
    public static final String JSON_KEY_TITLE = "title";

    /** JSON key for the title found within the payload of a trigger. */
    public static final String JSON_KEY_ID = "id";

    /** JSON key for the title found within the payload of a trigger. */
    public static final String JSON_KEY_IMAGE = "image";

    ProgressBar mProgressBar;

    /** Broadcast receiver to receive beacon events. */
    private BeaconEventReceiver mBroadcastReceiver;

    /** Intent filter to listen to the specific beacon event intent. */
    private IntentFilter mBeaconEventFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        mProgressBar = (ProgressBar) findViewById(R.id.pbProgressIndicator);

        Authentication.getAuthManager(this).authenticate(APP_NAME, this);

//        configureBeaconEventList();
        configureBeaconEventListener();

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

    @Override
    public void onAuthSuccess() {
//        Toast.makeText(this, "Auth successful", Toast.LENGTH_LONG).show();

        Synchronisation.getSyncManager(this).synchronise(this);
    }

    @Override
    public void onAuthFailure(ApiError apiError) {
        mProgressBar.setVisibility(View.INVISIBLE);

//        Toast.makeText(this, "Auth failed: " + apiError.name(), Toast.LENGTH_LONG).show();
        Log.d(TAG, apiError.getErrorMessage());
    }

    @Override
    public void onSyncSuccess(SyncResult syncResult) {
        mProgressBar.setVisibility(View.INVISIBLE);

        int numberAdded = syncResult.getTriggersAdded().size();
        int numberRemoved = syncResult.getTriggersRemoved().size();

//        Toast.makeText(this, "Sync successful. Added triggers: " + numberAdded + ". Removed triggers: " +numberRemoved, Toast.LENGTH_LONG).show();
        Triggers.getTriggersManager(this).startMonitoring();
    }

    @Override
    public void onSyncFailure(ApiError apiError) {
        mProgressBar.setVisibility(View.INVISIBLE);

        Toast.makeText(this, "Sync failed: " + apiError.name(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBeaconEventClicked(String beaconId) {
        Reporting.getReportingManager(this).reportTriggerEvent(TriggerEventType.USER_SELECTED, beaconId);
    }

    /**
     * Configures the beacon event listener that will listen for beacon events.
     */
    private void configureBeaconEventListener() {
        mBroadcastReceiver = new BeaconEventReceiver();
        mBeaconEventFilter = new IntentFilter(Triggers.ACTION_TRIGGERS_DETECTED);
    }




    /**
     * {@link android.content.BroadcastReceiver} to listen to beacon events.
     */
    private class BeaconEventReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("intent.getAction()","-->"+intent.getAction());
            if (Triggers.ACTION_TRIGGERS_DETECTED.equals(intent.getAction())) {
                ArrayList<Trigger> triggers = intent.getParcelableArrayListExtra(Triggers.EXTRA_DETECTED_TRIGGERS);
                addBeaconEvents(triggers);
            }
        }
    }

    /**
     * Adds all beacon events that have recently been raised as a result of monitoring for beacon triggers.
     *
     * @param beacons a list of beacons to add as beacon events.
     */
    private void addBeaconEvents(ArrayList<Trigger> beacons) {
        for (Trigger beacon : beacons) {
            try {
//                mBeaconAdapter.addEvent(getBeaconEvent(beacon));

                Reporting.getReportingManager(this).reportTriggerEvent(TriggerEventType.USER_NOTIFIED,
                        beacon.getTriggerId());
            } catch (Exception e) {
                Log.d(TAG, "Could not parse JSON from beacon.");
            }
        }
    }

    /**
     * Gets the {@link com.proxama.tpsdkreferenceapp.BeaconEvent} according to the given beacon.
     *
     * @param beacon the beacon to retrieve trigger data from.
     * @return the converted beacon event to display.
     */
//    private BeaconEvent getBeaconEvent(Trigger beacon) throws JSONException {
//        String triggerId = beacon.getTriggerId();
//        JSONObject triggerData = beacon.getTriggerPayload().getJSONObject(JSON_KEY_DATA);
//        String subtitle = triggerData.getString(JSON_KEY_SUBTITLE);
//        String title = triggerData.getString(JSON_KEY_TITLE);
//
//        return new BeaconEvent(triggerId, title, subtitle, getBeaconImageId(title));
//    }

    /**
     * Returns the beacon image ID given the beacon's title.
     *
     * @param title to use for looking up the correct image resource.
     * @return the image ID used as an image resource.
     */
//    private int getBeaconImageId(String title) {
//        Integer imageId = mImageMap.get(title);
//        return imageId == null ? R.drawable.image_1 : imageId;
//    }

}
