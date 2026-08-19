package com.sap.codelab.domain

import com.sap.codelab.data.repository.IMemoRepository
import com.sap.codelab.utils.location.GeofenceManager
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Created by M.Çağatay
 * Created on 18.08.2026
 * RegisterAllGeofencesUseCase registers all geofences for all open memos in the database.
 */
internal class RegisterAllGeofencesUseCase @Inject constructor(
    private val memoRepository: IMemoRepository,
    private val geofenceManager: GeofenceManager
) {

    /*
    * Registers all geofences for all open memos in the database.
     */
    suspend operator fun invoke() {
        val memos = memoRepository.observeAllMemos().first()

        memos.filter { memo ->
            !memo.isDone && memo.reminderLatitude != 0.0 && memo.reminderLongitude != 0.0
        }.forEach { memo ->
            geofenceManager.registerGeofence(
                memoId = memo.id,
                latitude = memo.reminderLatitude,
                longitude = memo.reminderLongitude
            )
        }
    }
}