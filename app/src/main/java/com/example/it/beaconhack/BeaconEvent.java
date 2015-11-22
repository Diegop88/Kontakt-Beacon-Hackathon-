/*******************************************************************************
 * Copyright 2015 Proxama PLC
 ******************************************************************************/

package com.example.it.beaconhack;

import java.text.DateFormat;
import java.util.Date;

/**
 * Model holding all information for a beacon event.
 */
public class BeaconEvent {

    /** Beacon event trigger ID. */
    private String mTriggerId;

    /** Beacon event title. */
    private String mTitle;

    /** Beacon event subtitle. */
    private String mSubTitle;

    /** Resource image ID. */
    private int mImageResource;

    /** Timestamp of the beacon event. */
    private String mTimeStamp;

    /**
     * Creates a new instance.
     *
     * @param triggerId the trigger ID.
     * @param title the title.
     * @param subTitle the subtitle.
     * @param imageResource the image resource ID.
     */
    public BeaconEvent(String triggerId, String title, String subTitle, int imageResource) {
        mTriggerId = triggerId;
        mTitle = title;
        mSubTitle = subTitle;
        mImageResource = imageResource;
        mTimeStamp = DateFormat.getTimeInstance(DateFormat.MEDIUM).format(new Date());
    }

    /**
     * Returns the beacon event trigger ID.
     *
     * @return the trigger ID.
     */
    public String getTriggerId() {
        return mTriggerId;
    }

    /**
     * Returns the beacon event title.
     *
     * @return the title.
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * Returns the beacon event subtitle.
     *
     * @return the subtitle.
     */
    public String getSubTitle() {
        return mSubTitle;
    }

    /**
     * Returns the beacon event image resource ID.
     *
     * @return the resource ID.
     */
    public int getImageResource() {
        return mImageResource;
    }

    /**
     * Returns the beacon time stamp.
     *
     * @return the time of the event.
     */
    public String getTimeStamp() {
        return mTimeStamp;
    }
}
