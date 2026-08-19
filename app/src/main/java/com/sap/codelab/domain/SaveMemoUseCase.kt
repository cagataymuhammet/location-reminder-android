package com.sap.codelab.domain

import com.sap.codelab.data.model.Memo
import com.sap.codelab.data.repository.IMemoRepository
import com.sap.codelab.utils.location.GeofenceManager
import javax.inject.Inject

/**
 * Created by M.Çağatay
 * Created on 17.08.2026
 * SaveMemoUseCase saves a memo to the repository.
 */
internal class SaveMemoUseCase @Inject constructor(
    private val repository: IMemoRepository,
    private val geofenceManager: GeofenceManager
) {

    /*
    * Saves the given memo to the repository.
     */
    suspend operator fun invoke(memo: Memo) {
        repository.saveMemo(memo)
        geofenceManager.registerGeofence(
            memoId = memo.id,
            latitude = memo.reminderLatitude,
            longitude = memo.reminderLongitude
        )
    }
}