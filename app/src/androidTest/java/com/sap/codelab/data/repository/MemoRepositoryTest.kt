package com.sap.codelab.data.repository

import com.sap.codelab.data.db.MemoDao
import com.sap.codelab.data.model.Memo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

/**
 * Created by M.Çağatay
 * Created on 19.08.2026
 */
internal class MemoRepositoryTest {

    private lateinit var memoDao: MemoDao
    private lateinit var repository: MemoRepository

    @Before
    fun setUp() {
        memoDao = mockk()
        repository = MemoRepository(memoDao)
    }

    @Test
    fun returnsAllMemosFlow_fromDao() {

        // Creates an expected Flow that represents the memo list returned by the DAO.
        val expectedFlow = MutableStateFlow<List<Memo>>(emptyList())

        // Defines the mocked DAO behavior:
        // when observeAllMemos() is called, return the expected Flow.
        every { memoDao.observeAllMemos() } returns expectedFlow

        // Calls the repository method that should delegate the request to the DAO.
        val result = repository.observeAllMemos()

        // Verifies that the repository returns the exact same Flow instance provided by the DAO.
        assertSame(expectedFlow, result)

        // Verifies that observeAllMemos() was called exactly once on the DAO.
        verify(exactly = 1) { memoDao.observeAllMemos() }
    }

    @Test
    fun returnsOpenMemosFlow_fromDao() {

        val expectedFlow = MutableStateFlow<List<Memo>>(emptyList())

        every { memoDao.observeOpenMemos() } returns expectedFlow

        val result = repository.observeOpenMemos()

        assertSame(expectedFlow, result)

        verify(exactly = 1) { memoDao.observeOpenMemos() }
    }

    @Test
    fun insertsMemo_intoDao() = runTest {
        val memo = mockk<Memo>()

        coEvery { memoDao.insert(memo) } returns Unit

        repository.saveMemo(memo)

        coVerify(exactly = 1) { memoDao.insert(memo) }
    }


    @Test
    fun returnsMemo_whenMemoExists() = runTest {
        val memoId = 10L
        val expectedMemo = mockk<Memo>()

        coEvery { memoDao.getMemoById(memoId) } returns expectedMemo

        val result = repository.getMemoById(memoId)

        assertSame(expectedMemo, result)

        coVerify(exactly = 1) { memoDao.getMemoById(memoId) }
    }


    @Test
    fun returnsNull_whenMemoDoesNotExist() = runTest {
        val memoId = 10L

        coEvery { memoDao.getMemoById(memoId) } returns null

        val result = repository.getMemoById(memoId)

        assertNull(result)

        coVerify(exactly = 1) { memoDao.getMemoById(memoId) }
    }
}