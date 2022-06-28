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
import com.samlach2222.velocityvolume.DBHelper
import com.samlach2222.velocityvolume.ProfileDrawerActivity
import com.samlach2222.velocityvolume.R
import com.samlach2222.velocityvolume.databinding.FragmentSettingsBinding

/**
 * Interface for the Settings fragment class which manages the interactivity of the Settings ui
 */
abstract class SettingsFragmentAbstract : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private lateinit var db: DBHelper

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

        // Initialize DB variable for getting and updating settings
        db = DBHelper(this.requireContext(), null)
        val settings = db.getSettings()

        // GENERAL SETTINGS

        // Units of measurements
        when (settings.getString(settings.getColumnIndex(DBHelper.UOM))) {
            kilometersString -> binding.tvUnitValue.text = resources.getString(R.string.kilometers)
            milesString -> binding.tvUnitValue.text = resources.getString(R.string.miles)
            else -> binding.tvUnitValue.text = resources.getString(R.string.kilometers)
        }
        val unitLayout: ConstraintLayout = binding.clUnit
        unitLayout.setOnClickListener {
            showUnitDialog()
        }

        // Night mode
        when (settings.getString(settings.getColumnIndex(DBHelper.NM))) {
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
        val gpsSensibilityFromSavedSettings = settings.getInt(settings.getColumnIndex(DBHelper.GPSD))
        gpsSensibilitySeekbar.progress = gpsSensibilityFromSavedSettings
        binding.tvGpsSensibilityValue.text = gpsSensibilityFromSavedSettings.toString()
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

        // [DEPENDS ON FLAVOR] Rate this app
        initialiseRateAppLayout(binding)

        return root
    }

    /**
     * Called when the view is being destroyed
     */
    override fun onDestroyView() {
        (activity as ProfileDrawerActivity).lockDrawerLayout(DrawerLayout.LOCK_MODE_UNLOCKED)
        super.onDestroyView()
        _binding = null
        db.close()
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
    protected fun DEBUGToast(message: Any) {
        Toast.makeText(this.context, "$message", Toast.LENGTH_SHORT).show()
    }

    /**
     * DEBUG method to display the value of [selectedValue]
     */
    protected fun DEBUGToastSelectedValue(selectedValue: Any) {
        Toast.makeText(this.context, "Selected value : $selectedValue", Toast.LENGTH_SHORT).show()
    }

    /**
     * Show an alert dialog to let the user choose the unit
     */
    private fun showUnitDialog() {
        val dialogBuilder = AlertDialog.Builder(this.requireContext())
        dialogBuilder.setTitle(resources.getString(R.string.unit_of_measurement))
        val items = resources.getStringArray(R.array.units_of_measurement)

        val settings = db.getSettings()
        val checkedItem = when(settings.getString(settings.getColumnIndex(DBHelper.UOM))) {
            kilometersString -> 0
            milesString -> 1
            else -> 0
        }

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

        db.updateUnitOfMeasurement(selectedValue)
        DEBUGToastSelectedValue(selectedValue)
    }

    /**
     * Show an alert dialog to let the user choose the night mode
     */
    private fun showNightModeDialog() {
        val dialogBuilder = AlertDialog.Builder(this.requireContext())
        dialogBuilder.setTitle(resources.getString(R.string.night_mode))
        val items = resources.getStringArray(R.array.night_modes)

        val settings = db.getSettings()
        val checkedItem = when(settings.getString(settings.getColumnIndex(DBHelper.NM))) {
            systemString -> 0
            onString -> 1
            offString -> 2
            else -> 0
        }

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
        // Update subtext with the selected night mode and precise a restart is needed
        val textViewNightModeCurrentValue = this.requireView().findViewById<TextView>(R.id.tv_nightModeValue)
        val restartNeededString = resources.getString(R.string.restart_needed)
        when (selectedValue) {
            systemString -> {
                textViewNightModeCurrentValue.text = resources.getString(R.string.system) + ' ' + restartNeededString
            }
            onString -> {
                textViewNightModeCurrentValue.text = resources.getString(R.string.on) + ' ' + restartNeededString
            }
            offString -> {
                textViewNightModeCurrentValue.text = resources.getString(R.string.off) + ' ' + restartNeededString
            }
        }

        db.updateNightMode(selectedValue)
        DEBUGToastSelectedValue(selectedValue)
    }

    /**
     * Apply the gps sensibility selected
     */
    private fun onSeekbarGPSSensibilityStopTrackingTouch(seekBar: SeekBar?) {
        val selectedValue = seekBar?.progress

        if (selectedValue != null) {
            db.updateGPSDifference(selectedValue)
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
     * Overridable function to initialise the layout for rating the app
     */
    protected open fun initialiseRateAppLayout(binding: FragmentSettingsBinding) {

    }

    /**
     * const values which correspond to the database enums
     */
    companion object DatabaseEnums {
        const val kilometersString = "km"
        const val milesString = "miles"
        const val systemString = "system"
        const val onString = "on"
        const val offString = "off"
    }
}