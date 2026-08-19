package com.sap.codelab.utils.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.sap.codelab.domain.GetMemoByIdUseCase
import com.sap.codelab.utils.notification.NotificationUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by M.Çağatay
 * Created on 17.08.2026
 *
 * Handles geofence transition events and shows the related memo notification when the user enters the registered area.
 *
 */
@AndroidEntryPoint
internal class GeofenceBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var getMemoByIdUseCase: GetMemoByIdUseCase

    @Inject
    lateinit var notificationUtil: NotificationUtil

    @Inject
    lateinit var geofenceManager: GeofenceManager

    override fun onReceive(context: Context, intent: Intent) {

        /*
         * Gets the geofencing event from the intent.
         */
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return

        /*
         * Checks for errors and returns
         */
        if (geofencingEvent.hasError()) {
            return
        }

        /*
         * Checks for geofence transition types.
         */
        if (geofencingEvent.geofenceTransition != Geofence.GEOFENCE_TRANSITION_ENTER) {
            return
        }


        // Keeps the BroadcastReceiver alive while asynchronous work is running.
        val pendingResult = goAsync()

        // Runs database and geofence operations asynchronously on the IO dispatcher.
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {

                // Handles each geofence that triggered the ENTER event.
                geofencingEvent.triggeringGeofences?.forEach { geofence ->

                    // Converts the geofence request ID back to the related memo ID.
                    val memoId = geofence.requestId.toLongOrNull() ?: return@forEach

                    // Retrieves the related memo from the database.
                    val memo = getMemoByIdUseCase(memoId) ?: return@forEach

                    // Shows the memo notification to the user.
                    notificationUtil.showMemoNotification(memo)

                    // Removes the geofence after it has been triggered
                    // to prevent duplicate notifications.
                    geofenceManager.removeGeofence(memoId)
                }
            } finally {
                // Notifies Android that the asynchronous BroadcastReceiver work is complete.
                pendingResult.finish()
            }
        }
    }
}