package com.sap.codelab.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sap.codelab.data.model.Memo
import com.sap.codelab.domain.GetMemoByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for matching ViewMemo view.
 */
@HiltViewModel
internal class ViewMemoViewModel @Inject constructor(private val getMemoByIdUseCase: GetMemoByIdUseCase) :
    ViewModel() {

    private val _memo: MutableStateFlow<Memo?> = MutableStateFlow(null)
    val memo: StateFlow<Memo?> = _memo

    /**
     * Loads the memo whose id matches the given memoId from the database.
     */
    fun loadMemo(id: Long) {
        viewModelScope.launch {
            _memo.value = getMemoByIdUseCase(id)
        }
    }
}