package com.example.it.beaconhack;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
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
import com.kontakt.sdk.android.ble.discovery.ibeacon.IBeaconAdvertisingPacket;
import com.kontakt.sdk.android.ble.filter.ibeacon.IBeaconFilter;
import com.kontakt.sdk.android.ble.filter.ibeacon.IBeaconFilters;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.rssi.RssiCalculators;
import com.kontakt.sdk.android.ble.util.BluetoothUtils;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.EnumSet;
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
                .setEventTypes(Arrays.asList(
                        EventType.SPACE_ENTERED,
                        EventType.DEVICE_DISCOVERED,
                        EventType.DEVICES_UPDATE,
                        EventType.DEVICE_LOST,
                        EventType.SPACE_ABANDONED
                ))
                .setRssiCalculator(RssiCalculators.newLimitedMeanRssiCalculator(2))
                .setIBeaconFilters(Arrays.asList(
                        new IBeaconFilter() {
                            @Override
                            public boolean apply(IBeaconAdvertisingPacket iBeaconAdvertisingPacket) {
                                return iBeaconAdvertisingPacket.getProximityUUID().toString().equals("945765bc-90b1-11e5-8994-feff819cdc9f");
                            }
                        }
                ))
                .build();

        scanContext = new ScanContext.Builder()
                .setScanMode(ProximityManager.SCAN_MODE_BALANCED)
                .setIBeaconScanContext(iBeaconScanContext)
                .setActivityCheckConfiguration(ActivityCheckConfiguration.DEFAULT)
                .setForceScanConfiguration(ForceScanConfiguration.DEFAULT)
                .setScanPeriod(new ScanPeriod(TimeUnit.SECONDS.toMillis(3), TimeUnit.SECONDS.toMillis(10)))
                .build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(BluetoothUtils.isBluetoothEnabled()){
            initializeScan();

        }

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
        Log.d(TAG, "Scaneo iniciado");
    }

    @Override
    public void onScanStop() {
        Log.d(TAG, "Scaneo detenido");
    }

    @Override
    public void onEvent(BluetoothDeviceEvent event) {
        switch (event.getDeviceProfile()) {

            case IBEACON:
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                builder.setSmallIcon(R.mipmap.monkey_96);
                builder.setVibrate(new long[]{1000, 1000});
                builder.setLights(Color.RED, 3000, 3000);
                Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                builder.setSound(alarmSound);
                builder.setContentTitle(getResources().getString(R.string.notif_title));
                builder.setContentText(getResources().getString(R.string.notif_info));
                PendingIntent intencionPendiente = PendingIntent.getActivity(
                        this,
                        0,
                        new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(intencionPendiente);
                builder.setAutoCancel(true);
                manager.notify(ID_NOTIFICACION, builder.build());
                stopSelf();
                break;

            default:
        }
    }
}
