package com.sap.codelab.utils.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sap.codelab.domain.RegisterAllGeofencesUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by M.Çağatay
 * Created on 17.08.2026
 * Re-registers active geofences after the device restarts.
 */
@AndroidEntryPoint
internal class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var registerAllGeofencesUseCase: RegisterAllGeofencesUseCase

    /*
     * Called when the BroadcastReceiver is receiving an Intent broadcast.
     */
    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        val pendingResult = goAsync()

        /*
         * Runs the operation asynchronously on the IO dispatcher with an isolated coroutine job.
         */
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                registerAllGeofencesUseCase()
            } finally {
                pendingResult.finish()
            }
        }
    }
}