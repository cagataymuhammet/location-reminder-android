package com.sap.codelab.data.repository

import com.sap.codelab.data.model.Memo
import kotlinx.coroutines.flow.Flow

/**
 * Interface for a repository offering memo related CRUD operations.
 */
internal interface IMemoRepository {

    /**
     * @return all memos currently in the database.
     */
    fun observeAllMemos(): Flow<List<Memo>>

    /**
     * @return all memos currently in the database, except those that have been marked as "done".
     */
    fun observeOpenMemos(): Flow<List<Memo>>

    /**
     * @return the memo whose id matches the given id.
     */
    suspend fun getMemoById(id: Long): Memo?

    /**
     * Saves the given memo to the database.
     */
    suspend fun saveMemo(memo: Memo)
}