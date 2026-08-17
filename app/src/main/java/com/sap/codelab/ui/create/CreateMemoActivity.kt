package com.sap.codelab.ui.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.sap.codelab.R
import com.sap.codelab.databinding.ActivityCreateMemoBinding
import com.sap.codelab.ui.BaseBindingActivity
import com.sap.codelab.utils.extensions.empty
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Activity that allows a user to create a new Memo.
 */
@AndroidEntryPoint
internal class CreateMemoActivity : BaseBindingActivity<ActivityCreateMemoBinding>() {

    private val createMemoViewModel: CreateMemoViewModel by viewModels()

    override fun inflateBinding(inflater: LayoutInflater): ActivityCreateMemoBinding {
         return ActivityCreateMemoBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        observeMemoSaved()
     }


    private fun observeMemoSaved() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                createMemoViewModel.memoSaved.collect {
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }
    }


    /**
     * Saves the memo if the input is valid; otherwise shows the corresponding error messages.
     */
    private fun saveMemo() {
        binding.contentCreateMemo.run {
            createMemoViewModel.updateMemo(
                title = memoTitle.text.toString(),
                description = memoDescription.text.toString()
            )

            if (createMemoViewModel.isMemoValid()) {
                createMemoViewModel.saveMemo()
            } else {
                memoTitleContainer.error = getErrorMessage(
                    createMemoViewModel.hasTitleError(),
                    R.string.memo_title_empty_error
                )
                memoDescription.error = getErrorMessage(
                    createMemoViewModel.hasTextError(),
                    R.string.memo_text_empty_error
                )
            }
        }
    }

    /**
     * Returns the error message if there is an error, or an empty string otherwise.
     *
     * @param hasError          - whether there is an error.
     * @param errorMessageResId - the resource id of the error message to show.
     * @return the error message if there is an error, or an empty string otherwise.
     */
    private fun getErrorMessage(hasError: Boolean, @StringRes errorMessageResId: Int): String {
        return if (hasError) {
            getString(errorMessageResId)
        } else {
            String.empty()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_create_memo, menu)
        return true
    }

    /**
     * Handles actionbar interactions.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                saveMemo()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
