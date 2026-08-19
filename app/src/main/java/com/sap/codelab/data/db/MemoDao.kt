package com.sap.codelab.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sap.codelab.data.model.Memo
import kotlinx.coroutines.flow.Flow

/**
 * The Dao representation of a Memo.
 */
@Dao
internal interface MemoDao {

    /**
     * @return all memos that are currently in the database.
     */
    @Query("SELECT * FROM ${MEMO_TABLE_NAME}")
    fun observeAllMemos(): Flow<List<Memo>>

    /**
     * @return all memos that are currently in the database and have not yet been marked as "done".
     */
    @Query("SELECT * FROM ${MEMO_TABLE_NAME} WHERE isDone = 0")
    fun observeOpenMemos(): Flow<List<Memo>>

    /**
     * Inserts the given Memo into the database. We currently do not support updating of memos.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memo: Memo)

    /**
     * @return the memo whose id matches the given id.
     */
    @Query("SELECT * FROM ${MEMO_TABLE_NAME} WHERE id = :memoId")
    suspend fun getMemoById(memoId: Long): Memo?

    companion object {
        const val MEMO_TABLE_NAME = "memo"
    }
}