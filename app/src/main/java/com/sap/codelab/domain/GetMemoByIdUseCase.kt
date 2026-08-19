package com.sap.codelab.domain

import com.sap.codelab.data.model.Memo
import com.sap.codelab.data.repository.IMemoRepository
import javax.inject.Inject

/**
 * Created by M.Çağatay
 * Created on 17.08.2026
 * GetMemoByIdUseCase retrieves a memo by its ID from the repository.
 */
internal class GetMemoByIdUseCase @Inject constructor(private val repository: IMemoRepository) {

    /*
     * @return the memo whose id matches the given id.
     */
    suspend operator fun invoke(id: Long): Memo? {
        return repository.getMemoById(id)
    }
}