package com.sap.codelab.utils.permission

/**
 * Created by M.Çağatay
 * Created on 19.08.2026
 * Defines the permission flow based on the requested feature.
 */
internal enum class PermissionFlow {

    /**
     * Requires notification, fine location and background location.
     */
    NOTIFICATION,

    /**
     * Requires only foreground fine location.
     */
    LOCATION
}