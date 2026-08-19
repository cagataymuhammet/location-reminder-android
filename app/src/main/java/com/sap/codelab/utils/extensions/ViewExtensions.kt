package com.sap.codelab.utils.extensions

import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.sap.codelab.R
import com.sap.codelab.utils.permission.PermissionType

/**
 * Created by M.Çağatay
 * Created on 19.08.2026
 * Shows a permission warning Snackbar based on the missing permission type.
 */
internal fun View.showPermissionWarning(
    permissionType: PermissionType,
    duration: Int = Snackbar.LENGTH_INDEFINITE,
    onEnableClick: (() -> Unit)? = null
): Snackbar {

    val messageRes = when (permissionType) {
        PermissionType.NOTIFICATION ->
            R.string.permission_notification_required

        PermissionType.FINE_LOCATION ->
            R.string.permission_location_required

        PermissionType.BACKGROUND_LOCATION ->
            R.string.permission_background_location_required
    }

    return Snackbar.make(this, messageRes, duration).apply {
        onEnableClick?.let { action ->
            setAction(R.string.enable) {
                action()
            }
        }

        show()
    }
}