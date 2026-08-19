package com.sap.codelab.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.viewbinding.ViewBinding
import com.sap.codelab.R

/**
 * Created by M.Çağatay
 * Created on 17.08.2026
 * Base activity that centralizes common Activity behavior such as
 * ViewBinding initialization and edge-to-edge window configuration support required for targeting API 35 and above
 * @param VB ViewBinding type used by the child Activity.
 */
internal abstract class BaseBindingActivity<VB : ViewBinding> : AppCompatActivity() {

    /**
     * This field is used to access the binding object in the child activity.
     */
    protected lateinit var binding: VB
        private set


    /**
     * Inflates the view binding for the activity.
     */
    protected abstract fun inflateBinding(inflater: LayoutInflater): VB


    /*
     * Called when the activity is starting. This is where most initialization should go
     */
    override fun onCreate(savedInstanceState: Bundle?) {

        /*
        *  Sets up edge-to-edge display.
         */
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = inflateBinding(layoutInflater)
        setContentView(binding.root)
        setupSystemBarsInset()
        setupNavigationBarInset()
    }


    /*
     * Sets up edge-to-edge display.
     */
    private fun setupSystemBarsInset() {
        val appBar = findViewById<View>(R.id.appBar) ?: return

        ViewCompat.setOnApplyWindowInsetsListener(appBar) { view, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = statusBarInsets.top)
            insets
        }

        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
            false
    }

    /*
    * Sets up navigation bar inset for the activity.
     */
    private fun setupNavigationBarInset() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { target, insets ->

            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            target.updatePadding(bottom = bottomInset)

            insets
        }
    }

}