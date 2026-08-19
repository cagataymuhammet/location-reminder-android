package com.sap.codelab.utils.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

/**
 * Created by M.Çağatay
 * Created on 18.08.2026
 *
 * Manages runtime permissions required for location-based reminders.
 * Reminder permission flow:
 * Notification -> Fine Location -> Background Location
 */
@ActivityScoped
internal class PermissionManager @Inject constructor(@ActivityContext private val context: Context) {

    private lateinit var notificationLauncher: ActivityResultLauncher<String>
    private lateinit var locationLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var backgroundLocationLauncher: ActivityResultLauncher<String>
    private lateinit var settingsLauncher: ActivityResultLauncher<Intent>

    private var currentFlow: PermissionFlow? = null
    private var settingsPermissionType: PermissionType? = null

    private var onGranted: (() -> Unit)? = null
    private var onDenied: ((PermissionType) -> Unit)? = null

    private var isRegistered = false

    /**
     * Registers Activity Result launchers.
     *
     * Must be called once during Activity creation.
     */
    fun register(activity: ComponentActivity) {
        if (isRegistered) return

        registerNotificationLauncher(activity)
        registerLocationLauncher(activity)
        registerBackgroundLocationLauncher(activity)
        registerSettingsLauncher(activity)

        isRegistered = true
    }

    /**
     * Ensures all permissions required for location reminders.
     *
     * Permission flow:
     * Notification -> Fine Location -> Background Location
     */
    fun ensureNotificationPermissions(onGranted: () -> Unit, onDenied: (PermissionType) -> Unit) {
        prepareRequest(
            flow = PermissionFlow.NOTIFICATION,
            onGranted = onGranted,
            onDenied = onDenied
        )
        checkNotificationPermission()
    }

    /*
     * Ensures only foreground location permission.
     */
    private fun registerNotificationLauncher(activity: ComponentActivity) {
        notificationLauncher =
            activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (hasNotificationPermission()) {
                    checkLocationPermission()
                } else {
                    permissionDenied(PermissionType.NOTIFICATION)
                }
            }
    }

    /*
     * Ensures precise foreground location permission.
     */
    private fun registerLocationLauncher(activity: ComponentActivity) {
        locationLauncher =
            activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {

                if (!hasFineLocationPermission()) {
                    permissionDenied(PermissionType.FINE_LOCATION)
                    return@registerForActivityResult
                }

                when (currentFlow) {
                    PermissionFlow.NOTIFICATION -> checkBackgroundLocationPermission()
                    PermissionFlow.LOCATION -> permissionGranted()
                    null -> Unit
                }
            }
    }

    /*
     * Ensures background location permission required
     * for location reminders while the app is not in use.
     */
    private fun registerBackgroundLocationLauncher(activity: ComponentActivity) {
        backgroundLocationLauncher =
            activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (hasBackgroundLocationPermission()) {
                    permissionGranted()
                } else {
                    permissionDenied(PermissionType.BACKGROUND_LOCATION)
                }
            }
    }

    /*
     * Ensures system settings launcher.
     */
    private fun registerSettingsLauncher(activity: ComponentActivity) {
        settingsLauncher =
            activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                handleSettingsResult()
            }
    }

    /*
     * Prepares the permission request.
     */
    private fun prepareRequest(
        flow: PermissionFlow,
        onGranted: () -> Unit,
        onDenied: (PermissionType) -> Unit
    ) {

        // Ensures Activity Result launchers are registered before starting the permission flow.
        check(isRegistered) {
            "PermissionManager.register(activity) must be called before requesting permissions."
        }

        currentFlow = flow
        this.onGranted = onGranted
        this.onDenied = onDenied
    }


    /*
     * STEP 1
     *
     * Checks notification runtime permission first.
     * Also checks if notifications were later disabled
     * manually from system settings.
     */
    private fun checkNotificationPermission() {

        if (!hasNotificationRuntimePermission()) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                openAppSettings(PermissionType.NOTIFICATION)
            }

            return
        }

        if (!areNotificationsEnabled()) {
            openAppSettings(PermissionType.NOTIFICATION)
            return
        }

        checkLocationPermission()
    }

    /*
     * STEP 2
     *
     * Checks precise foreground location permission.
     */
    private fun checkLocationPermission() {

        if (hasFineLocationPermission()) {
            when (currentFlow) {
                PermissionFlow.NOTIFICATION -> checkBackgroundLocationPermission()
                PermissionFlow.LOCATION -> permissionGranted()
                null -> Unit
            }

            return
        }

        locationLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }

    /*
     * STEP 3
     *
     * Checks background location permission required
     * for location reminders while the app is not in use.
     */
    private fun checkBackgroundLocationPermission() {

        if (hasBackgroundLocationPermission()) {
            permissionGranted()
            return
        }

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                /*
                 * Android 11+ requires background location
                 * to be enabled from system settings.
                 */
                openAppSettings(PermissionType.BACKGROUND_LOCATION)
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }

            else -> {
                permissionGranted()
            }
        }
    }

    /**
     * Re-checks the permission after returning from system settings.
     */
    private fun handleSettingsResult() {

        when (settingsPermissionType) {

            PermissionType.NOTIFICATION -> {
                if (hasNotificationPermission()) {
                    checkLocationPermission()
                } else {
                    permissionDenied(PermissionType.NOTIFICATION)
                }
            }

            PermissionType.BACKGROUND_LOCATION -> {
                if (hasBackgroundLocationPermission()) {
                    permissionGranted()
                } else {
                    permissionDenied(PermissionType.BACKGROUND_LOCATION)
                }
            }

            else -> Unit
        }
    }

    /**
     * Checks both runtime notification permission and
     * application-level notification settings.
     */
    private fun hasNotificationPermission(): Boolean {
        return hasNotificationRuntimePermission() &&
                areNotificationsEnabled()
    }

    /**
     * POST_NOTIFICATIONS is a runtime permission on Android 13+.
     */
    private fun hasNotificationRuntimePermission(): Boolean {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true
        }

        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Detects notifications disabled manually from system settings.
     */
    private fun areNotificationsEnabled(): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    private fun hasFineLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasBackgroundLocationPermission(): Boolean {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return true
        }

        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun openAppSettings(permissionType: PermissionType) {
        settingsPermissionType = permissionType

        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts(
                "package",
                context.packageName,
                null
            )
        }

        settingsLauncher.launch(intent)
    }

    private fun permissionGranted() {
        val callback = onGranted

        clearRequest()

        callback?.invoke()
    }

    /*
     * Handles permission denied.
     */
    private fun permissionDenied(permissionType: PermissionType) {
        val callback = onDenied
        clearRequest()
        callback?.invoke(permissionType)
    }


    /*
     * Returns the first missing permission.
     */
    fun getMissingReminderPermission(): PermissionType? {
        return when {
            !hasNotificationPermission() -> PermissionType.NOTIFICATION
            !hasFineLocationPermission() -> PermissionType.FINE_LOCATION
            !hasBackgroundLocationPermission() -> PermissionType.BACKGROUND_LOCATION
            else -> null
        }
    }

    private fun clearRequest() {
        currentFlow = null
        settingsPermissionType = null
        onGranted = null
        onDenied = null
    }
}