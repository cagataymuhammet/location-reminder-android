package com.sap.codelab.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sap.codelab.data.model.Memo
import com.sap.codelab.domain.SaveMemoUseCase
import com.sap.codelab.utils.extensions.empty
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for matching CreateMemo view. Handles user interactions.
 */
@HiltViewModel
internal class CreateMemoViewModel @Inject constructor(private val saveMemoUseCase: SaveMemoUseCase) :
    ViewModel() {

    private var memo = Memo(0, String.empty(), String.empty(), 0, 0, 0, false)

    private val _memoSaved = MutableSharedFlow<Unit>()
    val memoSaved: SharedFlow<Unit> = _memoSaved

    /**
     * Saves the memo in it's current state.
     */
    fun saveMemo() {
        viewModelScope.launch {
            saveMemoUseCase(memo)
            _memoSaved.emit(Unit)
        }
    }

    /**
     * Call this method to update the memo. This is usually needed when the user changed his input.
     */
    fun updateMemo(title: String, description: String) {
        memo = memo.copy(title = title, description = description)
    }

    /**
     * @return true if the title and content are not blank; false otherwise.
     */
    fun isMemoValid(): Boolean = memo.title.isNotBlank() && memo.description.isNotBlank()

    /**
     * @return true if the memo text is blank, false otherwise.
     */
    fun hasTextError() = memo.description.isBlank()

    /**
     * @return true if the memo title is blank, false otherwise.
     */
    fun hasTitleError() = memo.title.isBlank()
}