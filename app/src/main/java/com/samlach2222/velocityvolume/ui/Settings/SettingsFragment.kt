package com.samlach2222.velocityvolume.ui.Settings

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.slider.Slider
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

class SettingsFragment : Fragment() {
// TODO : UPDATE THE SETTINGS WHEN THE USER CHANGE A VALUE
    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

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
        val unitFromSavedSettings = null  // TODO : Get the unit stored in the saved settings
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
        val nightModeFromSavedSettings = null  // TODO : Get the night mode stored in the saved settings
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
        val gpsSensibilitySlider: Slider = binding.sGpsSensibility
        val gpsSensibilityFromSavedSettings = null  // TODO : Get the gps sensibility stored in the saved settings
        if (gpsSensibilityFromSavedSettings == null) {
            gpsSensibilitySlider.value = 0F
        } else {
            gpsSensibilitySlider.value = gpsSensibilityFromSavedSettings as Float
        }
        gpsSensibilitySlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                //Do nothing
            }

            override fun onStopTrackingTouch(slider: Slider) {
                onSliderGPSSensibilityStopTrackingTouch(slider)
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

    override fun onDestroyView() {
        (activity as ProfileDrawerActivity).lockDrawerLayout(DrawerLayout.LOCK_MODE_UNLOCKED)
        super.onDestroyView()
        _binding = null
    }

    fun Fragment.setActivityTitle(title: String) {
        (activity as AppCompatActivity?)?.supportActionBar?.title = title
    }

    private fun DEBUGToast(message: Any) {
        Toast.makeText(this.context, "$message", Toast.LENGTH_SHORT).show()
    }

    private fun DEBUGToastSelectedValue(selectedValue: Any) {
        Toast.makeText(this.context, "Selected value : $selectedValue", Toast.LENGTH_SHORT).show()
    }

    private fun showUnitDialog() {
        // TODO : Show unit dialog with radio buttons in a vertical orientation and get the selected value
        DEBUGToast("showUnitDialog isn't implemented")
    }

    private fun showNightModeDialog() {
        // TODO : Show night mode dialog with radio buttons in a vertical orientation and get the selected value
        DEBUGToast("showNightModeDialog isn't implemented")
    }

    private fun onSliderGPSSensibilityStopTrackingTouch(slider: Slider) {
        val selectedValue = slider.value.toInt()

        // TODO : Update the saved settings with the selected gps sensibility
        DEBUGToastSelectedValue(selectedValue)
    }

    private fun importSettings() {
        // TODO : Implement import settings
        DEBUGToast("importSettings isn't implemented")
    }

    private fun exportSettings() {
        // TODO : Implement export settings
        DEBUGToast("exportSettings isn't implemented")
    }

    private fun githubPage() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/samlach2222/VelocityVolume"))
        startActivity(browserIntent)
    }

    private fun reportBugPage() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/samlach2222/VelocityVolume/issues/new"))
        startActivity(browserIntent)
    }

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