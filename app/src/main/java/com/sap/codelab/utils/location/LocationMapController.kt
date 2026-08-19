package com.sap.codelab.utils.location

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.sap.codelab.R
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.plugins.annotation.Symbol
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions

/**
 * Created by M.Çağatay
 * Created on 18.08.2026
 *
 * Manages map initialization, location selection, marker updates,
 * camera positioning, and map lifecycle operations.
 *
 * It centralizes map-related behavior to avoid duplicating
 * map implementation logic across different screens.
 */
internal class LocationMapController(
    private val mapView: MapView,
    lifecycleOwner: LifecycleOwner,
    savedInstanceState: Bundle?,
    private val isSelectable: Boolean = true,
    private val onLocationSelected: ((LatLng) -> Unit)? = null
) : DefaultLifecycleObserver {

    private lateinit var map: MapLibreMap
    private lateinit var symbolManager: SymbolManager

    private var selectedSymbol: Symbol? = null

    private var pendingLocation: LatLng? = null
    private var pendingMoveCamera = false

    init {
        lifecycleOwner.lifecycle.addObserver(this)
        mapView.onCreate(savedInstanceState)
        setupMap()
    }

    /*
     * Initializes the map and its style.
     */
    private fun setupMap() {
        mapView.getMapAsync { mapLibreMap ->

            map = mapLibreMap

            map.setStyle(MAP_STYLE) { style ->

                val markerDrawable = requireNotNull(
                    ContextCompat.getDrawable(
                        mapView.context,
                        R.drawable.ic_location
                    )
                )

                style.addImage(MARKER_ICON_ID, markerDrawable)

                symbolManager = SymbolManager(mapView, map, style).apply {
                    iconAllowOverlap = true
                    iconIgnorePlacement = true
                }

                if (isSelectable) {
                    setupMapListener()
                }

                pendingLocation?.let { latLng ->
                    setLocation(latLng = latLng, moveCamera = pendingMoveCamera)
                }
            }

            map.cameraPosition = CameraPosition.Builder()
                .target(ISTANBUL_DEFAULT_LOCATION)
                .zoom(DEFAULT_ZOOM)
                .build()
        }
    }

    /*
    * Sets up the map listener for long-press events.
     */
    private fun setupMapListener() {
        map.addOnMapLongClickListener { latLng ->
            setLocation(latLng)
            onLocationSelected?.invoke(latLng)
            true
        }
    }

    /*
    * Updates the location marker and camera position.
     */
    fun setLocation(latLng: LatLng, moveCamera: Boolean = false) {
        pendingLocation = latLng
        pendingMoveCamera = moveCamera

        if (!::symbolManager.isInitialized) {
            return
        }

        selectedSymbol?.let(symbolManager::delete)

        selectedSymbol = symbolManager.create(
            SymbolOptions()
                .withLatLng(latLng)
                .withIconImage(MARKER_ICON_ID)
                .withIconAnchor("bottom")
                .withIconSize(2.0f)
        )

        if (moveCamera) {
            map.cameraPosition = CameraPosition.Builder()
                .target(latLng)
                .zoom(SELECTED_LOCATION_ZOOM)
                .build()
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        mapView.onStart()
    }

    override fun onResume(owner: LifecycleOwner) {
        mapView.onResume()
    }

    override fun onPause(owner: LifecycleOwner) {
        mapView.onPause()
    }

    override fun onStop(owner: LifecycleOwner) {
        mapView.onStop()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        if (::symbolManager.isInitialized) {
            symbolManager.onDestroy()
        }

        mapView.onDestroy()
    }

    companion object {
        private const val MAP_STYLE = "https://tiles.openfreemap.org/styles/liberty"

        private const val MARKER_ICON_ID = "location_marker"

        private const val DEFAULT_ZOOM = 11.0
        private const val SELECTED_LOCATION_ZOOM = 15.0

        private val ISTANBUL_DEFAULT_LOCATION = LatLng(41.0082, 28.9784)
    }
}