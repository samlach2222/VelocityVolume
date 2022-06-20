package com.samlach2222.velocityvolume.ui.Settings

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
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
        val unitGroup: RadioGroup = binding.rgUnit
        val unitFromSavedSettings = null  // TODO : Get the unit stored in the saved settings
        when (unitFromSavedSettings) {
            kilometersString -> unitGroup.check(binding.rbKm.id)
            milesString -> unitGroup.check(binding.rbMile.id)
            else -> unitGroup.check(binding.rbKm.id)
        }
        unitGroup.setOnCheckedChangeListener { _, checkedId ->
            onRadioGroupUnitChange(checkedId)
        }

        // Night mode
        val nightModeGroup: RadioGroup = binding.rgNightMode
        val nightModeFromSavedSettings = null  // TODO : Get the night mode stored in the saved settings
        when (nightModeFromSavedSettings) {
            systemString -> nightModeGroup.check(binding.rbNightModeSystem.id)
            onString -> nightModeGroup.check(binding.rbNightModeOn.id)
            offString -> nightModeGroup.check(binding.rbNightModeOff.id)
            else -> nightModeGroup.check(binding.rbNightModeSystem.id)
        }
        nightModeGroup.setOnCheckedChangeListener { _, checkedId ->
            onRadioGroupNightModeChange(checkedId)
        }

        // GPS sensibility
        val gpsSensibilityLayout: ConstraintLayout = binding.clGpsSensibility
        gpsSensibilityLayout.setOnClickListener {
            showGPSSensibilityDialog()
        }

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

    private fun onRadioGroupUnitChange(checkedId: Int) {
        val selectedValue = when (checkedId) {
            R.id.rb_km -> kilometersString
            R.id.rb_mile -> milesString
            else -> return
        }

        // TODO : Update the saved settings with the selected unit
        DEBUGToastSelectedValue(selectedValue)
    }

    private fun onRadioGroupNightModeChange(checkedId: Int) {
        val selectedValue = when (checkedId) {
            R.id.rb_nightMode_system -> systemString
            R.id.rb_nightMode_on -> onString
            R.id.rb_nightMode_off -> offString
            else -> return
        }

        // TODO : Update the saved settings with the selected night mode
        DEBUGToastSelectedValue(selectedValue)
    }

    private fun showGPSSensibilityDialog() {
        val numberPickerGPSSensibility = NumberPicker(this.context)
        numberPickerGPSSensibility.minValue = 0
        numberPickerGPSSensibility.maxValue = 20
        numberPickerGPSSensibility.wrapSelectorWheel = false
        val numberPickerGPSSensibilityValues = Array(21) {(it - 10).toString()}
        numberPickerGPSSensibility.displayedValues = numberPickerGPSSensibilityValues
        var gpsSensibilityFromSavedSettings: Int? = null  // TODO : Get the gps sensibility stored in the saved settings
        if (gpsSensibilityFromSavedSettings == null) {
            gpsSensibilityFromSavedSettings = numberPickerGPSSensibilityValues.indexOf("0")
        }
        numberPickerGPSSensibility.value = gpsSensibilityFromSavedSettings  // value uses the index

        val dialog: AlertDialog = AlertDialog.Builder(this.context)
            .setTitle(resources.getString(R.string.gps_sensibility))
            .setMessage(resources.getString(R.string.difference_gps_actual_speed))  // toast or message ?
            .setView(numberPickerGPSSensibility)
            .setPositiveButton("OK", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.setOnClickListener {
                val selectedValue = numberPickerGPSSensibility.displayedValues[numberPickerGPSSensibility.value].toInt()

                // TODO : Update the saved settings with the selected gps sensibility
                DEBUGToastSelectedValue(selectedValue)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun importSettings() {
        // TODO : Implement import settings
        DEBUGToast("importSettings isn't implemented")
    }

    private fun exportSettings() {
        // TODO : Implement export settings
        DEBUGToast("exportSettings isn't implemented")
    }
}