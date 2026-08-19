package com.sap.codelab.domain

import com.sap.codelab.data.model.Memo
import com.sap.codelab.data.repository.IMemoRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

/**
 * Created by M.Çağatay
 * Created on 19.08.2026
 */
internal class GetMemoByIdUseCaseTest {

    private lateinit var repository: IMemoRepository
    private lateinit var useCase: GetMemoByIdUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetMemoByIdUseCase(repository)
    }

    @Test
    fun returnsMemo_whenMemoExists() = runTest {
        val memoId = 10L
        val memo = mockk<Memo>()

        coEvery { repository.getMemoById(memoId) } returns memo

        val result = useCase(memoId)

        assertSame(memo, result)

        coVerify(exactly = 1) { repository.getMemoById(memoId) }
    }

    @Test
    fun returnsNull_whenMemoDoesNotExist() = runTest {
        val memoId = 10L

        coEvery { repository.getMemoById(memoId) } returns null

        val result = useCase(memoId)

        assertNull(result)

        coVerify(exactly = 1) { repository.getMemoById(memoId) }
    }
}