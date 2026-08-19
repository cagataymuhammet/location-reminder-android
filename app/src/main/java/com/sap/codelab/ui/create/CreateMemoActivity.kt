package com.sap.codelab.ui.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.sap.codelab.R
import com.sap.codelab.databinding.ActivityCreateMemoBinding
import com.sap.codelab.ui.BaseBindingActivity
import com.sap.codelab.utils.extensions.empty
import com.sap.codelab.utils.extensions.showPermissionWarning
import com.sap.codelab.utils.location.LocationMapController
import com.sap.codelab.utils.permission.PermissionManager
import com.sap.codelab.utils.permission.PermissionType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Activity that allows a user to create a new Memo.
 */
@AndroidEntryPoint
internal class CreateMemoActivity : BaseBindingActivity<ActivityCreateMemoBinding>() {

    private val createMemoViewModel: CreateMemoViewModel by viewModels()

    private lateinit var locationMapController: LocationMapController

    @Inject
    lateinit var permissionManager: PermissionManager

    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null

    /*
     * Inflates the layout for this activity.
     */
    override fun inflateBinding(inflater: LayoutInflater): ActivityCreateMemoBinding {
         return ActivityCreateMemoBinding.inflate(layoutInflater)
    }

    /*
     * Initializes the activity.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        permissionManager.register(this)
        observeMemoSaved()
        setupMap(savedInstanceState)
     }


    /*
    * Sets up the map.
     */
    private fun setupMap(savedInstanceState: Bundle?) {
        binding.contentMemo.locationTitle.text = getString(R.string.select_location)
        locationMapController = LocationMapController(
            mapView = binding.contentMemo.mapView,
            lifecycleOwner = this,
            savedInstanceState = savedInstanceState
        ) { latLng ->
            selectedLatitude = latLng.latitude
            selectedLongitude = latLng.longitude
        }
    }

    /*
     * Observes the memoSaved flow in the CreateMemoViewModel.
     */
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

    /*
     * Handles the save button click.
     */
    private fun onSaveClicked() {
        permissionManager.ensureNotificationPermissions(
            onGranted = {
                saveMemo()
            },
            onDenied = { permissionType ->
                showPermissionWarning(permissionType)
            }
        )
    }

    /*
     * Shows a warning snackbar for the given permission.
     */
    private fun showPermissionWarning(permissionType: PermissionType) {
        binding.root.showPermissionWarning(
            permissionType = permissionType,
            duration = Snackbar.LENGTH_INDEFINITE,
            onEnableClick = {
                requestReminderPermissions()
            }
        )
    }

    /*
     * Requests the reminder permissions.
     */
    private fun requestReminderPermissions() {
        permissionManager.ensureNotificationPermissions(
            onGranted = {
                saveMemo()
            },
            onDenied = { permissionType ->
                showPermissionWarning(permissionType)
            }
        )
    }


    /**
     * Saves the memo if the input is valid; otherwise shows the corresponding error messages.
     */
    private fun saveMemo() {
        binding.contentMemo.run {

            val latitude = selectedLatitude
            val longitude = selectedLongitude

            if (latitude == null || longitude == null) {
                Toast.makeText(applicationContext, R.string.select_location, Toast.LENGTH_SHORT)
                    .show()
                return
            }

            createMemoViewModel.updateMemo(
                title = memoTitle.text.toString(),
                description = memoDescription.text.toString(),
                latitude = latitude,
                longitude = longitude
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

    /*
     * Inflates the menu for this activity.
     */
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
                onSaveClicked()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
