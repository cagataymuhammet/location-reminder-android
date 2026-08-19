package com.sap.codelab.di

import android.content.Context
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Created by M.Çağatay
 * Created on 17.08.2026
 * LocationModule provides location-related dependencies to the Hilt dependency graph.
 */
@Module
@InstallIn(SingletonComponent::class)
internal object LocationModule {

    /*
     * Provides a singleton instance of the GeofencingClient.
     */
    @Provides
    @Singleton
    fun provideGeofencingClient(@ApplicationContext context: Context): GeofencingClient {
        return LocationServices.getGeofencingClient(context)
    }
}