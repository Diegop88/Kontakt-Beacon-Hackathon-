package com.example.it.beaconhack;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.kontakt.sdk.android.ble.configuration.ActivityCheckConfiguration;
import com.kontakt.sdk.android.ble.configuration.ForceScanConfiguration;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.configuration.scan.IBeaconScanContext;
import com.kontakt.sdk.android.ble.configuration.scan.ScanContext;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.discovery.BluetoothDeviceEvent;
import com.kontakt.sdk.android.ble.discovery.EventType;
import com.kontakt.sdk.android.ble.discovery.ibeacon.IBeaconDeviceEvent;
import com.kontakt.sdk.android.ble.filter.ibeacon.IBeaconFilters;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.rssi.RssiCalculators;
import com.kontakt.sdk.android.ble.util.BluetoothUtils;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;

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

    private ProximityManager proximityManager;
    private ScanContext scanContext;
    private NotificationManager manager;

    private static final int ID_NOTIFICACION = 1;

    @Override
    public void onCreate() {
        super.onCreate();

        proximityManager = new ProximityManager(this);

        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        IBeaconScanContext iBeaconScanContext = new IBeaconScanContext.Builder()
                .setEventTypes(EnumSet.of(EventType.SPACE_ENTERED, EventType.DEVICE_DISCOVERED, EventType.DEVICES_UPDATE, EventType.DEVICE_LOST, EventType.SPACE_ABANDONED))
                .setRssiCalculator(RssiCalculators.newLimitedMeanRssiCalculator(2))
                .setIBeaconFilters(Arrays.asList(IBeaconFilters.newProximityUUIDFilter(UUID.fromString("f7826da6-4fa2-4e98-8024-bc5b71e0893e"))))
                .build();

        scanContext = new ScanContext.Builder()
                .setScanMode(ProximityManager.SCAN_MODE_BALANCED)
                .setIBeaconScanContext(iBeaconScanContext)
                .setActivityCheckConfiguration(ActivityCheckConfiguration.DEFAULT)
                .setForceScanConfiguration(ForceScanConfiguration.DEFAULT)
                .setScanPeriod(new ScanPeriod(TimeUnit.SECONDS.toMillis(60), 0))
                .build();

        Log.d(TAG, "Created!");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "Inicio");

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
        Log.d(TAG, "OK");

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
        Toast.makeText(this, "Buscando beacons", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onScanStop() {
        Toast.makeText(this, "Busqueda detenida", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEvent(BluetoothDeviceEvent event) {
        switch (event.getDeviceProfile()) {

            case IBEACON:
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                builder.setSmallIcon(R.mipmap.ic_launcher);
                builder.setVibrate(new long[]{1000, 1000});
                builder.setLights(Color.RED, 3000, 3000);
                Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                builder.setSound(alarmSound);
                builder.setContentTitle(getResources().getString(R.string.notif_title));
                builder.setContentText(getResources().getString(R.string.notif_info));
                PendingIntent intencionPendiente = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(intencionPendiente);
                builder.setAutoCancel(true);
                manager.notify(ID_NOTIFICACION, builder.build());

                stopSelf();

                break;

            default:
        }
    }
}
