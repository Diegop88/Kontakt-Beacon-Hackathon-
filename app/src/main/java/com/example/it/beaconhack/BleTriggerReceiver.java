/*******************************************************************************
 * Copyright 2015 Proxama PLC
 ******************************************************************************/

package com.example.it.beaconhack;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.proxama.tappoint.trigger.Trigger;
import com.proxama.tappoint.trigger.Triggers;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A {@link BroadcastReceiver} that receives new BLE trigger events and displays a notification relating
 * to that BLE trigger event.
 */
public class BleTriggerReceiver extends BroadcastReceiver {

    /** Log tag. */
    private static final String TAG = BleTriggerReceiver.class.getSimpleName();

    /** Action main for launching the same intent. */
    public static final String ANDROID_INTENT_ACTION_MAIN = "android.intent.action.MAIN";

    /** Intent category for launching the same intent. */
    public static final String ANDROID_INTENT_CATEGORY_LAUNCHER = "android.intent.category.LAUNCHER";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Triggers.ACTION_TRIGGERS_DETECTED.equals(intent.getAction())) {
            ArrayList<Trigger> triggers = intent.getParcelableArrayListExtra(Triggers.EXTRA_DETECTED_TRIGGERS);

            for (Trigger trigger : triggers) {
                sendNotificationForTrigger(context, trigger);
            }
        }
    }

    /**
     * Sends a notification with information about a trigger that has been detected by this receiver.
     *
     * @param context the application context used for sending the notification.
     * @param trigger the trigger object to send a notification for.
     */
    private void sendNotificationForTrigger(Context context, Trigger trigger) {
        String title = getTitleFromTrigger(trigger);
        String subtitle = getSubtitleFromTrigger(trigger);
        String id = getIdFromTrigger(trigger);
        String image = getImageFromTrigger(trigger);


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher).setContentTitle(title)
                .setContentText(subtitle).setDefaults(NotificationCompat
                        .DEFAULT_ALL);

//        if(! wasShown(id, context)){
            Bundle extras = new Bundle();
            extras.putString("title", title);
            extras.putString("subtitle", subtitle);
            extras.putString("id", id);
            extras.putString("image", image);


            Intent intent = new Intent(context, MarketingActivity.class);
            intent.setAction(ANDROID_INTENT_ACTION_MAIN);
            intent.addCategory(ANDROID_INTENT_CATEGORY_LAUNCHER);
            intent.putExtras(extras);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            notificationBuilder.setContentIntent(resultPendingIntent);
            notificationBuilder.setAutoCancel(true);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context
                    .NOTIFICATION_SERVICE);
            notificationManager.notify(Integer.parseInt(id), notificationBuilder.build());
//        }else{
//            Log.i("Notificación ya mostrada","Notificación ya mostrada");
//        }


    }

    private boolean wasShown(String id, Context context) {
        SharedPreferences preferences = context.getSharedPreferences("prefs", context.MODE_PRIVATE);
        String[] ids = preferences.getString("id","0").split("-");
        for( int i = 0; i < ids.length; i++){
            if(id.equals(ids[i])){
                return true;
            }else{
                return false;
            }
        }
        return false;
    }

    /**
     * Retrieves the current time in milliseconds.
     *
     * @return the current time.
     */
    private int getCurrentTimeInMilliseconds() {
        return (int) System.currentTimeMillis();
    }

    /**
     * Retrieves the title from a {@link Trigger}.
     *
     * @param trigger the trigger to retrieve the content text from.
     * @return the content text if found in the trigger, or an empty string if not found.
     */
    private String getTitleFromTrigger(Trigger trigger) {
        JSONObject triggerPayload = trigger.getTriggerPayload();

        String title = "";

        try {
            JSONObject triggerData = triggerPayload.getJSONObject(Home.JSON_KEY_DATA);
            title = triggerData.optString(Home.JSON_KEY_TITLE);
        } catch (JSONException ex) {
            Log.d(TAG, "Unable to retrieve title from payload.");
        }

        return title;
    }

    /**
     * Retrieves the content text from a {@link Trigger}.
     *
     * @param trigger the trigger to retrieve the subtitle text from.
     * @return the subtitle text if found in the trigger, or an empty string if not found.
     */
    private String getSubtitleFromTrigger(Trigger trigger) {
        JSONObject triggerPayload = trigger.getTriggerPayload();

        String subtitle = "";

        try {
            JSONObject triggerData = triggerPayload.getJSONObject(Home.JSON_KEY_DATA);
            subtitle = triggerData.optString(Home.JSON_KEY_SUBTITLE);
        } catch (JSONException ex) {
            Log.d(TAG, "Unable to retrieve subtitle from payload.");
        }

        return subtitle;
    }

    /**
     * Retrieves the content text from a {@link Trigger}.
     *
     * @param trigger the trigger to retrieve the subtitle text from.
     * @return the subtitle text if found in the trigger, or an empty string if not found.
     */
    private String getIdFromTrigger(Trigger trigger) {
        JSONObject triggerPayload = trigger.getTriggerPayload();

        String id = "";

        try {
            JSONObject triggerData = triggerPayload.getJSONObject(Home.JSON_KEY_DATA);
            id = triggerData.optString(Home.JSON_KEY_ID);
        } catch (JSONException ex) {
            Log.d(TAG, "Unable to retrieve id from payload.");
        }

        return id;
    }

    /**
     * Retrieves the content text from a {@link Trigger}.
     *
     * @param trigger the trigger to retrieve the subtitle text from.
     * @return the subtitle text if found in the trigger, or an empty string if not found.
     */
    private String getImageFromTrigger(Trigger trigger) {
        JSONObject triggerPayload = trigger.getTriggerPayload();

        String image = "";

        try {
            JSONObject triggerData = triggerPayload.getJSONObject(Home.JSON_KEY_DATA);
            image = triggerData.optString(Home.JSON_KEY_IMAGE);
        } catch (JSONException ex) {
            Log.d(TAG, "Unable to retrieve image from payload.");
        }

        return image;
    }

}
