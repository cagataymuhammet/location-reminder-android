package com.sap.codelab.data.repository

import androidx.annotation.WorkerThread
import com.sap.codelab.data.db.MemoDao
import com.sap.codelab.data.model.Memo
import javax.inject.Inject
import javax.inject.Singleton


/**
 * The repository is used to retrieve data from a data source.
 */
@Singleton
internal class MemoRepository @Inject constructor(private val memoDao: MemoDao) : IMemoRepository {

    @WorkerThread
    override fun saveMemo(memo: Memo) {
        memoDao.insert(memo)
    }

    @WorkerThread
    override fun getOpen(): List<Memo> = memoDao.getOpen()

    @WorkerThread
    override fun getAll(): List<Memo> = memoDao.getAll()

    @WorkerThread
    override fun getMemoById(id: Long): Memo = memoDao.getMemoById(id)
}