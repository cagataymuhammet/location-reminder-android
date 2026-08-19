package com.sap.codelab.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.sap.codelab.R
import com.sap.codelab.data.model.Memo
import com.sap.codelab.databinding.ActivityHomeBinding
import com.sap.codelab.ui.BaseBindingActivity
import com.sap.codelab.ui.create.CreateMemoActivity
import com.sap.codelab.ui.detail.ViewMemoActivity
import com.sap.codelab.utils.Constants
import com.sap.codelab.utils.extensions.showPermissionWarning
import com.sap.codelab.utils.permission.PermissionManager
import com.sap.codelab.utils.permission.PermissionType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The main activity of the app. Shows a list of recorded memos and lets the user add new memos.
 */
@AndroidEntryPoint
internal class HomeActivity : BaseBindingActivity<ActivityHomeBinding>() {

    override fun inflateBinding(inflater: LayoutInflater): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(inflater)
    }

    private val homeViewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var permissionManager: PermissionManager
    private var permissionSnackbar: Snackbar? = null
    private var hasActiveMemo = false

    private lateinit var menuItemShowAll: MenuItem
    private lateinit var menuItemShowOpen: MenuItem

    /*
     * Called when the activity is starting. This is where most initialization should go
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        permissionManager.register(this)

        // Setup the adapter, fab and the recycler view
        val adapter = initializeAdapter()
        setupRecyclerView(adapter)
        setupFabButton()

        observeMemos(adapter)
    }


    /**
     * Observes the memos and updates the adapter when they change.
     *  Check permissions if there are any active memos
     */
    private fun observeMemos(adapter: MemoAdapter) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.memos.collect { memos ->

                    adapter.setItems(memos)

                    // Check permissions if there are any active memos
                    hasActiveMemo = memos.any { memo -> !memo.isDone }

                    if (hasActiveMemo) {
                        checkReminderPermissions()
                    } else {
                        hidePermissionWarning()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasActiveMemo) {
            checkReminderPermissions()
        }
    }

    /*
     * Checks the reminder permissions.
     */
    private fun checkReminderPermissions() {
        val missingPermission =
            permissionManager.getMissingReminderPermission()

        if (missingPermission == null) {
            hidePermissionWarning()
        } else {
            showPermissionWarning(missingPermission)
        }
    }

    /*
     * Shows a warning snackbar for the given permission.
     */
    private fun showPermissionWarning(permissionType: PermissionType) {
        permissionSnackbar?.dismiss()
        permissionSnackbar = binding.root.showPermissionWarning(
            permissionType = permissionType,
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
                hidePermissionWarning()
            },
            onDenied = { permissionType ->
                showPermissionWarning(permissionType)
            }
        )
    }

    /*
     * Hides the permission warning snackbar.
     */
    private fun hidePermissionWarning() {
        permissionSnackbar?.dismiss()
        permissionSnackbar = null
    }


    /*
     * Initializes the fab button and sets the needed callbacks.
     */
    private fun setupFabButton() {
        binding.fab.setOnClickListener {
            startActivity(
                Intent(this@HomeActivity, CreateMemoActivity::class.java)
            )
        }
    }

    /**
     * Initializes the adapter and sets the needed callbacks.
     */
    private fun initializeAdapter(): MemoAdapter {
        return MemoAdapter(
            mutableListOf(),
            { view ->
                showMemo(
                    (view.tag as Memo).id
                )
            },
            { checkbox, isChecked ->
                homeViewModel.updateMemo(
                    checkbox.tag as Memo,
                    isChecked
                )
            }
        )
    }

    /**
     * Opens the Memo detail view for the given memoId.
     *
     * @param memoId    - the id of the memo to be shown.
     */
    private fun showMemo(memoId: Long) {
        startActivity(
            Intent(this, ViewMemoActivity::class.java).apply {
                putExtra(Constants.BUNDLE_MEMO_ID, memoId)
            }
        )
    }

    /**
     * Initializes the recycler view to display the list of memos.
     */
    private fun setupRecyclerView(adapter: MemoAdapter) {
        binding.contentHome.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity, LinearLayoutManager.VERTICAL, false)
            this.adapter = adapter
            addItemDecoration(
                DividerItemDecoration(
                    this@HomeActivity,
                    LinearLayoutManager.VERTICAL
                )
            )
        }
    }


    /*
     * Creates the options menu.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        menuItemShowAll = menu.findItem(R.id.action_show_all)
        menuItemShowOpen = menu.findItem(R.id.action_show_open)
        return true
    }

    /**
     * Handles actionbar interactions.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.action_show_all -> {
                homeViewModel.loadMemos(true)
                menuItemShowAll.isVisible = false
                menuItemShowOpen.isVisible = true
                true
            }

            R.id.action_show_open -> {
                homeViewModel.loadMemos(false)
                menuItemShowOpen.isVisible = false
                menuItemShowAll.isVisible = true
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
