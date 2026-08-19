package com.sap.codelab.ui.home

import com.sap.codelab.MainDispatcherRule
import com.sap.codelab.data.model.Memo
import com.sap.codelab.domain.ObserveAllMemosUseCase
import com.sap.codelab.domain.ObserveOpenMemosUseCase
import com.sap.codelab.domain.SaveMemoUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Created by M.Çağatay
 * Created on 19.08.2026
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var observeAllMemosUseCase: ObserveAllMemosUseCase
    private lateinit var observeOpenMemosUseCase: ObserveOpenMemosUseCase
    private lateinit var saveMemoUseCase: SaveMemoUseCase

    private lateinit var viewModel: HomeViewModel

    private lateinit var allMemosFlow: MutableStateFlow<List<Memo>>
    private lateinit var openMemosFlow: MutableStateFlow<List<Memo>>

    @Before
    fun setUp() {
        observeAllMemosUseCase = mockk()
        observeOpenMemosUseCase = mockk()
        saveMemoUseCase = mockk()

        allMemosFlow = MutableStateFlow(emptyList())
        openMemosFlow = MutableStateFlow(emptyList())

        every { observeAllMemosUseCase() } returns allMemosFlow

        every { observeOpenMemosUseCase() } returns openMemosFlow

        viewModel = HomeViewModel(
            observeAllMemosUseCase = observeAllMemosUseCase,
            observeOpenMemosUseCase = observeOpenMemosUseCase,
            saveMemoUseCase = saveMemoUseCase
        )
    }

    @Test
    fun returnsOpenMemos_byDefault() = runTest {
        collectMemos()

        val expectedMemos = listOf(
            mockk<Memo>(),
            mockk<Memo>()
        )

        openMemosFlow.value = expectedMemos

        advanceUntilIdle()

        assertEquals(expectedMemos, viewModel.memos.value)
    }

    @Test
    fun returnsAllMemos_whenLoadMemosCalledWithTrue() = runTest {
        collectMemos()

        val expectedMemos = listOf(
            mockk<Memo>(),
            mockk<Memo>(),
            mockk<Memo>()
        )

        allMemosFlow.value = expectedMemos

        viewModel.loadMemos(true)

        advanceUntilIdle()

        assertEquals(expectedMemos, viewModel.memos.value)
    }

    @Test
    fun returnsOpenMemos_whenLoadMemosCalledWithFalse() = runTest {
        collectMemos()

        val expectedMemos = listOf(
            mockk<Memo>()
        )

        openMemosFlow.value = expectedMemos

        viewModel.loadMemos(true)
        viewModel.loadMemos(false)

        advanceUntilIdle()

        assertEquals(expectedMemos, viewModel.memos.value)
    }

    @Test
    fun doesNotUpdateMemo_whenMemoIsUnchecked() = runTest {
        val memo = mockk<Memo>()

        viewModel.updateMemo(memo = memo, isChecked = false)

        advanceUntilIdle()

        coVerify(exactly = 0) { saveMemoUseCase(any()) }
    }

    @Test
    fun updatesMemoAsDone_whenMemoIsChecked() = runTest {
        val memo = mockk<Memo>()
        val completedMemo = mockk<Memo>()

        every { memo.copy(isDone = true) } returns completedMemo

        coEvery { saveMemoUseCase(completedMemo) } returns Unit

        viewModel.updateMemo(memo = memo, isChecked = true)

        advanceUntilIdle()

        coVerify(exactly = 1) { saveMemoUseCase(completedMemo) }
    }

    /**
     * Starts collecting the StateFlow because HomeViewModel uses
     * SharingStarted.WhileSubscribed.
     */
    private fun kotlinx.coroutines.test.TestScope.collectMemos() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.memos.collect()
        }
    }
}