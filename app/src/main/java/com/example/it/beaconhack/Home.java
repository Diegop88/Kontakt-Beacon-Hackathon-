package com.example.it.beaconhack;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
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
import com.kontakt.sdk.android.ble.discovery.ibeacon.IBeaconDeviceEvent;
import com.kontakt.sdk.android.ble.filter.ibeacon.IBeaconFilter;
import com.kontakt.sdk.android.ble.filter.ibeacon.IBeaconFilters;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.rssi.RssiCalculators;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by diego on 21/11/15.
 */
public class Home extends AppCompatActivity implements ProximityManager.ProximityListener {

    private ProximityManager proximityManager;
    private ScanContext scanContext;

    TextView textView;
    TextView listDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        textView = (TextView) findViewById(R.id.textinfo);
        listDevices = (TextView) findViewById(R.id.listdevices);

        proximityManager = new ProximityManager(this);

        IBeaconScanContext iBeaconScanContext = new IBeaconScanContext.Builder()
                .setEventTypes(Arrays.asList(
                        EventType.SPACE_ENTERED,
                        EventType.DEVICE_DISCOVERED,
                        EventType.DEVICES_UPDATE,
                        EventType.DEVICE_LOST,
                        EventType.SPACE_ABANDONED
                ))
                .setIBeaconFilters(Arrays.asList(
                        new IBeaconFilter() {
                            @Override
                            public boolean apply(IBeaconAdvertisingPacket iBeaconAdvertisingPacket) {
                                return iBeaconAdvertisingPacket.getProximityUUID().toString().equals("945765bc-90b1-11e5-8994-feff819cdc9f");
                            }
                        },
                        IBeaconFilters.newMajorFilter(69),
                        IBeaconFilters.newMinorFilter(2)
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

    @Override
    protected void onResume() {
        super.onResume();

        proximityManager.initializeScan(scanContext, new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                proximityManager.attachListener(Home.this);
            }

            @Override
            public void onConnectionFailure() {
                Toast.makeText(getApplicationContext(), R.string.scanFailure, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        proximityManager.finishScan();
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
        switch (event.getDeviceProfile()) {
            case IBEACON:
                IBeaconDeviceEvent iBeaconDeviceEvent = (IBeaconDeviceEvent) event;

                listDevices.setText("");
                List<IBeaconDevice> deviceList = iBeaconDeviceEvent.getDeviceList();
                for (IBeaconDevice device : deviceList) {
                    listDevices.append(device.getMajor() + "," + device.getMinor() + "\n");
                }

                switch (iBeaconDeviceEvent.getEventType()) {
                    case SPACE_ENTERED:
                        textView.append("Space entered\n");
                        break;

                    case DEVICE_DISCOVERED:
                        textView.append("Device discovered\n");
                        break;

                    case DEVICES_UPDATE:
                        textView.append("Devices update " + iBeaconDeviceEvent.getDeviceList().size() + "\n");
                        break;

                    case DEVICE_LOST:
                        textView.append("Device lost\n");
                        break;

                    case SPACE_ABANDONED:
                        textView.append("Space abandoned\n");
                        break;

                    default:

                }

            default:
        }
    }
}
