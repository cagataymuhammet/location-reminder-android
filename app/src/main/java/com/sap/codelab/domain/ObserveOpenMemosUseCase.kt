package com.sap.codelab.domain

import com.sap.codelab.data.model.Memo
import com.sap.codelab.data.repository.IMemoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by M.Çağatay
 * Created on 17.08.2026
 * ObserveOpenMemosUseCase retrieves all open memos from the repository.
 */

internal class ObserveOpenMemosUseCase @Inject constructor(private val repository: IMemoRepository) {

    /*
    * @return all memos currently in the database, except those that have been marked as "done".
     */
    operator fun invoke(): Flow<List<Memo>> {
        return repository.observeOpenMemos()
    }
}