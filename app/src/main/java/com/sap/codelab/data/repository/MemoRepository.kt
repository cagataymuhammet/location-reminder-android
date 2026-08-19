package com.sap.codelab.data.repository

import com.sap.codelab.data.db.MemoDao
import com.sap.codelab.data.model.Memo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton


/**
 * The repository is used to retrieve data from a data source.
 */
@Singleton
internal class MemoRepository @Inject constructor(private val memoDao: MemoDao) : IMemoRepository {

    /*
     * @return all memos currently in the database.
     */
    override fun observeAllMemos(): Flow<List<Memo>> = memoDao.observeAllMemos()

    /*
     * @return all memos currently in the database, except those that have been marked as "done".
     */
    override fun observeOpenMemos(): Flow<List<Memo>> = memoDao.observeOpenMemos()

    /*
     * @return the memo whose id matches the given id.
     */
    override suspend fun getMemoById(id: Long): Memo? = memoDao.getMemoById(id)

    /*
     * Saves the given memo to the database.
     */
    override suspend fun saveMemo(memo: Memo) {
        memoDao.insert(memo)
    }
}