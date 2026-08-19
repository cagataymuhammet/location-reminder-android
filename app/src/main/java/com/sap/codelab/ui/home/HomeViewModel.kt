package com.sap.codelab.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sap.codelab.data.model.Memo
import com.sap.codelab.domain.ObserveAllMemosUseCase
import com.sap.codelab.domain.ObserveOpenMemosUseCase
import com.sap.codelab.domain.SaveMemoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Home Activity.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
internal class HomeViewModel @Inject constructor(
    private val observeAllMemosUseCase: ObserveAllMemosUseCase,
    private val observeOpenMemosUseCase: ObserveOpenMemosUseCase,
    private val saveMemoUseCase: SaveMemoUseCase
) : ViewModel() {

    private val isShowAll = MutableStateFlow(false)

    val memos: StateFlow<List<Memo>> = isShowAll
        .flatMapLatest { showAll ->
            if (showAll) {
                observeAllMemosUseCase()
            } else {
                observeOpenMemosUseCase()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(4_000),
            initialValue = emptyList()
        )

    /**
     * Loads all memos.
     */
    fun loadMemos(isAll: Boolean) {
        isShowAll.value = isAll
    }


    /**
     * Updates the given memo, marking it as done if isChecked is true.
     *
     * @param memo      - the memo to update.
     * @param isChecked - whether the memo has been checked (marked as done).
     */
    fun updateMemo(memo: Memo, isChecked: Boolean) {
        if (!isChecked) return
        viewModelScope.launch {
            saveMemoUseCase(
                memo.copy(isDone = true)
            )
        }
    }
}