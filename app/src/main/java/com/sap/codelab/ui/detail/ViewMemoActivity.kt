package com.sap.codelab.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.sap.codelab.data.model.Memo
import com.sap.codelab.databinding.ActivityViewMemoBinding
import com.sap.codelab.ui.BaseBindingActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

internal const val BUNDLE_MEMO_ID: String = "memoId"

/**
 * Activity that allows a user to see the details of a memo.
 */
@AndroidEntryPoint
internal class ViewMemoActivity : BaseBindingActivity<ActivityViewMemoBinding>() {

    private val viewMemoViewModel: ViewMemoViewModel by viewModels()

    override fun inflateBinding(inflater: LayoutInflater): ActivityViewMemoBinding {
       return  ActivityViewMemoBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        // Initialize views with the passed memo id

        observeMemo()
        if (savedInstanceState == null) {
            loadMemo()
        }
    }

    /**
     * Observes memo changes and updates the UI.
     */
    private fun observeMemo() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewMemoViewModel.memo.collect { memo ->
                    memo?.let(::updateUI)
                }
            }
        }
    }


    /**
     * Loads the memo using the id passed to this Activity.
     */
    private fun loadMemo() {
        val memoId = intent.getLongExtra(BUNDLE_MEMO_ID, -1L)

        if (memoId != -1L) {
            viewMemoViewModel.loadMemo(memoId)
        }
    }


    /**
     * Updates the UI with the given memo details.
     *
     * @param memo - the memo whose details are to be displayed.
     */
    private fun updateUI(memo: Memo) {
        binding.contentCreateMemo.run {
            memoTitle.setText(memo.title)
            memoDescription.setText(memo.description)
            memoTitle.isEnabled = false
            memoDescription.isEnabled = false
        }
    }
}
