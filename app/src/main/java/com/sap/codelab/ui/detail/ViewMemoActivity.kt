package com.sap.codelab.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.sap.codelab.R
import com.sap.codelab.data.model.Memo
import com.sap.codelab.databinding.ActivityViewMemoBinding
import com.sap.codelab.ui.BaseBindingActivity
import com.sap.codelab.utils.Constants
import com.sap.codelab.utils.location.LocationMapController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng



/**
 * Activity that allows a user to see the details of a memo.
 */
@AndroidEntryPoint
internal class ViewMemoActivity : BaseBindingActivity<ActivityViewMemoBinding>() {

    private val viewMemoViewModel: ViewMemoViewModel by viewModels()

    /*
     * Inflates the layout for this activity.
     */
    override fun inflateBinding(inflater: LayoutInflater): ActivityViewMemoBinding {
       return  ActivityViewMemoBinding.inflate(layoutInflater)
    }

    private lateinit var locationMapController: LocationMapController

    /*
     * Initializes the activity.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        // Initialize views with the passed memo id
        setupMap(savedInstanceState)
        observeMemo()
        if (savedInstanceState == null) {
            loadMemo()
        }
    }

    /*
    * Sets up the map.
     */
    private fun setupMap(savedInstanceState: Bundle?) {
        binding.contentMemo.locationTitle.text = getString(R.string.selected_location)
        locationMapController = LocationMapController(
            mapView = binding.contentMemo.mapView,
            lifecycleOwner = this,
            savedInstanceState = savedInstanceState,
            isSelectable = false
        )
    }


    /**
     * Observes memo changes and updates the UI.
     */
    private fun observeMemo() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewMemoViewModel.memo.collect { memo ->
                    memo ?: return@collect
                    updateUI(memo)
                    showMemoLocation(memo)
                }
            }
        }
    }


    /**
     * Loads the memo using the id passed to this Activity.
     */
    private fun loadMemo() {
        val memoId = intent.getLongExtra(Constants.BUNDLE_MEMO_ID, -1L)

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
        binding.contentMemo.run {
            memoTitle.setText(memo.title)
            memoDescription.setText(memo.description)
            memoTitle.isEnabled = false
            memoDescription.isEnabled = false
        }
    }

    /**
     * Shows the location of the given memo.
     *
     * @param memo - the memo whose location is to be displayed.
     */
    private fun showMemoLocation(memo: Memo) {
        locationMapController.setLocation(
            latLng = LatLng(
                memo.reminderLatitude,
                memo.reminderLongitude
            ),
            moveCamera = true
        )
    }

}
