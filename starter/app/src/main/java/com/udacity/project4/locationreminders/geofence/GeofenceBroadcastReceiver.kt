package com.udacity.project4.locationreminders.geofence

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.locationreminders.savereminder.ACTION_GEOFENCE_EVENT
import com.udacity.project4.locationreminders.savereminder.selectreminderlocation.SelectLocationFragment

/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 */

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private val TAG = GeofenceBroadcastReceiver::class.java.simpleName

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive")
        if (intent.action == ACTION_GEOFENCE_EVENT) {
            if (GeofencingEvent.fromIntent(intent).hasError()) {
                Log.e(TAG, "Geofence error in the intent received")
                return
            }
            GeofenceTransitionsJobIntentService.enqueueWork(context, intent)
        }
    }
}