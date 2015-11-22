/*******************************************************************************
 * Copyright 2015 Proxama PLC
 ******************************************************************************/

package com.example.it.beaconhack;

/**
 * Listener to define behaviour for when an beacon event is clicked.
 */
public interface BeaconEventClickListener {

    /**
     * Used to report when a beacon event has been clicked.
     *
     * @param beaconId the ID of the beacon.
     */
    void onBeaconEventClicked(String beaconId);
}
