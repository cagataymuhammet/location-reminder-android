package com.sap.codelab

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.maplibre.android.MapLibre

/**
 * Extension of the Android Application class.
 */
@HiltAndroidApp
internal class App : Application() {

    /*
     * Called when the application is starting, before any activity, service, or receiver objects (excluding content providers) have been created.
     */
    override fun onCreate() {
        super.onCreate()
        MapLibre.getInstance(this)
    }
}