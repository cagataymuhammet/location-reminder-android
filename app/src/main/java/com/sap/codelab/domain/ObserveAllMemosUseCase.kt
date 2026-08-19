package com.sap.codelab.domain

import com.sap.codelab.data.model.Memo
import com.sap.codelab.data.repository.IMemoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by M.Çağatay
 * Created on 17.08.2026
 * ObserveAllMemosUseCase retrieves all memos from the repository.
 */
internal class ObserveAllMemosUseCase @Inject constructor(private val repository: IMemoRepository) {

    /*
    * @return all memos currently in the database.
     */
    operator fun invoke(): Flow<List<Memo>> {
        return repository.observeAllMemos()
    }
}