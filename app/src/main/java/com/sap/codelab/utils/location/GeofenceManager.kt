package com.sap.codelab.utils.location

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Created by M.Çağatay
 * Created on 17.08.2026
 */
internal class GeofenceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val geofencingClient: GeofencingClient
) {

    /**
     * Creates the PendingIntent used by the Geofencing API
     * to deliver geofence transition events to GeofenceBroadcastReceiver.
     */
    private val geofencePendingIntent: PendingIntent by lazy {

        // Defines the BroadcastReceiver that will handle geofence transition events.
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)

        // Android 12+ requires a mutable PendingIntent for geofencing operations.
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        /*
        *Creates a broadcast PendingIntent that will be triggered by the Geofencing API.
        * A single request code is sufficient because all geofence events are delivered through the same BroadcastReceiver.
        */
        PendingIntent.getBroadcast(context, 0, intent, flags)
    }


    /*
     * Removes the geofences that have been previously registered.
     */
    internal suspend fun removeGeofence(memoId: Long) {
        suspendCancellableCoroutine { continuation ->
            geofencingClient
                .removeGeofences(listOf(memoId.toString()))
                .addOnSuccessListener {
                    if (continuation.isActive) {
                        continuation.resume(Unit)
                    }
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) {
                        continuation.resumeWithException(exception)
                    }
                }
        }
    }

    /*
     * Registers the geofences that have been previously registered.
     */
    @SuppressLint("MissingPermission")
    internal suspend fun registerGeofence(memoId: Long, latitude: Double, longitude: Double) {

        val geofence = createGeofence(memoId = memoId, latitude = latitude, longitude = longitude)

        /*
         * Creates the geofence request.
         */
        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()


        /*
        * GeofencingClient uses a callback-based Task API, while this function
        * follows a coroutine-based suspend structure.
        *
        * suspendCancellableCoroutine bridges the callback-based API with Kotlin
        * Coroutines, allowing the caller to wait for the geofence registration
        * without blocking the current thread.
        *
        * The coroutine is resumed with Unit when the registration succeeds,
        * and resumed with the original exception when the operation fails.
        *
        * isActive is checked before resuming the continuation to avoid trying
        * to resume a coroutine that has already been cancelled or completed.
        */
        suspendCancellableCoroutine { continuation ->
            geofencingClient
                .addGeofences(request, geofencePendingIntent)
                .addOnSuccessListener {
                    if (continuation.isActive) {
                        continuation.resume(Unit)
                    }
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) {
                        continuation.resumeWithException(exception)
                    }
                }
        }
    }

    /*
     * Creates a geofence.
     */
    private fun createGeofence(memoId: Long, latitude: Double, longitude: Double): Geofence {
        return Geofence.Builder()
            .setRequestId(memoId.toString())
            .setCircularRegion(
                latitude,
                longitude,
                GEOFENCE_RADIUS_METERS
            )
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER
            )
            .setExpirationDuration(
                Geofence.NEVER_EXPIRE
            )
            .build()
    }

    companion object {
        private const val GEOFENCE_RADIUS_METERS = 200f
    }
}