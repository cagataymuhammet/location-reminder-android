package com.sap.codelab.domain

import com.sap.codelab.data.model.Memo
import com.sap.codelab.data.repository.IMemoRepository
import javax.inject.Inject

/**
 * Created by M.Çağatay
 * Created on 17.08.2026
 */

internal class SaveMemoUseCase @Inject constructor(private val repository: IMemoRepository) {

    suspend operator fun invoke(memo: Memo) {
        repository.saveMemo(memo)
    }
}