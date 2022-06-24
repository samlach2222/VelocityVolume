package com.samlach2222.velocityvolume.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.play.core.review.ReviewManagerFactory
import com.samlach2222.velocityvolume.ProfileDrawerActivity
import com.samlach2222.velocityvolume.R
import com.samlach2222.velocityvolume.databinding.FragmentSettingsBinding


// Internal values have to be the same in every language
const val milesString = "miles"
const val kilometersString = "kilometers"
const val systemString = "system"
const val onString = "on"
const val offString = "off"

/**
 * The Settings fragment class manages the interactivity of the Settings ui
 */
class SettingsFragment : Fragment() {
// TODO : UPDATE THE SETTINGS WHEN THE USER CHANGE A VALUE
    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    /**
     * Called when the view is being created
     * Initialize some texts with the current settings values and interactivity of the interactable elements
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val slideshowViewModel =
            ViewModelProvider(this)[SettingsViewModel::class.java]

        setActivityTitle(getString(R.string.settingsActionBar))

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // GENERAL SETTINGS

        // Units of measurements
        val unitFromSavedSettings: String? = null  // TODO : Get the unit stored in the saved settings
        when (unitFromSavedSettings) {
            kilometersString -> binding.tvUnitValue.text = resources.getString(R.string.kilometers)
            milesString -> binding.tvUnitValue.text = resources.getString(R.string.miles)
            else -> binding.tvUnitValue.text = resources.getString(R.string.kilometers)
        }
        val unitLayout: ConstraintLayout = binding.clUnit
        unitLayout.setOnClickListener {
            showUnitDialog()
        }

        // Night mode
        val nightModeFromSavedSettings: String? = null  // TODO : Get the night mode stored in the saved settings
        when (nightModeFromSavedSettings) {
            systemString -> binding.tvNightModeValue.text = resources.getString(R.string.system)
            onString -> binding.tvNightModeValue.text = resources.getString(R.string.on)
            offString -> binding.tvNightModeValue.text = resources.getString(R.string.off)
            else -> binding.tvNightModeValue.text = resources.getString(R.string.system)
        }
        val nightModeLayout: ConstraintLayout = binding.clNightMode
        nightModeLayout.setOnClickListener {
            showNightModeDialog()
        }

        // GPS sensibility
        val gpsSensibilitySeekbar: SeekBar = binding.sGpsSensibility
        val gpsSensibilityFromSavedSettings: Int? = null  // TODO : Get the gps sensibility stored in the saved settings
        if (gpsSensibilityFromSavedSettings == null) {
            val defaultSeekbarValue = 0
            gpsSensibilitySeekbar.progress = defaultSeekbarValue
            binding.tvGpsSensibilityValue.text = defaultSeekbarValue.toString()
        } else {
            gpsSensibilitySeekbar.progress = gpsSensibilityFromSavedSettings
            binding.tvGpsSensibilityValue.text = gpsSensibilityFromSavedSettings.toString()
        }
        gpsSensibilitySeekbar.setOnSeekBarChangeListener (object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                requireView().findViewById<TextView>(R.id.tv_gpsSensibilityValue).text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                //Do nothing
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                onSeekbarGPSSensibilityStopTrackingTouch(seekBar)
            }
        })

        // SAVING

        // Import settings
        val importSettingsLayout: ConstraintLayout = binding.clImportSettings
        importSettingsLayout.setOnClickListener {
            importSettings()
        }

        // Export settings
        val exportSettingsLayout: ConstraintLayout = binding.clExportSettings
        exportSettingsLayout.setOnClickListener {
            exportSettings()
        }

        // ABOUT

        // GitHub
        val githubLayout: ConstraintLayout = binding.clGithubPage
        githubLayout.setOnClickListener {
            githubPage()
        }

        // Report a bug
        val reportBugLayout: ConstraintLayout = binding.clReportBugPage
        reportBugLayout.setOnClickListener {
            reportBugPage()
        }

        // Rate this app
        val rateAppLayout: ConstraintLayout = binding.clRateApplication
        rateAppLayout.setOnClickListener {
            rateApp()
        }

        return root
    }

    /**
     * Called when the view is being destroyed
     */
    override fun onDestroyView() {
        (activity as ProfileDrawerActivity).lockDrawerLayout(DrawerLayout.LOCK_MODE_UNLOCKED)
        super.onDestroyView()
        _binding = null
    }

    /**
     * Change the title of the activity
     */
    private fun Fragment.setActivityTitle(title: String) {
        (activity as AppCompatActivity?)?.supportActionBar?.title = title
    }

    /**
     * DEBUG method to display [message]
     */
    private fun DEBUGToast(message: Any) {
        Toast.makeText(this.context, "$message", Toast.LENGTH_SHORT).show()
    }

    /**
     * DEBUG method to display the value of [selectedValue]
     */
    private fun DEBUGToastSelectedValue(selectedValue: Any) {
        Toast.makeText(this.context, "Selected value : $selectedValue", Toast.LENGTH_SHORT).show()
    }

    /**
     * Show an alert dialog to let the user choose the unit
     */
    private fun showUnitDialog() {
        val dialogBuilder = AlertDialog.Builder(this.requireContext())
        dialogBuilder.setTitle(resources.getString(R.string.unit_of_measurement))
        val items = resources.getStringArray(R.array.units_of_measurement)
        val checkedItem = 0  // TODO : Get the unit stored in the saved settings
        dialogBuilder.setSingleChoiceItems(
            items, checkedItem
        ) { dialog, which ->
            when (which) {
                0 -> unitChange(kilometersString)
                1 -> unitChange(milesString)
            }
            dialog.dismiss()
        }
        val dialog = dialogBuilder.create()
        dialog.show()
    }

    /**
     * Apply the unit selected
     */
    private fun unitChange(selectedValue: String) {
        // Update subtext with the selected unit
        val textViewUnitCurrentValue = this.requireView().findViewById<TextView>(R.id.tv_unitValue)
        when (selectedValue) {
            kilometersString -> textViewUnitCurrentValue.text = resources.getString(R.string.kilometers)
            milesString -> textViewUnitCurrentValue.text = resources.getString(R.string.miles)
        }

        // TODO : Update the saved settings with the selected unit
        DEBUGToastSelectedValue(selectedValue)
    }

    /**
     * Show an alert dialog to let the user choose the night mode
     */
    private fun showNightModeDialog() {
        val dialogBuilder = AlertDialog.Builder(this.requireContext())
        dialogBuilder.setTitle(resources.getString(R.string.night_mode))
        val items = resources.getStringArray(R.array.night_modes)
        val checkedItem = 0  // TODO : Get the unit stored in the saved settings
        dialogBuilder.setSingleChoiceItems(
            items, checkedItem
        ) { dialog, which ->
            when (which) {
                0 -> nightModeChange(systemString)
                1 -> nightModeChange(onString)
                2 -> nightModeChange(offString)
            }
            dialog.dismiss()
        }
        val dialog = dialogBuilder.create()
        dialog.show()
    }

    /**
     * Apply the night mode selected
     */
    private fun nightModeChange(selectedValue: String) {
        // Update subtext with the selected night mode
        val textViewNightModeCurrentValue = this.requireView().findViewById<TextView>(R.id.tv_nightModeValue)
        when (selectedValue) {
            systemString -> textViewNightModeCurrentValue.text = resources.getString(R.string.system)
            onString -> textViewNightModeCurrentValue.text = resources.getString(R.string.on)
            offString -> textViewNightModeCurrentValue.text = resources.getString(R.string.off)
        }

        // TODO : Update the saved settings with the selected night mode
        DEBUGToastSelectedValue(selectedValue)
    }

    /**
     * Apply the gps sensibility selected
     */
    private fun onSeekbarGPSSensibilityStopTrackingTouch(seekBar: SeekBar?) {
        val selectedValue = seekBar?.progress

        if (selectedValue != null) {
            // TODO : Update the saved settings with the selected gps sensibility
            DEBUGToastSelectedValue(selectedValue)
        }
    }

    /**
     * Import the settings from a file
     */
    private fun importSettings() {
        // TODO : Implement import settings
        DEBUGToast("importSettings isn't implemented")
    }

    /**
     * Export the settings to a file
     */
    private fun exportSettings() {
        // TODO : Implement export settings
        DEBUGToast("exportSettings isn't implemented")
    }

    /**
     * Go to the github page of the project
     */
    private fun githubPage() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/samlach2222/VelocityVolume"))
        startActivity(browserIntent)
    }

    /**
     * Go the github page of the project for creating a new issue
     */
    private fun reportBugPage() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/samlach2222/VelocityVolume/issues/new"))
        startActivity(browserIntent)
    }

    /**
     * Show a dialog to rate the app on the google play store
     */
    private fun rateApp() {
        DEBUGToast("rateApp called")

        val manager = ReviewManagerFactory.create(this.requireContext())

        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = task.result
                val flow = reviewInfo?.let { manager.launchReviewFlow(requireActivity(), it) }
                flow?.addOnCompleteListener { _ ->
                    DEBUGToast("App rated")
                }
            }
        }
    }
}